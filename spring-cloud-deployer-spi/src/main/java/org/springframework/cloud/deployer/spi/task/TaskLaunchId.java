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

package org.springframework.cloud.deployer.spi.task;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Representation created by a launcher based on a {@link TaskLaunchRequest}.
 * This contains everything the launcher needs to either cancel or check the
 * status of a task.
 *
 * The environment properties in this instance are unmodifiable, as they are
 * only intended to be provided by the launcher implementation that constructs
 * an instance.
 *
 * The contract between {@link TaskLaunchRequest#getEnvironmentProperties()} and
 * {@link TaskLaunchId#getProperties()} is up to the launcher implementation to
 * decide. For example, a launcher may use environment properties passed via the
 * {@link TaskLaunchRequest} as a hint to do something more clever and actual
 * information needed for cancellation or a status check would then be available
 * from the properties in this class.
 *
 * @author Mark Fisher
 * @author Janne Valkealahti
 */
public class TaskLaunchId implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the task.
	 */
	private final String name;

	/**
	 * The launch properties for the task.
	 */
	private final Map<String, String> properties;

	/**
	 * Instantiates a new task launch id.
	 *
	 * @param name the name
	 * @param properties the properties
	 */
	public TaskLaunchId(String name, Map<String, String> properties) {
		Assert.hasText(name);
		Assert.doesNotContain(name, ".");
		this.name = name;
		this.properties = properties == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap(properties);
	}

	/**
	 * Returns the task name.
	 *
	 * @return the task name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the launch properties.
	 *
	 * @return the unmodifiable launch properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TaskLaunchId that = (TaskLaunchId) o;
		return this.name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Return a string representation of this ID.
	 *
	 * @return string representation of this ID
	 */
	@Override
	public String toString() {
		return name;
	}
}
