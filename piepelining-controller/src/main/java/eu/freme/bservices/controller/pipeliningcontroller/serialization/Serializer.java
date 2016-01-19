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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import eu.freme.bservices.controller.pipeliningcontroller.core.WrappedPipelineResponse;
import eu.freme.bservices.controller.pipeliningcontroller.requests.SerializedRequest;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Gerald Haesendonck
 */
public class Serializer {
	private final static Gson gson = new Gson();
	//private final static Gson gson_pretty = new GsonBuilder().setPrettyPrinting().create();

	private static Set<String> requestFieldNames = new HashSet<>(5, 1);
	static {
		Class<SerializedRequest> src = SerializedRequest.class;
		for (Field field : src.getDeclaredFields()) {
			requestFieldNames.add(field.getName());
		}
	}

	private static Set<String> pipelineFieldNames = new HashSet<>(7, 1);
	static {
		Class<Pipeline_local> pc = Pipeline_local.class;
		for (Field field : pc.getDeclaredFields()) {
			pipelineFieldNames.add(field.getName());
		}
	}

	/**
	 * Converts requests to JSON in the given order.
	 * @param requests The requests to convert to JSON.
	 * @return		   A JSON string representing serialized requests, which can be sent to the Pipelines API.
	 */
	@SuppressWarnings("unused")
	public static String toJson(final SerializedRequest... requests) {
		return toJson(Arrays.asList(requests));
	}

	/**
	 * Converts a list of requests to JSON.
	 * @param requests	The list of requests to convert to JSON.
	 * @return			A JSON string representing serialized requests, which can be sent to the Pipelines API.
	 */
	public static String toJson(final List<SerializedRequest> requests) {
		return gson.toJson(requests);
	}

	/**
	 * Converts a WrappedPipelineResponse to JSON.
	 * @param response	The WrappedPipelineResponse to serialize.
	 * @return			A JSON string representing the wrapped response.
	 */
	@SuppressWarnings("unused")
	public static String toJson(final WrappedPipelineResponse response) {
		return gson.toJson(response);
	}

	/**
	 * Converts a JSON string to a list of requests.
	 * @param serializedRequests	The JSON string of requests to convert.
	 * @return						The list of requests represented by the JSON string.
	 * @throws JsonSyntaxException    Something is wrong with the JSON syntax.
	 */
	@SuppressWarnings("unused")
	public static List<SerializedRequest> fromJson(final String serializedRequests) {
		checkOnRequestsMembers(serializedRequests);
		SerializedRequest[] requests = gson.fromJson(serializedRequests, SerializedRequest[].class);
		for (int reqNr = 0; reqNr < requests.length; reqNr++) {
			String invalid = requests[reqNr].isValid();
			if (!invalid.isEmpty()) {
				throw new JsonSyntaxException("Request " + (reqNr + 1) + ": " + invalid);
			}
		}
		return Arrays.asList(requests);
	}

	/**
	 * Converts a pipeline template to a JSON string.
	 * @param pipeline	The pipeline template to convert.
	 * @return			A JSON string representing the pipeline template. This is id, if it is persistent, the owner name,
	 * 					the visibility (PUBLIC or PRIVATE) and the serialized requests.
	 * @throws JsonSyntaxException	Something is wrong with the JSON syntax.	.
	 */
	@SuppressWarnings("unused")
	public static String toJson(final Pipeline_local pipeline) {
		return gson.toJson(pipeline);
	}

	/**
	 * Converts a pipeline template resource to a JSON string.
	 * @param pipeline	The pipeline template resource to convert.
	 * @return			A JSON string representing the pipeline template. This is id, if it is persistent, the owner name,
	 * 					the visibility (PUBLIC or PRIVATE) and the serialized requests.
	 * @throws JsonSyntaxException	Something is wrong with the JSON syntax.	.
	 */
	@SuppressWarnings("unused")
	public static String toJson(final eu.freme.common.persistence.model.Pipeline pipeline) {
		List<SerializedRequest> serializedRequests = fromJson(pipeline.getSerializedRequests());
		Pipeline_local pipelineObj = new Pipeline_local(
				pipeline.getId(),
				pipeline.getCreationTime(),
				pipeline.getLabel(),
				pipeline.getDescription(),
				pipeline.isPersistent(),
				pipeline.getOwner().getName(),
				pipeline.getVisibility().name(),
				serializedRequests);
		return gson.toJson(pipelineObj);
	}

	/**
	 * Converts a JSON string into an object containing pipeline template information.
	 * @param pipelineTemplate 	A JSON string representing the pipeline template.
	 * @return  			    The pipeline template info object.
	 * @throws JsonSyntaxException	Something is wrong with the JSON syntax.	.
	 */
	@SuppressWarnings("unused")
	public static Pipeline_local templateFromJson(final String pipelineTemplate) {
		checkOnPipelineMembers(pipelineTemplate);
		Pipeline_local pipeline = gson.fromJson(pipelineTemplate, Pipeline_local.class);
		checkOnRequestsMembers(toJson(pipeline.getSerializedRequests()));
		String invalid = pipeline.isValid();
		if (!invalid.isEmpty()) {
			throw new JsonSyntaxException(invalid);
		}
		return gson.fromJson(pipelineTemplate, Pipeline_local.class);
	}

	/**
	 * Converts a list of pipeline templates to a JSON string.
	 * @param pipelines	The pipeline templates to convert.
	 * @return			A JSON string representing the pipeline templates. This is id, if it is persistent, the owner name,
	 * 					the visibility (PUBLIC or PRIVATE) and the serialized requests per pipeline.
	 * @throws JsonSyntaxException	Something is wrong with the JSON syntax.	.
	 */
	@SuppressWarnings("unused")
	public static String templatesToJson(final List<eu.freme.common.persistence.model.Pipeline> pipelines) {
		List<Pipeline_local> pipelineInfos = new ArrayList<>();
		for (eu.freme.common.persistence.model.Pipeline pipeline : pipelines) {
			List<SerializedRequest> serializedRequests = fromJson(pipeline.getSerializedRequests());
			Pipeline_local pipelineObj = new Pipeline_local(
					pipeline.getId(),
					pipeline.getCreationTime(),
					pipeline.getLabel(),
					pipeline.getDescription(),
					pipeline.isPersistent(),
					pipeline.getOwner().getName(),
					pipeline.getVisibility().name(),
					serializedRequests);
			pipelineInfos.add(pipelineObj);
		}
		return gson.toJson(pipelineInfos);
	}

	/**
	 * Converts a JSON string into an object containing pipeline templates information.
	 * @param pipelineTemplates 	A JSON string representing the pipeline templates.
	 * @return  			    	The pipeline template info objects.
	 */
	@SuppressWarnings("unused")
	public static List<Pipeline_local> templatesFromJson(final String pipelineTemplates) {
		Pipeline_local[] requests = gson.fromJson(pipelineTemplates, Pipeline_local[].class);
		return Arrays.asList(requests);
	}

	/**
	 * Checks if all fields in the JSON string are valid field names of the {@link SerializedRequest} class. Throws an
	 * exception if not valid.
	 * @param serializedRequests	The JSON string to check; it should represent a list of {@link SerializedRequest} objects.
	 * @throws JsonSyntaxException	A field is not recognized.
	 */
	private static void checkOnRequestsMembers(final String serializedRequests) {
		Object serReqObj = gson.fromJson(serializedRequests, Object.class);
		if (! (serReqObj instanceof ArrayList)) {
			throw new JsonSyntaxException("Expected an array of requests");
		}
		ArrayList<LinkedTreeMap> requests = (ArrayList<LinkedTreeMap>)serReqObj;
		for (int reqNr = 0; reqNr < requests.size(); reqNr++) {
			LinkedTreeMap map = requests.get(reqNr);
			for (Object o : map.keySet()) {
				String fieldName = (String)o;
				if (!requestFieldNames.contains(fieldName)) {
					throw new JsonSyntaxException("request " + (reqNr + 1) + ": field \"" + fieldName + "\" not known.");
				}
			}
		}
	}

	private static void checkOnPipelineMembers(final String pipeline) {
		LinkedTreeMap<String, String> pipelineObject = (LinkedTreeMap<String, String>)gson.fromJson(pipeline, Object.class);
		for (String fieldName : pipelineObject.keySet()) {
			if (!pipelineFieldNames.contains(fieldName)) {
				throw new JsonSyntaxException("field \"" + fieldName + "\" not known.");
			}
		}
	}
}
