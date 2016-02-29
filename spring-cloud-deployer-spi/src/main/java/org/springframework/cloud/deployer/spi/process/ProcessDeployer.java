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

/**
 * SPI defining a runtime environment capable of launching and managing the
 * lifecycle of apps as indefinitely running processes. The term 'process' in
 * the context of an {@code ProcessDeployer} is merely a runtime representation
 * of a container or application wherein the underlying app should be executed.
 *
 * The SPI itself doesn't expect the deployer to keep state of launched
 * processes, meaning it doesn't need to reconstruct all existing
 * {@link ProcessStatus}es or {@link ProcessDeploymentId}s needed to resolve a
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
	 * Deploy a process using an {@link ProcessDeploymentRequest}. The returned
	 * {@link ProcessDeploymentId} is later used with
	 * {@link #undeploy(ProcessDeploymentId)} or
	 * {@link #status(ProcessDeploymentId)} to undeploy an app or check its
	 * status, respectively.
	 *
	 * Implementations may perform this operation asynchronously; therefore a
	 * successful deployment may not be assumed upon return. To determine the
	 * status of a deployment, invoke {@link #status(ProcessDeploymentId)}.
	 *
	 * @param request
	 *            the process deployment request
	 * @return the deployment id for the process
	 * @throws IllegalStateException
	 *             if the process has already been deployed
	 */
	ProcessDeploymentId deploy(ProcessDeploymentRequest request);

	/**
	 * Un-deploy a process using a {@link ProcessDeploymentId}. Implementations
	 * may perform this operation asynchronously; therefore a successful
	 * un-deployment may not be assumed upon return. To determine the status of
	 * a deployment, invoke {@link #status(ProcessDeploymentId)}.
	 *
	 * @param id
	 *            the process deployment id
	 * @throws IllegalStateException
	 *             if the process has not been deployed
	 */
	void undeploy(ProcessDeploymentId id);

	/**
	 * Return the {@link ProcessStatus} for a process represented by a {@link ProcessDeploymentId}.
	 *
	 * @param id the process deployment id
	 * @return the processs deployment status
	 */
	ProcessStatus status(ProcessDeploymentId id);
}
