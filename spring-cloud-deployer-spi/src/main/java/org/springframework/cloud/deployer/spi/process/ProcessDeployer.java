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

package org.springframework.cloud.deployer.spi.process;

import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;

/**
 * SPI defining a runtime environment capable of launching and managing the
 * lifecycle of apps as indefinitely running processes. The term 'process' in
 * the context of an {@code ProcessDeployer} is merely a runtime representation
 * of a container or application wherein the underlying app should be executed.
 *
 * The SPI itself doesn't expect the deployer to keep state of launched
 * processes, meaning it doesn't need to reconstruct all existing
 * {@link ProcessStatus}es or the deployment IDs needed to resolve a
 * {@link ProcessStatus}. The use of the SPI is responsible for keeping track of
 * the state of all existing processes.
 *
 * @author Mark Fisher
 * @author Patrick Peralta
 * @author Marius Bogoevici
 * @author Janne Valkealahti
 */
public interface ProcessDeployer {

	/**
	 * The environment property for the count (number of app instances).
	 */
	public static String COUNT_PROPERTY_KEY = "spring.cloud.deployer.count";

	/**
	 * The environment property for the group to which an app belongs.
	 */
	public static String GROUP_PROPERTY_KEY = "spring.cloud.deployer.group";

	/**
	 * Deploy a process using an {@link AppDeploymentRequest}. The returned id
	 * is later used with {@link #undeploy(String)} or {@link #status(String)}
	 * to undeploy an app or check its status, respectively.
	 *
	 * Implementations may perform this operation asynchronously; therefore a
	 * successful deployment may not be assumed upon return. To determine the
	 * status of a deployment, invoke {@link #status(String)}.
	 *
	 * @param request the app deployment request
	 * @return the deployment id for the process
	 * @throws IllegalStateException if the process has already been deployed
	 */
	String deploy(AppDeploymentRequest request);

	/**
	 * Un-deploy a process using its deployment id. Implementations
	 * may perform this operation asynchronously; therefore a successful
	 * un-deployment may not be assumed upon return. To determine the status of
	 * a deployment, invoke {@link #status(String)}.
	 *
	 * @param id the process deployment id, as returned by {@link #deploy(AppDeploymentRequest)}
	 * @throws IllegalStateException if the process has not been deployed
	 */
	void undeploy(String id);

	/**
	 * Return the {@link ProcessStatus} for a process represented by a deployment id.
	 *
	 * @param id the process deployment id, as returned by {@link #deploy(AppDeploymentRequest)}
	 * @return the processs deployment status
	 */
	ProcessStatus status(String id);
}
