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

import java.util.Map;

import org.springframework.cloud.deployer.core.AppDeploymentId;
import org.springframework.cloud.deployer.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.resolver.ArtifactMetadata;
import org.springframework.cloud.deployer.status.AppStatus;

/**
 * Interface specifying the operations for a runtime environment capable of
 * launching apps via {@link AppDeploymentRequest app deployment requests}.
 *
 * @author Mark Fisher
 * @author Patrick Peralta
 * @author Marius Bogoevici
 */
public interface AppDeployer<A extends ArtifactMetadata> {

	public static final String SERVER_PORT_KEY = "server.port";

	public static final int DEFAULT_SERVER_PORT = 8080;

	public static final String JMX_DEFAULT_DOMAIN_KEY  = "spring.jmx.default-domain";

	public static final String GROUP_DEPLOYMENT_ID = "spring.cloud.group-deployment-id";

	/**
	 * Handle the given {@code AppDeploymentRequest}. Implementations
	 * may perform this operation asynchronously; therefore
	 * a successful deployment may not be assumed upon return.
	 * To determine the status of a deployment, invoke
	 * {@link #status(AppDeploymentId)}.
	 *
	 * @param request request for the app to be deployed
	 * @return the deployment id for the app
	 * @throws IllegalStateException if the app has already been deployed
	 */
	AppDeploymentId deploy(AppDeploymentRequest<A> request);

	/**
	 * Un-deploy the the given {@code AppDeploymenId}. Implementations
	 * may perform this operation asynchronously; therefore
	 * a successful un-deployment may not be assumed upon return.
	 * To determine the status of a deployment, invoke
	 * {@link #status(AppDeploymentId)}.
	 *
	 * @param id unique id for the app to be un-deployed
	 *
	 * @throws IllegalStateException if the app has not been deployed
	 */
	void undeploy(AppDeploymentId id);

	/**
	 * Return the deployment status of the given {@code AppDeploymentId}.
	 *
	 * @param id id for the app this status is for
	 *
	 * @return app deployment status
	 */
	AppStatus status(AppDeploymentId id);

	/**
	 * Return a map of all deployed apps.
	 *
	 * @return map of deployed apps.
	 */
	Map<AppDeploymentId, AppStatus> status();

}
