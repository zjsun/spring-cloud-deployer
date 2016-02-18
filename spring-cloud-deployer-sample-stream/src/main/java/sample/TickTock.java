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

package sample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.deployer.resolver.maven.MavenArtifactResolver;
import org.springframework.cloud.deployer.resolver.maven.MavenCoordinates;
import org.springframework.cloud.deployer.spi.AppDefinition;
import org.springframework.cloud.deployer.spi.AppDeploymentId;
import org.springframework.cloud.deployer.spi.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.local.LocalAppDeployer;

/**
 * @author Mark Fisher
 */
public class TickTock {

	private static final File LOCAL_REPO = new File(System.getProperty("user.home")
			+ File.separator + ".m2" + File.separator + "repository");

	public static void main(String[] args) throws InterruptedException {
		MavenArtifactResolver resolver = new MavenArtifactResolver(LOCAL_REPO, null);
		LocalAppDeployer deployer = new LocalAppDeployer(resolver);
		AppDeploymentId logId = deployer.deploy(createAppDeploymentRequest("log-sink", "ticktock"));
		AppDeploymentId timeId = deployer.deploy(createAppDeploymentRequest("time-source", "ticktock"));
		for (int i = 0; i < 12; i++) {
			Thread.sleep(5 * 1000);
			System.out.println("time: " + deployer.status(timeId));
			System.out.println("log:  " + deployer.status(logId));
		}
		deployer.undeploy(timeId);
		deployer.undeploy(logId);
	}

	private static AppDeploymentRequest<MavenCoordinates> createAppDeploymentRequest(String app, String stream) {
		MavenCoordinates coordinates = new MavenCoordinates.Builder()
				.setArtifactId(app)
				.setGroupId("org.springframework.cloud.stream.module")
				.setVersion("1.0.0.BUILD-SNAPSHOT")
				.setExtension("jar")
				.setClassifier("exec")
				.build();
		Map<String, String> properties = new HashMap<>();
		properties.put("server.port", "0");
		if (app.endsWith("-source")) {
			properties.put("spring.cloud.stream.bindings.output.destination", stream);
		}
		else {
			properties.put("spring.cloud.stream.bindings.input.destination", stream);
			properties.put("spring.cloud.stream.bindings.input.group", "default");
		}
		AppDefinition definition = new AppDefinition(app, stream, properties);
		AppDeploymentRequest<MavenCoordinates> request =
				new AppDeploymentRequest<MavenCoordinates>(definition, coordinates);
		return request;
	}
}
