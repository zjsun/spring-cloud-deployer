/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.deployer.core;

import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Representation of an app deployment request. This includes
 * app configuration properties as well as deployment properties.
 *
 * @author Patrick Peralta
 * @author Mark Fisher
 */
public class AppDeploymentRequest {

	/**
	 * App definition.
	 */
	private final AppDefinition definition;

	/**
	 * Coordinates for the app's jar file.
	 */
	private final ArtifactCoordinates coordinates;

	/**
	 * Map of deployment properties for this app.
	 */
	private final Map<String, String> deploymentProperties;

	/**
	 * Number of app instances to launch.
	 */
	private final int count;

	/**
	 * Construct an {@code AppDeploymentRequest}.
	 *
	 * @param definition app definition
	 * @param coordinates maven coordinates for the app's jar file
	 * @param deploymentProperties map of deployment properties; may be {@code null}
	 */
	public AppDeploymentRequest(AppDefinition definition, ArtifactCoordinates coordinates,
			Map<String, String> deploymentProperties) {
		Assert.notNull(definition, "definition must not be null");
		Assert.notNull(coordinates, "coordinates must not be null");
		this.definition = definition;
		this.coordinates = coordinates;
		this.deploymentProperties = deploymentProperties == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap(deploymentProperties);
		this.count = this.deploymentProperties.containsKey("count")
				? Integer.parseInt(this.deploymentProperties.get("count"))
				: 1;
	}

	/**
	 * Construct an {@code AppDeploymentRequest} for one instance and
	 * no deployment properties.
	 *
	 * @param definition app definition
	 * @param coordinates coordinates for the app jar file
	 */
	public AppDeploymentRequest(AppDefinition definition, ArtifactCoordinates coordinates) {
		this(definition, coordinates, null);
	}

	/**
	 * @see #definition
	 */
	public AppDefinition getDefinition() {
		return definition;
	}

	/**
	 * @see #coordinates
	 */
	public ArtifactCoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * @see #count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @see #deploymentProperties
	 */
	public Map<String, String> getDeploymentProperties() {
		return deploymentProperties;
	}
}
