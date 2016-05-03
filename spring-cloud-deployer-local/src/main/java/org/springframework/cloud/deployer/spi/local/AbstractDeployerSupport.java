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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.util.Assert;

/**
 * Base class for app deployer and task launcher providing
 * support for common functionality.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractDeployerSupport {

	private final LocalDeployerProperties properties;

	/**
	 * Instantiates a new abstract deployer support.
	 *
	 * @param properties the local deployer properties
	 */
	public AbstractDeployerSupport(LocalDeployerProperties properties) {
		Assert.notNull(properties, "LocalDeployerProperties must not be null");
		this.properties = properties;
	}

	/**
	 * Gets the local deployer properties.
	 *
	 * @return the local deployer properties
	 */
	final protected LocalDeployerProperties getLocalDeployerProperties() {
		return properties;
	}

	/**
	 * Builds the jar execution command.
	 *
	 * @param jarPath the jar path
	 * @param request the request
	 * @return the string[]
	 */
	protected String[] buildJarExecutionCommand(String jarPath, AppDeploymentRequest request) {
		ArrayList<String> commands = new ArrayList<String>();
		commands.add(properties.getJavaCmd());
		commands.add("-jar");
		commands.add(jarPath);
		commands.addAll(request.getCommandlineArguments());
		return commands.toArray(new String[0]);
	}

	/**
	 * Retain the environment variable strings in the provided set indicated by
	 * {@link LocalDeployerProperties#getEnvVarsToInherit}.
	 * This assumes that the provided set can be modified.
	 *
	 * @param vars set of environment variable strings
	 */
	protected void retainEnvVars(Set<String> vars) {
		String[] patterns = getLocalDeployerProperties().getEnvVarsToInherit();

		for (Iterator<String> iterator = vars.iterator(); iterator.hasNext();) {
			String var = iterator.next();
			boolean retain = false;
			for (String pattern : patterns) {
				if (Pattern.matches(pattern, var)) {
					retain = true;
					break;
				}
			}
			if (!retain) {
				iterator.remove();
			}
		}
	}

	/**
	 * Builds the process builder.
	 *
	 * @param jarPath the jar path
	 * @param request the request
	 * @param args the args
	 * @return the process builder
	 */
	protected ProcessBuilder buildProcessBuilder(String jarPath, AppDeploymentRequest request, Map<String, String> args) {
		Assert.notNull(jarPath, "Jar path must be set");
		Assert.notNull(request, "AppDeploymentRequest must be set");
		Assert.notNull(args, "Args must be set");
		ProcessBuilder builder = new ProcessBuilder(buildJarExecutionCommand(jarPath, request));
		retainEnvVars(builder.environment().keySet());
		builder.environment().putAll(args);
		return builder;
	}
}
