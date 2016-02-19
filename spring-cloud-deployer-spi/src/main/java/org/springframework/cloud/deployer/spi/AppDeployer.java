/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.deployer.spi;

import org.springframework.cloud.deployer.spi.status.AppStatus;

/**
 * {@code AppDeployer} is a SPI defining a runtime environment capable of launching and managing
 * the lifecycle of apps. The term 'app' in the context of an {@code AppDeployer} is merely a
 * runtime representation of a container or application wherein the app should be executed.
 *
 * The SPI itself doesn't expect the deployer to keep state of launched apps, meaning it doesn't
 * need to reconstruct all existing {@link AppStatus}es or {@link AppDeploymentId}s needed to
 * resolve an {@link AppStatus}. The responsibility for keeping track of the state of all
 * existing apps lies to whomever is using this SPI. It is unrealistic to expect the deployer to be
 * able to store enough information using the underlying infrastructure to reconstruct the full
 * history of app executions.
 *
 * @author Mark Fisher
 * @author Patrick Peralta
 * @author Marius Bogoevici
 * @author Janne Valkealahti
 */
public interface AppDeployer {

	/**
	 * Deploy an app using an {@link AppDeploymentRequest}. The returned
	 * {@link AppDeploymentId} is later used with an {@link #undeploy(AppDeploymentId)}
	 * and a {@link #status(AppDeploymentId)} to undeploy an app or get its status.
	 *
	 * Implementations may perform this operation asynchronously; therefore a successful
	 * deployment may not be assumed upon return. To determine the status of a deployment,
	 * invoke {@link #status(AppDeploymentId)}.
	 *
	 * @param request the app deployment request
	 * @return the deployment id for the app
	 * @throws IllegalStateException if the app has already been deployed
	 */
	AppDeploymentId deploy(AppDeploymentRequest request);

	/**
	 * Un-deploy an app using an {@link AppDeploymentRequest}. Implementations may
	 * perform this operation asynchronously; therefore a successful
	 * un-deployment may not be assumed upon return. To determine the status of
	 * a deployment, invoke {@link #status(AppDeploymentId)}.
	 *
	 * @param id the app deployment id
	 * @throws IllegalStateException if the app has not been deployed
	 */
	void undeploy(AppDeploymentId id);

	/**
	 * Gets the {@link AppStatus} for an app represented by an {@link AppDeploymentId}.
	 * Return the deployment status of the given {@code AppDeploymentId}.
	 *
	 * @param id the app deployment id
	 * @return the app deployment status
	 */
	AppStatus status(AppDeploymentId id);
}
