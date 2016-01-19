/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.bservices.controller.pipeliningcontroller.serialization;

import eu.freme.bservices.controller.pipeliningcontroller.requests.SerializedRequest;

import java.util.List;

/**
 * <p>An object representing a pipeline template</p>
 *
 * @author Gerald Haesendonck
 */
public class Pipeline_local {
	private long id;
	private long creationTime;
	private String label;
	private String description;
	private boolean persist;
	private String visibility;
	private String owner;
	private List<SerializedRequest> serializedRequests;

	@SuppressWarnings("unused")
	public Pipeline_local(final String label, final String description, final List<SerializedRequest> serializedRequests) {
		this(-1, -1, label, description, false, null, null, serializedRequests);
	}

	public Pipeline_local(long id,
						  long creationTime,
						  final String label,
						  final String description,
						  boolean persist,
						  final String owner,
						  final String visibility,
						  final List<SerializedRequest> serializedRequests) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.persist = persist;
		this.owner = owner;
		this.visibility = visibility;
		this.serializedRequests = serializedRequests;
		this.creationTime = creationTime;
	}

	@SuppressWarnings("unused")
	public long getId() {
		return id;
	}

	@SuppressWarnings("unused")
	public String getLabel() {
		return label;
	}

	@SuppressWarnings("unused")
	public String getDescription() {
		return description;
	}

	@SuppressWarnings("unused")
	public boolean isPersist() {
		return persist;
	}

	@SuppressWarnings("unused")
	public String getVisibility() {
		return visibility;
	}

	@SuppressWarnings("unused")
	public String getOwner() {
		return owner;
	}

	@SuppressWarnings("unused")
	public List<SerializedRequest> getSerializedRequests() {
		return serializedRequests;
	}

	@SuppressWarnings("unused")
	public void setSerializedRequests(List<SerializedRequest> serializedRequests) {
		this.serializedRequests = serializedRequests;
	}

	@SuppressWarnings("unused")
	public void setLabel(String label) {
		this.label = label;
	}

	@SuppressWarnings("unused")
	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("unused")
	public void setPersist(boolean persist) {
		this.persist = persist;
	}

	@SuppressWarnings("unused")
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	@SuppressWarnings("unused")
	public void setOwner(String owner) {
		this.owner = owner;
	}

	@SuppressWarnings("unused")
	public void setId(long id) {
		this.id = id;
	}

	@SuppressWarnings("unused")
	public long getCreationTime() {
		return creationTime;
	}

	@SuppressWarnings("unused")
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	@SuppressWarnings("unused")
	public String isValid() {
		if (label == null) {
			return "No label given.";
		}
		if (description == null) {
			return "No description given.";
		}
		if (serializedRequests == null) {
			return "No requests given.";
		}
		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Pipeline_local pipeline = (Pipeline_local) o;

		if (id != pipeline.id) return false;
		if (creationTime != pipeline.creationTime) return false;
		if (persist != pipeline.persist) return false;
		if (!label.equals(pipeline.label)) return false;
		if (!description.equals(pipeline.description)) return false;
		if (visibility != null ? !visibility.equals(pipeline.visibility) : pipeline.visibility != null) return false;
		if (owner != null ? !owner.equals(pipeline.owner) : pipeline.owner != null) return false;
		return serializedRequests.equals(pipeline.serializedRequests);

	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (int) (creationTime ^ (creationTime >>> 32));
		result = 31 * result + label.hashCode();
		result = 31 * result + description.hashCode();
		result = 31 * result + (persist ? 1 : 0);
		result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
		result = 31 * result + (owner != null ? owner.hashCode() : 0);
		result = 31 * result + serializedRequests.hashCode();
		return result;
	}
}
