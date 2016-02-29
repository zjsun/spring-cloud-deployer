/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.deployer.spi.local;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.process.AppInstanceStatus;
import org.springframework.cloud.deployer.spi.process.DeploymentState;
import org.springframework.cloud.deployer.spi.process.ProcessDeployer;
import org.springframework.cloud.deployer.spi.process.ProcessDeploymentId;
import org.springframework.cloud.deployer.spi.process.ProcessDeploymentRequest;
import org.springframework.cloud.deployer.spi.process.ProcessStatus;
import org.springframework.core.io.Resource;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * A {@link ProcessDeployer} implementation that spins off a new JVM process per app instance.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Mark Fisher
 */
public class LocalProcessDeployer implements ProcessDeployer {

	private Path logPathRoot;

	private static final Logger logger = LoggerFactory.getLogger(LocalProcessDeployer.class);

	private static final String SERVER_PORT_KEY = "server.port";

	private static final String JMX_DEFAULT_DOMAIN_KEY = "spring.jmx.default-domain";

	private static final int DEFAULT_SERVER_PORT = 8080;

	private static final String GROUP_DEPLOYMENT_ID = "dataflow.group-deployment-id";

	@Autowired
	private LocalDeployerProperties properties = new LocalDeployerProperties();

	private Map<ProcessDeploymentId, List<Instance>> running = new ConcurrentHashMap<>();

	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public ProcessDeploymentId deploy(ProcessDeploymentRequest request) {
		if (this.logPathRoot == null) {
			try {
				this.logPathRoot = Files.createTempDirectory(properties.getWorkingDirectoriesRoot(), "spring-cloud-dataflow-");
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		Resource resource = request.getResource();
		String jarPath;
		try {
			jarPath = resource.getFile().getAbsolutePath();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		ProcessDeploymentId processDeploymentId = new ProcessDeploymentId(request.getDefinition().getGroup(), request.getDefinition().getName());
		List<Instance> processes = new ArrayList<>();
		running.put(processDeploymentId, processes);
		boolean useDynamicPort = !request.getDefinition().getProperties().containsKey(SERVER_PORT_KEY);
		HashMap<String, String> args = new HashMap<>();
		args.putAll(request.getDefinition().getProperties());
		args.putAll(request.getEnvironmentProperties());
		String jmxDomainName = String.format("%s.%s", request.getDefinition().getGroup(), request.getDefinition().getName());
		args.put(JMX_DEFAULT_DOMAIN_KEY, jmxDomainName);
		args.put("endpoints.shutdown.enabled", "true");
		args.put("endpoints.jmx.unique-names", "true");
		try {
			String groupDeploymentId = request.getEnvironmentProperties().get(GROUP_DEPLOYMENT_ID);
			if (groupDeploymentId == null) {
				groupDeploymentId = request.getDefinition().getGroup() + "-" + System.currentTimeMillis();
			}
			Path processDeploymentGroupDir = Paths.get(logPathRoot.toFile().getAbsolutePath(), groupDeploymentId);
			if (!Files.exists(processDeploymentGroupDir)) {
				Files.createDirectory(processDeploymentGroupDir);
				processDeploymentGroupDir.toFile().deleteOnExit();
			}
			Path workDir = Files.createDirectory(Paths.get(processDeploymentGroupDir.toFile().getAbsolutePath(),
					processDeploymentId.toString()));
			if (properties.isDeleteFilesOnExit()) {
				workDir.toFile().deleteOnExit();
			}
			String countProperty = request.getDefinition().getProperties().get(ProcessDeploymentRequest.COUNT_PROPERTY_KEY);
			int count = (countProperty != null) ? Integer.parseInt(countProperty) : 1;
			for (int i = 0; i < count; i++) {
				int port = useDynamicPort ? SocketUtils.findAvailableTcpPort(DEFAULT_SERVER_PORT)
						: Integer.parseInt(request.getDefinition().getProperties().get(SERVER_PORT_KEY));
				if (useDynamicPort) {
					args.put(SERVER_PORT_KEY, String.valueOf(port));
				}
				ProcessBuilder builder = new ProcessBuilder(properties.getJavaCmd(), "-jar", jarPath);
				builder.environment().clear();
				builder.environment().putAll(args);
				Instance instance = new Instance(processDeploymentId, i, builder, workDir, port);
				processes.add(instance);
				if (properties.isDeleteFilesOnExit()) {
					instance.stdout.deleteOnExit();
					instance.stderr.deleteOnExit();
				}
				logger.info("deploying app {} instance {}\n   Logs will be in {}", processDeploymentId, i, workDir);
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Exception trying to deploy " + request, e);
		}
		return processDeploymentId;
	}

	@Override
	public void undeploy(ProcessDeploymentId id) {
		List<Instance> processes = running.get(id);
		if (processes != null) {
			for (Instance instance : processes) {
				if (isAlive(instance.process)) {
					shutdownAndWait(instance);
				}
			}
			running.remove(id);
		}
	}

	@Override
	public ProcessStatus status(ProcessDeploymentId id) {
		List<Instance> instances = running.get(id);
		ProcessStatus.Builder builder = ProcessStatus.of(id);
		if (instances != null) {
			for (Instance instance : instances) {
				builder.with(instance);
			}
		}
		return builder.build();
	}

	private void shutdownAndWait(Instance instance) {
		try {
			restTemplate.postForObject(instance.url + "/shutdown", null, String.class);
			instance.process.waitFor();
		}
		catch (InterruptedException | ResourceAccessException e) {
			instance.process.destroy();
		}
	}

	@PreDestroy
	public void shutdown() throws Exception {
		for (ProcessDeploymentId processDeploymentId : running.keySet()) {
			undeploy(processDeploymentId);
		}
	}

	private static class Instance implements AppInstanceStatus {

		private final ProcessDeploymentId processDeploymentId;

		private final int instanceNumber;

		private final Process process;

		private final File workDir;

		private final File stdout;

		private final File stderr;

		private final URL url;

		private Instance(ProcessDeploymentId processDeploymentId, int instanceNumber, ProcessBuilder builder, Path workDir, int port) throws IOException {
			this.processDeploymentId = processDeploymentId;
			this.instanceNumber = instanceNumber;
			builder.directory(workDir.toFile());
			String workDirPath = workDir.toFile().getAbsolutePath();
			this.stdout = Files.createFile(Paths.get(workDirPath, "stdout_" + instanceNumber + ".log")).toFile();
			this.stderr = Files.createFile(Paths.get(workDirPath, "stderr_" + instanceNumber + ".log")).toFile();
			builder.redirectOutput(this.stdout);
			builder.redirectError(this.stderr);
			builder.environment().put("INSTANCE_INDEX", Integer.toString(instanceNumber));
			this.process = builder.start();
			this.workDir = workDir.toFile();
			this.url = new URL("http", Inet4Address.getLocalHost().getHostAddress(), port, "");
		}

		@Override
		public String getId() {
			return processDeploymentId + "-" + instanceNumber;
		}

		@Override
		public DeploymentState getState() {
			Integer exit = getProcessExitValue(process);
			// TODO: consider using exit code mapper concept from batch
			if (exit != null) {
				if (exit == 0) {
					return DeploymentState.undeployed;
				}
				else {
					return DeploymentState.failed;
				}
			}
			try {
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.connect();
				urlConnection.disconnect();
				return DeploymentState.deployed;
			}
			catch (IOException e) {
				return DeploymentState.deploying;
			}
		}

		public Map<String, String> getAttributes() {
			HashMap<String, String> result = new HashMap<>();
			result.put("working.dir", workDir.getAbsolutePath());
			result.put("stdout", stdout.getAbsolutePath());
			result.put("stderr", stderr.getAbsolutePath());
			result.put("url", url.toString());
			return result;
		}
	}

	/**
	 * Returns the process exit value. We explicitly use Integer instead of int
	 * to indicate that if {@code NULL} is returned, the process is still running.
	 *
	 * @param process the process
	 * @return the process exit value or {@code NULL} if process is still alive
	 */
	private static Integer getProcessExitValue(Process process) {
		try {
			return process.exitValue();
		}
		catch (IllegalThreadStateException e) {
			// process is still alive
			return null;
		}
	}

	// Copy-pasting of JDK8+ isAlive method to retain JDK7 compatibility
	private static boolean isAlive(Process process) {
		try {
			process.exitValue();
			return false;
		}
		catch (IllegalThreadStateException e) {
			return true;
		}
	}
}
