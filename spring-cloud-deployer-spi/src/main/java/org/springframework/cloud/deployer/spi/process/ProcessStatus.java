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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Status of a process which is initially constructed from an
 * {@link org.springframework.cloud.deployer.spi.core.AppDeploymentRequest} and
 * runtime deployment properties by a deployer during deployment. This status is
 * composed of an aggregate of all individual app instance deployments for the
 * process' underlying app.
 * <p>
 * Consumers of the SPI obtain the process status via
 * {@link org.springframework.cloud.deployer.spi.process.ProcessDeployer#status(String)}
 * , whereas SPI implementations create instances of this class via
 * {@link ProcessStatus.Builder}.
 *
 * @author Patrick Peralta
 * @author Mark Fisher
 * @see AppInstanceStatus
 */
public class ProcessStatus {

	/**
	 * The id of the process this status is for.
	 */
	private final String processDeploymentId;

	/**
	 * Map of {@link AppInstanceStatus} keyed by a unique identifier
	 * for each app deployment instance.
	 */
	private final Map<String, AppInstanceStatus> instances = new HashMap<String, AppInstanceStatus>();

	/**
	 * Construct a new {@code ProcessStatus}.
	 *
	 * @param processDeploymentId id of the process this status is for
	 */
	protected ProcessStatus(String processDeploymentId) {
		this.processDeploymentId = processDeploymentId;
	}

	/**
	 * Return the process deployment id for the process.
	 *
	 * @return process deployment id
	 */
	public String getProcessDeploymentId() {
		return processDeploymentId;
	}

	/**
	 * Return the deployment state for the the process. If the descriptor
	 * indicates multiple instances, this state represents an aggregate
	 * of all individual app instances.
	 *
	 * @return deployment state for the process
	 */
	public DeploymentState getState() {
		Set<DeploymentState> states = new HashSet<>();
		for (Map.Entry<String, AppInstanceStatus> entry : instances.entrySet()) {
			states.add(entry.getValue().getState());
		}

		if (states.size() == 0) {
			return DeploymentState.unknown;
		}
		if (states.size() == 1) {
			return states.iterator().next();
		}
		if (states.contains(DeploymentState.error)) {
			return DeploymentState.error;
		}
		if (states.contains(DeploymentState.deploying)) {
			return DeploymentState.deploying;
		}
		if (states.contains(DeploymentState.deployed) || states.contains(DeploymentState.partial)) {
			return DeploymentState.partial;
		}
		if (states.contains(DeploymentState.failed)) {
			return DeploymentState.failed;
		}

		// reaching here is unlikely; it would require some
		// combination of unknown, undeployed, complete
		return DeploymentState.partial;
	}

	public String toString() {
		return this.getState().name();
	}

	/**
	 * Return a map of {@code AppInstanceStatus} keyed by a unique identifier
	 * for each app deployment instance.
	 * @return map of {@code AppInstanceStatus}
	 */
	public Map<String, AppInstanceStatus> getInstances() {
		return Collections.unmodifiableMap(this.instances);
	}

	private void addInstance(String id, AppInstanceStatus status) {
		this.instances.put(id, status);
	}

	/**
	 * Return a {@code Builder} for {@code ProcessStatus}.
	 * @param key of the process this status is for
	 * @return {@code Builder} for {@code ProcessStatus}
	 */
	public static Builder of(String id) {
		return new Builder(id);
	}

	/**
	 * Utility class constructing an instance of a {@link ProcessStatus}
	 * using a builder pattern.
	 */
	public static class Builder {

		private final ProcessStatus status;

		/**
		 * Instantiates a new builder.
		 *
		 * @param key the process deployment id
		 */
		private Builder(String id) {
			this.status = new ProcessStatus(id);
		}

		/**
		 * Add an instance of {@code AppInstanceStatus} to build the status for
		 * the process. This will be invoked once per individual app instance.
		 *
		 * @param instance status of individual app deployment
		 * @return this {@code Builder}
		 */
		public Builder with(AppInstanceStatus instance) {
			status.addInstance(instance.getId(), instance);
			return this;
		}

		/**
		 * Return a new instance of {@code ProcessStatus} based on
		 * the provided individual app instances via
		 * {@link #with(AppInstanceStatus)}.
		 * @return new instance of {@code ProcessStatus}
		 */
		public ProcessStatus build() {
			return status;
		}
	}
}
