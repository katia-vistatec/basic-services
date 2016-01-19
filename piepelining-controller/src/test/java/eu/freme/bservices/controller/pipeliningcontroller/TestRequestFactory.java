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
package eu.freme.bservices.controller.pipeliningcontroller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import eu.freme.bservices.controller.pipeliningcontroller.requests.RequestFactory;
import eu.freme.bservices.controller.pipeliningcontroller.requests.SerializedRequest;
import eu.freme.bservices.controller.pipeliningcontroller.serialization.Pipeline_local;
import eu.freme.bservices.controller.pipeliningcontroller.serialization.Serializer;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * @author Gerald Haesendonck
 */
public class TestRequestFactory {

	@Test
	public void testWrongRequestInJSON() {
		// POST = POS!
		String wrongRequest =
				"[\n" +
						"    {\n" +
						"        \"method\": \"POS\",\n" +
						"        \"endpoint\": \"http://api.freme-project.eu/current/e-entity/dbpedia-spotlight/documents\",\n" +
						"        \"parameters\": {\n" +
						"            \"language\": \"en\",\n" +
						"            \"prefix\": \"http://freme-project.eu/\"\n" +
						"        },\n" +
						"        \"headers\": {\n" +
						"            \"content-method\": \"text/plain\",\n" +
						"            \"accept\": \"text/turtle\"\n" +
						"        },\n" +
						"        \"body\": \"This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.\"\n" +
						"    }]";
		try {
			List<SerializedRequest> requests = Serializer.fromJson(wrongRequest);
			fail("A JsonSyntaxException is expected, but all went well (which is wrong!)"); // should throw exception
		} catch (JsonSyntaxException e) {
			// very good!
			System.out.println("Test succeeded. Error msg: " + e.getMessage());
		}
	}

	@Test
	public void testPipelineMemberCheckValid() {
		Gson gson = new Gson();
		List<SerializedRequest> serializedReqs = Collections.singletonList(RequestFactory.createEntitySpotlight("nl"));
		Pipeline_local pipeline = new Pipeline_local(1, 1, "label", "description", false, "Edgar Allan Poe", "PUBLIC", serializedReqs);
		String pipelineStr = gson.toJson(pipeline);
		Pipeline_local pipeline2 = Serializer.templateFromJson(pipelineStr);
		assertEquals(pipeline, pipeline2);
	}

	@Test
	public void testPipelineMemberCheckValidInvalid() {
		Gson gson = new Gson();
		List<SerializedRequest> serializedReqs = Collections.singletonList(RequestFactory.createEntitySpotlight("nl"));
		Pipeline_local pipeline = new Pipeline_local(1, 1, "label", "description", false, "Edgar Allan Poe", "PUBLIC", serializedReqs);
		String pipelineStr = gson.toJson(pipeline);
		String invalidPipelineStr = pipelineStr.replace("label", "labour");
		try {
			Serializer.templateFromJson(invalidPipelineStr);
			fail("A JsonSyntaxException is expected, but all went well (which is wrong!)");
		} catch (JsonSyntaxException e) {
			// very good!
			System.out.println("Test succeeded. Error msg: " + e.getMessage());
		}
	}

//	@Test
//	TODO: not valid anymore. Write new test.
//	public void testPipelineMemberCheckValidInvalidRequest() {
//		Gson gson = new Gson();
//		String serializedReqs = Serializer.toJson(RequestFactory.createEntitySpotlight("nl"));
//		String invalidSerializedReqs = serializedReqs.replace("parameters", "barometers");
//		Pipeline pipeline = new Pipeline(1, "label", "description", false, "Edgar Allan Poe", "PUBLIC", invalidSerializedReqs);
//		String pipelineStr = gson.toJson(pipeline);
//		try {
//			Serializer.templateFromJson(pipelineStr);
//			fail("A JsonSyntaxException is expected, but all went well (which is wrong!)");
//		} catch (JsonSyntaxException e) {
//			// very good!
//			System.out.println("Test succeeded. Error msg: " + e.getMessage());
//		}
//	}
}
