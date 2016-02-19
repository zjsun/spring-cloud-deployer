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

package org.springframework.cloud.deployer.spi;

import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * {@code AppDeploymentRequest} contains a runtime representation of a task deployment.
 *
 * Deployment properties are always related to a specific implementation of the SPI and will never
 * be passed into a task itself. For example, a runtime container may allow the definition of
 * various settings for a context where the actual task is executed, such as allowed memory, cpu or
 * simply a way to define colocation like node labeling.
 *
 * For passing properties or parameters into an app, use {{@link AppDefinition#getProperties()}.
 *
 * Representation of an app deployment request. This includes
 * app configuration properties as well as deployment properties.
 *
 * @author Patrick Peralta
 * @author Mark Fisher
 * @author Janne Valkealahti
 */
public class AppDeploymentRequest {

	/**
	 * App definition.
	 */
	private final AppDefinition definition;

	/**
	 * Resource for this app's deployable artifact.
	 */
	private final Resource resource;

	/**
	 * Map of deployment properties for this app.
	 */
	private final Map<String, String> deploymentProperties;

	/**
	 * The deployment property count.
	 */
	public static String DEPLOYMENT_PROPERTY_COUNT = "org.springframework.cloud.deployer.spi.count";

	/**
	 * Construct an {@code AppDeploymentRequest}.
	 *
	 * @param definition app definition
	 * @param resource resource for this app's deployable artifact
	 * @param deploymentProperties map of deployment properties; may be {@code null}
	 */
	public AppDeploymentRequest(AppDefinition definition, Resource resource,
			Map<String, String> deploymentProperties) {
		Assert.notNull(definition, "definition must not be null");
		Assert.notNull(resource, "resource must not be null");
		this.definition = definition;
		this.resource = resource;
		this.deploymentProperties = deploymentProperties == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap(deploymentProperties);
	}

	/**
	 * Construct an {@code AppDeploymentRequest} for one instance and
	 * no deployment properties.
	 *
	 * @param definition app definition
	 * @param resource resource for this app's deployable artifact
	 */
	public AppDeploymentRequest(AppDefinition definition, Resource resource) {
		this(definition, resource, null);
	}

	/**
	 * @see #definition
	 */
	public AppDefinition getDefinition() {
		return definition;
	}

	/**
	 * @see #resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @see #deploymentProperties
	 */
	public Map<String, String> getDeploymentProperties() {
		return deploymentProperties;
	}
}
