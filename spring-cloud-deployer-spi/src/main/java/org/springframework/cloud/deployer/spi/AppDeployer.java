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
 * {@code AppDeployer} is a SPI defining a runtime environment capable of launching and
 * handling apps. The term 'app' in a context of a {@code AppDeployer} is merely a runtime
 * representation of a container or application wherein app should be executed.
 *
 * SPI itself doesn't expect deployer to keep state of a launched apps meaning it doesn't
 * need to reconstruct all existing {@link AppStatus}s or {@link AppDeploymentId}s needed
 * to resolve a {@link AppStatus}. Responsibility for keeping state of all existing apps
 * lies to whomever is using this SPI. It is unrealistic to expect deployer to be able to
 * store enough information using the underlying infrastructure order to reconstruct full
 * history of a app executions.
 *
 * @author Mark Fisher
 * @author Patrick Peralta
 * @author Marius Bogoevici
 * @author Janne Valkealahti
 *
 * @param <A> the type of artifact metadata
 */
public interface AppDeployer<A extends ArtifactMetadata> {

	/**
	 * Deploy an app using an {@link AppDeploymentRequest}. Returned
	 * {@link AppDeploymentId} is later used with a {{@link #undeploy(AppDeploymentId)}
	 * and a {{@link #status(AppDeploymentId)} to undeploy an app or get its status.
	 *
	 * Implementations may perform this operation asynchronously; therefore a successful
	 * deployment may not be assumed upon return. To determine the status of a deployment,
	 * invoke {@link #status(AppDeploymentId)}.
	 *
	 * @param request the app deployment request
	 * @return the deployment id for the app
	 * @throws IllegalStateException if the app has already been deployed
	 */
	AppDeploymentId deploy(AppDeploymentRequest<A> request);

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
	 * Gets a {@link AppStatus} of an app represented by a {@link AppDeploymentId}.
	 * Return the deployment status of the given {@code AppDeploymentId}.
	 *
	 * @param id the app deployment id
	 * @return the app deployment status
	 */
	AppStatus status(AppDeploymentId id);
}
