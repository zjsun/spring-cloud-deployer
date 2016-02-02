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

package org.springframework.cloud.deployer.core;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Unique identifier for an {@link AppDeploymentRequest}.
 * Methods {@link #toString()} and {@link #parse(String)} can be used to convert
 * an ID to a string and a string to an ID, respectively. The ID string may be
 * used to uniquely identify an app in a database or execution environment.
 *
 * @author Patrick Peralta
 * @author Mark Fisher
 */
public class AppDeploymentId implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the associated group this app belongs to.
	 */
	private final String group;

	/**
	 * The name provided to uniquely identify the app within a group.
	 */
	private final String name;

	/**
	 * Construct an {@code AppDeploymentId}.
	 *
	 * @param group name of group this app belongs to
	 * @param name name to uniquely identify this app in its group
	 */
	public AppDeploymentId(String group, String name) {
		Assert.hasText(group);
		Assert.hasText(name);
		Assert.doesNotContain(group, ".");
		Assert.doesNotContain(name, ".");
		this.group = group;
		this.name = name;
	}

	/**
	 * @see #group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @see #name
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AppDeploymentId that = (AppDeploymentId) o;
		return this.group.equals(that.group)
				&& this.name.equals(that.name);
	}

	@Override
	public int hashCode() {
		int result = group.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	/**
	 * Return a string containing the ID fields separated by
	 * periods. This string may be used as a key in a database
	 * to uniquely identify an app.
	 *
	 * @return string representation of this ID
	 */
	@Override
	public String toString() {
		if (group == null) {
			return name;
		}
		return String.format("%s.%s", this.group, this.name);
	}

	/**
	 * Parse the given string and return an {@code AppDeploymentId} based
	 * on the string contents.
	 *
	 * @param id id containing the fields for an {@code AppDeploymentId}
	 * @return a new {@code AppDeploymentId} based on the provided string
	 * @throws IllegalArgumentException if a null or an invalid id is provided
	 */
	public static AppDeploymentId parse(String id) {
		Assert.notNull(id, "id must not be null");
		String[] fields = id.split("\\.");
		if (fields.length == 1) {
			return new AppDeploymentId(null, fields[0]);
		}
		if (fields.length == 2) {
			return new AppDeploymentId(fields[0], fields[1]);
		}
		throw new IllegalArgumentException(String.format("invalid format for id '%s'", id));
	}

	/**
	 * Return an {@code AppDeploymentId} based on the provided {@link AppDefinition}.
	 *
	 * @param definition app definition to generate an ID for
	 * @return new {@code AppDeploymentId} for the provided app definition
	 */
	public static AppDeploymentId fromAppDefinition(AppDefinition definition) {
		return new AppDeploymentId(definition.getGroup(), definition.getName());
	}

}
