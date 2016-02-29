/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.deployer.spi.process;

import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * A representation of a request to deploy an app as an indefinitely running process.
 *
 * Environment properties are always related to a specific implementation of the SPI and will never
 * be passed into a task itself. For example, a runtime container may allow the definition of
 * various settings for a context where the actual task is executed, such as allowed memory, cpu or
 * simply a way to define colocation like node labeling.
 *
 * For passing properties or parameters into an app, use {@link ProcessDefinition#getProperties()}.
 *
 * @author Patrick Peralta
 * @author Mark Fisher
 * @author Janne Valkealahti
 */
public class ProcessDeploymentRequest {

	/**
	 * Process definition.
	 */
	private final ProcessDefinition definition;

	/**
	 * Resource for the deployable artifact of the underlying app.
	 */
	private final Resource resource;

	/**
	 * Map of environment properties for the process.
	 */
	private final Map<String, String> environmentProperties;

	/**
	 * The environment property for the count (number of app instances).
	 */
	public static String COUNT_PROPERTY_KEY = "org.springframework.cloud.deployer.spi.count";

	/**
	 * Construct a {@code ProcessDeploymentRequest}.
	 *
	 * @param definition process definition
	 * @param resource resource for the underlying app's deployable artifact
	 * @param environmentProperties map of environment properties; may be {@code null}
	 */
	public ProcessDeploymentRequest(ProcessDefinition definition, Resource resource,
			Map<String, String> environmentProperties) {
		Assert.notNull(definition, "definition must not be null");
		Assert.notNull(resource, "resource must not be null");
		this.definition = definition;
		this.resource = resource;
		this.environmentProperties = environmentProperties == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap(environmentProperties);
	}

	/**
	 * Construct a {@code ProcessDeploymentRequest} for one instance and
	 * no environment properties.
	 *
	 * @param definition process definition
	 * @param resource resource for this app's deployable artifact
	 */
	public ProcessDeploymentRequest(ProcessDefinition definition, Resource resource) {
		this(definition, resource, null);
	}

	/**
	 * @see #definition
	 */
	public ProcessDefinition getDefinition() {
		return definition;
	}

	/**
	 * @see #resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @see #environmentProperties
	 */
	public Map<String, String> getEnvironmentProperties() {
		return environmentProperties;
	}
}
