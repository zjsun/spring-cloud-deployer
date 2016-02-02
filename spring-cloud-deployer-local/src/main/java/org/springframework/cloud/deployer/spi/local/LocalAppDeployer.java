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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.cloud.deployer.core.AppDeploymentId;
import org.springframework.cloud.deployer.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.resolver.ArtifactResolver;
import org.springframework.cloud.deployer.resolver.maven.MavenCoordinates;
import org.springframework.cloud.deployer.spi.AppDeployer;
import org.springframework.cloud.deployer.status.AppStatus;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * @author Mark Fisher
 */
public class LocalAppDeployer implements AppDeployer<MavenCoordinates> {

	private final ArtifactResolver<MavenCoordinates> resolver;

	public LocalAppDeployer(ArtifactResolver<MavenCoordinates> resolver) {
		Assert.notNull(resolver, "ArtifactResolver must not be null");
		this.resolver = resolver;
	}

	@Override
	public AppDeploymentId deploy(AppDeploymentRequest<MavenCoordinates> request) {
		Resource resource = this.resolver.resolve(request.getArtifactMetadata());
		try {
			JarFileArchive jarFileArchive = new JarFileArchive(resource.getFile());
			CustomJarLauncher jarLauncher = new CustomJarLauncher(jarFileArchive);
			jarLauncher.launch(generateArgs(request));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return AppDeploymentId.fromAppDefinition(request.getDefinition());
	}

	@Override
	public void undeploy(AppDeploymentId id) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public AppStatus status(AppDeploymentId id) {
		return AppStatus.of(id).build();
	}

	@Override
	public Map<AppDeploymentId, AppStatus> status() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	private String[] generateArgs(AppDeploymentRequest<MavenCoordinates> request) {
		Map<String, String> appProperties = request.getDefinition().getProperties();
		ArrayList<String> args = new ArrayList<>(appProperties.size());
		for (Map.Entry<String, String> entry : appProperties.entrySet()) {
			args.add(String.format("--%s=%s", entry.getKey(), entry.getValue()));
		}
		return args.toArray(new String[args.size()]);
	}

	/**
	 * Overrides to enable access.
	 */
	private static class CustomJarLauncher extends JarLauncher {

		private CustomJarLauncher(Archive archive) {
			super(archive);
		}

		@Override
		protected void launch(String[] args) {
			super.launch(args);
		}
	}
}
