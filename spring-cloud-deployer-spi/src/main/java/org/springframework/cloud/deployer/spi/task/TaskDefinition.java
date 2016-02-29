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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * {@code TaskDefinition} contains information about a task and is immutable.
 * The deployer's only responsibility is to pass the properties into a runtime
 * environment such that they are available for the launched task.
 *
 * @author Mark Fisher
 * @author Janne Valkealahti
 */
public class TaskDefinition {

	/**
	 * Name of task.
	 */
	private final String name;

	/**
	 * Properties for this task.
	 */
	private final Map<String, String> properties;

	/**
	 * Construct a {@code TaskDefinition}.
	 *
	 * @param name
	 *            name of task
	 * @param properties
	 *            task properties; may be {@code null}
	 */
	public TaskDefinition(String name, Map<String, String> properties) {
		Assert.notNull(name, "name must not be null");
		this.name = name;
		this.properties = properties == null ? Collections.<String, String> emptyMap()
				: Collections.unmodifiableMap(new HashMap<String, String>(properties));
	}

	/**
	 * Return the name of this task.
	 *
	 * @return the task name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the properties that should be passed into a launched task.
	 *
	 * @return the unmodifiable map of task properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("name", this.name)
				.append("properties", this.properties).toString();
	}
}
