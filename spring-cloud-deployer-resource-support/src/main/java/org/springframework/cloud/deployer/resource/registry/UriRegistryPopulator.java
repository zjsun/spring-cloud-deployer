/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.cloud.deployer.resource.registry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility class for populating a {@link UriRegistry} via a
 * {@link Properties} file.
 *
 * @author Patrick Peralta
 * @author Ilayaperumal Gopinathan
 * @author Eric Bottard
 *
 * @deprecated This class is likely to be removed/moved in the near future and should not be considered part of the
 * public API of Spring Cloud Deployer
 */
@Deprecated
public class UriRegistryPopulator {

	private UriRegistryPopulator() {

	}

	private static final Logger logger = LoggerFactory.getLogger(UriRegistryPopulator.class);

	/**
	 * Populate the provided registry with the contents of
	 * the property files indicated by {@code resources}.
	 *
	 * @param overwrite    if {@code true}, overwrites any pre-existing registrations with the same key
	 * @param registry     the registry to populate
	 * @param resources    spring resources pointing to properties files to read
	 * @return the registered URI values in the map with the keys being the property names
	 */
	public static Map<String, URI> populateRegistry(boolean overwrite, UriRegistry registry, Resource... resources) {
		Assert.notEmpty(resources);
		Map<String, URI> registered = new HashMap<>();
		for (Resource resource : resources) {
			Properties properties = new Properties();
			try (InputStream is = resource.getInputStream()) {
				properties.load(is);
				for (String key : properties.stringPropertyNames()) {
					try {
						URI uri = new URI(properties.getProperty(key));
						boolean validUri = true;
						if (uri == null || StringUtils.isEmpty(uri)) {
							logger.warn(String.format("Error when registering '%s': URI is required", key));
							validUri = false;
						}
						if (validUri && !StringUtils.hasText(uri.getScheme())) {
							logger.warn(String.format("Error when registering '%s' with URI %s: URI scheme must be specified", key, uri));
							validUri = false;
						}
						if (validUri && !StringUtils.hasText(uri.getSchemeSpecificPart())) {
							logger.warn(String.format("Error when registering '%s' with URI %s: URI scheme-specific part must be specified", key, uri));
							validUri = false;
						}
						if (!overwrite) {
							try {
								if (registry.find(key) != null) {
									// already exists; move on
									continue;
								}
							}
							catch (IllegalArgumentException e) {
								// this key does not exist; will add
							}
						}
						if (validUri) {
							registry.register(key, uri);
							registered.put(key, uri);
						}
					}
					catch (URISyntaxException e) {
						logger.warn(String.format("'%s' for '%s' is not a properly formed URI",
								properties.getProperty(key), key), e);
					}
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return registered;
	}

}
