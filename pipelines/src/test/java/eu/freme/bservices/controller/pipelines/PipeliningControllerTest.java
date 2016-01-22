package eu.freme.bservices.controller.pipelines;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;
import eu.freme.bservices.testhelper.LoggingHelper;
import eu.freme.bservices.testhelper.OwnedResourceManagingHelper;
import eu.freme.bservices.testhelper.SimpleEntityRequest;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.exception.FileNotFoundException;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Pipeline;
import eu.freme.common.persistence.model.SerializedRequest;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 19.01.2016.
 */
public class PipeliningControllerTest {
    private Logger logger = Logger.getLogger(PipeliningControllerTest.class);
    AuthenticatedTestHelper ath;
    OwnedResourceManagingHelper<Pipeline> ormh;
    MockupRequestFactory rf;

    public PipeliningControllerTest() throws UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("pipelining-controller-test-package.xml");
        ath = context.getBean(AuthenticatedTestHelper.class);
        ormh = new OwnedResourceManagingHelper<>("/pipelines", Pipeline.class, ath, null);
        ath.authenticateUsers();
        rf = new MockupRequestFactory(ath.getAPIBaseUrl());
    }


    @Test
    public void testExecuteDefaultPipeline() throws UnirestException, IOException {
        Pipeline pipeline = createDefaultTemplate(OwnedResource.Visibility.PUBLIC);
        String id = pipeline.getIdentifier();
        String contents = "The Atomium in Brussels is the symbol of Belgium.";
        HttpResponse<String> response = sendRequest(ath.getTokenWithPermission(), HttpStatus.SC_OK, id, contents, RDFConstants.RDFSerialization.PLAINTEXT);
        ormh.deleteEntity(pipeline.getIdentifier(), ath.getTokenWithPermission(), org.springframework.http.HttpStatus.OK);
    }

    @Test
    public void testExecuteDefaultSingle() throws JsonProcessingException, UnirestException {
        SerializedRequest entityRequest = rf.createEntitySpotlight("en");
        SerializedRequest linkRequest = rf.createLink("3");    // Geo pos
        String contents = "The Atomium in Brussels is the symbol of Belgium.";
        HttpResponse<String> response = sendRequest(HttpStatus.SC_OK, contents, entityRequest, linkRequest);
    }

    @Test
    public void testCreateDefaultPipeline() throws UnirestException, IOException {
        Pipeline pipelineInfo = createDefaultTemplate(OwnedResource.Visibility.PUBLIC);
        assertFalse(pipelineInfo.isPersist());
        assertTrue(pipelineInfo.getId() > 0);
        ormh.deleteEntity(pipelineInfo.getIdentifier(), ath.getTokenWithPermission(), org.springframework.http.HttpStatus.OK);
    }

    // test pipeline privacy
    @Test
    public void testPipelining() throws IOException, UnirestException {
        Pipeline pipeline1 = createDefaultTemplate(OwnedResource.Visibility.PRIVATE);

        // use pipeline as
        String contents = "The Atomium in Brussels is the symbol of Belgium.";
        logger.info("execute private default pipeline with content=\""+contents+"\" as userWithPermission");
        sendRequest(ath.getTokenWithPermission(), HttpStatus.SC_OK, pipeline1.getIdentifier(), contents, RDFConstants.RDFSerialization.PLAINTEXT);

        logger.info("execute private pipeline as userWithoutPermission");
        LoggingHelper.loggerIgnore(LoggingHelper.accessDeniedExceptions);
        sendRequest(ath.getTokenWithoutPermission(), HttpStatus.SC_UNAUTHORIZED, pipeline1.getIdentifier(), contents, RDFConstants.RDFSerialization.PLAINTEXT);
        LoggingHelper.loggerUnignore(LoggingHelper.accessDeniedExceptions);

        // delete pipelines
        ormh.deleteEntity(pipeline1.getIdentifier(), ath.getTokenWithPermission(), org.springframework.http.HttpStatus.OK);
    }

    @Test
    public void testSomethingThatWorks() throws UnirestException, JsonProcessingException {
        String input = "With just 200,000 residents, Reykjavík ranks as one of Europe’s smallest capital cities. But when Iceland’s total population only hovers around 300,000, it makes sense that the capital is known as the “big city” and offers all the cultural perks of a much larger place.\n" +
                "\n" +
                "“From live music almost every night to cosy cafes, colourful houses and friendly cats roaming the street, Reykjavík has all the charms of a small town in a fun capital city,” said Kaelene Spence, ";
        SerializedRequest entityRequest = rf.createEntityFremeNER("en", "dbpedia");
        SerializedRequest linkRequest = rf.createLink("3");	// Geo pos
        SerializedRequest terminologyRequest = rf.createTerminology("en", "nl");

        sendRequest(HttpStatus.SC_OK, input, entityRequest, linkRequest, terminologyRequest);
    }

    //// test pipeline with link

    @Test
    public void testSpotlight() throws UnirestException, JsonProcessingException {
        String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
        SerializedRequest entityRequest = rf.createEntitySpotlight("en");
        SerializedRequest linkRequest = rf.createLink("3");	// Geo pos

        sendRequest(HttpStatus.SC_OK, data, entityRequest, linkRequest);
    }

    /**
     * e-Entity using FREME NER with database viaf and e-Link using template 3 (Geo pos). All should go well.
     * @throws UnirestException
     */
    @Test
    public void testFremeNER() throws UnirestException, JsonProcessingException {
        String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
        SerializedRequest entityRequest = rf.createEntityFremeNER("en", "viaf");
        SerializedRequest linkRequest = rf.createLink("3");	// Geo pos

        sendRequest(HttpStatus.SC_OK, data, entityRequest, linkRequest);
    }

    /**
     * e-Entity using an unexisting data set to test error reporting.
     */
    @Test
    @Ignore // doesnt work with mockup endpoint
    public void testWrongDatasetEntity() throws UnirestException, JsonProcessingException {
        String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
        SerializedRequest entityRequest = rf.createEntityFremeNER("en", "anunexistingdatabase");
        SerializedRequest linkRequest = rf.createLink("3");	// Geo pos

        sendRequest(HttpStatus.SC_BAD_REQUEST, data, entityRequest, linkRequest);
    }

    /**
     * e-Entity using an unexisting language set to test error reporting.
     */
    @Test
    @Ignore // doesnt work with mockup endpoint
    public void testWrongLanguageEntity() throws UnirestException, JsonProcessingException {
        String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
        SerializedRequest entityRequest = rf.createEntityFremeNER("zz", "viaf");
        SerializedRequest linkRequest = rf.createLink("3");	// Geo pos

        sendRequest(HttpStatus.SC_BAD_REQUEST, data, entityRequest, linkRequest);
    }


    //// pipeline management

    @Test
    public void testPipelineManagement() throws UnirestException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        String fn1 = classLoader.getResource("pipelines/pipeline1.json").getFile();
        if(fn1==null)
            throw new FileNotFoundException("could not read resource pipelines/pipeline1.json");
        File file1 = new File(fn1);
        String body1 = FileUtils.readFileToString(file1);

        String fn2 = classLoader.getResource("pipelines/pipeline2.json").getFile();
        if(fn2==null)
            throw new FileNotFoundException("could not read resource pipelines/pipeline2.json");
        File file2 = new File(fn2);
        String body2 = FileUtils.readFileToString(file2);
        ormh.checkCRUDOperations(
                new SimpleEntityRequest(body1, null, null),
                new SimpleEntityRequest(body2, null, null));

    }


    //////////////////////////////////////////////////////////////////////////////////
    //   PipelinesCommon                                                            //
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * Sends the actual pipeline request. It serializes the request objects to JSON and puts this into the body of
     * the request.
     * @param expectedResponseCode	The expected HTTP response code. Will be checked against.
     * @param requests		The serialized requests to send.
     * @return				The result of the request. This can either be the result of the pipelined requests, or an
     *                      error response with some explanation what went wrong in the body.
     * @throws UnirestException
     */
    protected HttpResponse<String> sendRequest(int expectedResponseCode, String content, final SerializedRequest... requests) throws UnirestException, JsonProcessingException {
        List<SerializedRequest> serializedRequests = Arrays.asList(requests);
        serializedRequests.get(0).setBody(content);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String body = ow.writeValueAsString(requests);

        HttpResponse<String> response =   Unirest.post(ath.getAPIBaseUrl() + "/pipelines/chain")
                .header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
                .body(body)
                .asString();

        RDFConstants.RDFSerialization responseContentType = RDFConstants.RDFSerialization.fromValue(response.getHeaders().getFirst("content-type"));
        RDFConstants.RDFSerialization accept = getContentTypeOfLastResponse(serializedRequests);
        assertEquals(expectedResponseCode, response.getStatus());
        if (expectedResponseCode / 100 != 2) {
            assertEquals(RDFConstants.RDFSerialization.JSON, responseContentType);
        } else {
            assertEquals(responseContentType, accept);
        }

        return response;
    }

    protected HttpResponse<String> sendRequest(final String token, int expectedResponseCode, String identifier, final String contents, final RDFConstants.RDFSerialization contentType) throws UnirestException {
        HttpResponse<String> response = ath.addAuthentication(Unirest.post(ath.getAPIBaseUrl() + "/pipelines/chain/"+identifier), token)
                .header("content-type", contentType.contentType())
                .body(contents)
                .asString();

        assertEquals(expectedResponseCode, response.getStatus());
        return response;
    }

    /**
     * Helper method that returns the content type of the response of the last request (or: the value of the 'accept'
     * header of the last request).
     * @param serializedRequests	The requests that (will) serve as input for the pipelining service.
     * @return						The content type of the response that the service will return.
     */
    protected static RDFConstants.RDFSerialization getContentTypeOfLastResponse(final List<SerializedRequest> serializedRequests) {
        String contentType = "";
        if (!serializedRequests.isEmpty()) {
            SerializedRequest lastRequest = serializedRequests.get(serializedRequests.size() - 1);
            Map<String, String> headers = lastRequest.getHeaders();
            if (headers.containsKey("accept")) {
                contentType = headers.get("accept");
            } else {
                Map<String, Object> parameters = lastRequest.getParameters();
                if (parameters.containsKey("outformat")) {
                    contentType = parameters.get("outformat").toString();
                }
            }
        }
        RDFConstants.RDFSerialization serialization = RDFConstants.RDFSerialization.fromValue(contentType);
        return serialization != null ? serialization : RDFConstants.RDFSerialization.TURTLE;
    }

    protected Pipeline createDefaultTemplate(final OwnedResource.Visibility visibility) throws UnirestException, IOException {
        SerializedRequest entityRequest = rf.createEntitySpotlight("en");
        SerializedRequest linkRequest = rf.createLink("3");    // Geo pos
        return createTemplate(visibility, "a label", "a description", entityRequest, linkRequest);
    }

    protected Pipeline createTemplate(final OwnedResource.Visibility visibility, final String label, final String description, final SerializedRequest... requests) throws UnirestException, IOException {
        List<SerializedRequest> serializedRequests = Arrays.asList(requests);

        // create local Entity to build json
        Pipeline pipeline = new Pipeline();
        pipeline.setVisibility(visibility);
        pipeline.setLabel(label);
        pipeline.setDescription(description);
        pipeline.setSerializedRequests(serializedRequests);
        pipeline.setPersist(false);
        // send json
        pipeline = ormh.createEntity(new SimpleEntityRequest(ormh.toJSON(pipeline),null,null), ath.getTokenWithPermission(), org.springframework.http.HttpStatus.OK);
        return pipeline;
    }
}
