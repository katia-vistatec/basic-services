package eu.freme.bservices.controller.pipeliningcontroller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.controller.pipeliningcontroller.requests.RequestFactory;
import eu.freme.bservices.controller.pipeliningcontroller.requests.SerializedRequest;
import eu.freme.bservices.controller.pipeliningcontroller.serialization.Pipeline_local;
import eu.freme.bservices.controller.pipeliningcontroller.serialization.Serializer;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;
import eu.freme.bservices.testhelper.LoggingHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.Pipeline;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

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
    LoggingHelper lh;

    public PipeliningControllerTest() throws UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("pipelining-controller-test-package.xml");
        ath = context.getBean(AuthenticatedTestHelper.class);
        ath.authenticateUsers();
        lh = context.getBean(LoggingHelper.class);
    }


    //////////////////////////////////////////////////////////////////////////////////
    // PipelinesCommon
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
    protected HttpResponse<String> sendRequest(int expectedResponseCode, final SerializedRequest... requests) throws UnirestException {
        List<SerializedRequest> serializedRequests = Arrays.asList(requests);
        String body = Serializer.toJson(requests);

        HttpResponse<String> response =   Unirest.post(ath.getAPIBaseUrl() + "/pipelines/chain")
                .header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
                .body(new JsonNode(body))
                .asString();

        // print some response info
        logger.info("response.getStatus() = " + response.getStatus());
        logger.info("response.getStatusText() = " + response.getStatusText());
        logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
        logger.debug("response.getBody() = " + response.getBody());

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

    protected HttpResponse<String> sendRequest(final String token, int expectedResponseCode, long id, final String contents, final RDFConstants.RDFSerialization contentType) throws UnirestException {
        HttpResponse<String> response = ath.addAuthentication(Unirest.post(ath.getAPIBaseUrl() + "/pipelines/chain/"+id), token)
                .header("content-type", contentType.contentType())
                .body(contents)
                .asString();

        // print some response info
        logger.info("response.getStatus() = " + response.getStatus());
        logger.info("response.getStatusText() = " + response.getStatusText());
        logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
        logger.debug("response.getBody() = " + response.getBody());

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

    protected Pipeline_local createDefaultTemplate(final String token, final OwnedResource.Visibility visibility) throws UnirestException {
        SerializedRequest entityRequest = RequestFactory.createEntitySpotlight("en");
        SerializedRequest linkRequest = RequestFactory.createLink("3");    // Geo pos
        return createTemplate(token, visibility, "a label", "a description", entityRequest, linkRequest);
    }

    protected Pipeline_local createTemplate(final String token, final OwnedResource.Visibility visibility, final String label, final String description, final SerializedRequest... requests) throws UnirestException {
        List<SerializedRequest> serializedRequests = Arrays.asList(requests);

        Pipeline_local pipeline = new Pipeline_local(label, description, serializedRequests);
        String body = Serializer.toJson(pipeline);
        HttpResponse<String> response = ath.addAuthentication(Unirest.post(ath.getAPIBaseUrl()+ "/pipelines/manage"), token)
                .queryString("visibility", visibility.name())
                .header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
                .body(new JsonNode(body))
                .asString();
        // print some response info
        logger.info("response.getStatus() = " + response.getStatus());
        logger.info("response.getStatusText() = " + response.getStatusText());
        logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
        logger.debug("response.body = " + response.getBody());
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        Pipeline_local pipelineInfo = Serializer.templateFromJson(response.getBody());
        pipelineInfo.setSerializedRequests(serializedRequests);
        return pipelineInfo;
    }

    protected String updateTemplate(final String token, final Pipeline_local newPipeline, int expectedResponseCode) throws UnirestException {
        // mind that all info is gathered from the newPipeline. The request itself expects owner, visibility and persistence
        // as parameters!
        String owner = newPipeline.getOwner();
        String visibility = newPipeline.getVisibility();
        String toPersist = Boolean.toString(newPipeline.isPersist());
        String body = Serializer.toJson(newPipeline);
        HttpResponse<String> response = ath.addAuthentication(Unirest.put(ath.getAPIBaseUrl()+ "/pipelines/manage/" + newPipeline.getId()), token)
                .header("content-type", RDFConstants.RDFSerialization.JSON.contentType())
                .queryString("owner", owner)
                .queryString("visibility", visibility)
                .queryString("persist", toPersist)
                .body(new JsonNode(body))
                .asString();
        logger.info("response.getStatus() = " + response.getStatus());
        logger.info("response.getStatusText() = " + response.getStatusText());
        logger.info("response.contentType = " + response.getHeaders().getFirst("content-type"));
        logger.debug("response.body = " + response.getBody());
        assertEquals(expectedResponseCode, response.getStatus());
        return response.getBody();
    }

    protected List<Pipeline_local> readTemplates(final String token) throws UnirestException {
        HttpResponse<String> response = ath.addAuthentication(Unirest.get(ath.getAPIBaseUrl()+"/pipelines/manage"), token).asString();
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        return Serializer.templatesFromJson(response.getBody());
    }

    protected Pipeline_local readTemplate(final String token, long id) throws UnirestException {
        HttpResponse<String> response = ath.addAuthentication(Unirest.get(ath.getAPIBaseUrl()+"/pipelines/manage/" + id), token).asString();
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        return Serializer.templateFromJson(response.getBody());
    }

    protected void deleteTemplate(final String token, long id, int expectedResponseCode) throws UnirestException {
        HttpResponse<String> response = ath.addAuthentication(Unirest.delete(ath.getAPIBaseUrl()+"/pipelines/manage/" + id), token).asString();
        logger.info("Response body: " + response.getBody());
        assertEquals(expectedResponseCode, response.getStatus());
        if (expectedResponseCode == HttpStatus.SC_OK) {
            assertEquals("The pipeline was sucessfully removed.", response.getBody());
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // CRUDTest
    /////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testCreateDefault() throws UnirestException {
        Pipeline_local pipelineInfo = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
        assertFalse(pipelineInfo.isPersist());
        assertTrue(pipelineInfo.getId() > 0);
        deleteTemplate(ath.getTokenWithPermission(), pipelineInfo.getId(), HttpStatus.SC_OK);
    }

    @Test
    public void testCreateAndRead() throws UnirestException {
        Pipeline_local pipelineInfo = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PRIVATE);
        long id = pipelineInfo.getId();

        // now query pipeline with id
        HttpResponse<String> readResponse = ath.addAuthentication(Unirest.get(ath.getAPIBaseUrl()+ "pipelines/manage/" + id)).asString();
        assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
        Pipeline_local readPipeline = Serializer.templateFromJson(readResponse.getBody());
        assertEquals(pipelineInfo.getId(), readPipeline.getId());
        assertEquals(pipelineInfo.getSerializedRequests(), readPipeline.getSerializedRequests());
        deleteTemplate(ath.getTokenWithPermission(), id, HttpStatus.SC_OK);
    }

    @Test
    public void testCreatePrivateWithOneAndReadWithOther() throws UnirestException {
        Pipeline_local pipelineInfo = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PRIVATE);
        long id = pipelineInfo.getId();

        // now query pipeline with id
        HttpResponse<String> readResponse = ath.addAuthentication(Unirest.get(ath.getAPIBaseUrl()+ "/pipelines/manage/" + id)).asString();
        assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
        Pipeline_local readPipeline = Serializer.templateFromJson(readResponse.getBody());
        assertEquals(pipelineInfo.getId(), readPipeline.getId());
        assertEquals(pipelineInfo.getSerializedRequests(), readPipeline.getSerializedRequests());

        // now try to read pipeline with other user
        lh.loggerIgnore(lh.accessDeniedExceptions);
        HttpResponse<String> readResponseOther = ath.addAuthenticationWithoutPermission(Unirest.get(ath.getAPIBaseUrl()+ "/pipelines/manage/" + id)).asString();
        assertEquals(HttpStatus.SC_UNAUTHORIZED, readResponseOther.getStatus());
        lh.loggerUnignore(lh.accessDeniedExceptions);
        logger.info("Response for unauthorized user: " + readResponseOther.getBody());

        deleteTemplate(ath.getTokenWithPermission(), id, HttpStatus.SC_OK);
    }

    @Test
    public void testCreateAndReadMultiple() throws UnirestException {
        logger.info("Creating one public and one private pipeline per user");
        Pipeline_local pipeline1 = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
        Pipeline_local pipeline2 = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PRIVATE);
        Pipeline_local pipeline3 = createDefaultTemplate(ath.getTokenWithOutPermission(), OwnedResource.Visibility.PUBLIC);
        Pipeline_local pipeline4 = createDefaultTemplate(ath.getTokenWithOutPermission(), OwnedResource.Visibility.PRIVATE);

        // now try to read pipeline with other user
        logger.info("Each user tries to read pipelines; only 3 should be visible.");
        List<Pipeline_local> pipelinesFromUser1 = readTemplates(ath.getTokenWithPermission());
        assertEquals(3, pipelinesFromUser1.size());	// TODO: delete pipelines after each test, then this can be "equals"
        for (Pipeline_local pipeline : pipelinesFromUser1) {
            // TODO: re-add this!
            //assertTrue(pipeline.getOwner().equals(usernameWithPermission) || pipeline.getVisibility().equals(OwnedResource.Visibility.PUBLIC.name()));
        }
        List<Pipeline_local> pipelinesFromUser2 = readTemplates(ath.getTokenWithOutPermission());
        assertEquals(3, pipelinesFromUser2.size());
        for (Pipeline_local pipeline : pipelinesFromUser2) {
            // TODO: re-add this!
            // assertTrue(pipeline.getOwner().equals(usernameWithoutPermission) || pipeline.getVisibility().equals(OwnedResource.Visibility.PUBLIC.name()));
        }
        List<Pipeline_local> pipelinesFromAdmin = readTemplates(ath.getTokenAdmin());
        assertEquals(2, pipelinesFromAdmin.size());
        // TODO: shouldn't the admin see all templates?

        deleteTemplate(ath.getTokenWithPermission(), pipeline1.getId(), HttpStatus.SC_OK);
        deleteTemplate(ath.getTokenWithPermission(), pipeline2.getId(), HttpStatus.SC_OK);
        deleteTemplate(ath.getTokenWithOutPermission(), pipeline3.getId(), HttpStatus.SC_OK);
        deleteTemplate(ath.getTokenWithOutPermission(), pipeline4.getId(), HttpStatus.SC_OK);
    }

    @Test
    public void testAllMethods() throws UnirestException {

        // create 2 templates
        Pipeline_local pipeline1 = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
        SerializedRequest nerRequest = RequestFactory.createEntityFremeNER("en", "dbpedia");
        SerializedRequest translateRequest = RequestFactory.createTranslation("en", "fr");
        Pipeline_local pipeline2 = createTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PRIVATE, "NER-Translate", "Apply FRENE NER and then e-Translate", nerRequest, translateRequest);

        // list the pipelines
        List<Pipeline_local> pipelines = readTemplates(ath.getTokenWithPermission());
        assertEquals(pipeline1, pipelines.get(0));
        assertEquals(pipeline2, pipelines.get(1));

        // read individual pipelines
        Pipeline_local storedPipeline1 = readTemplate(ath.getTokenWithPermission(), pipeline1.getId());
        Pipeline_local storedPipeline2 = readTemplate(ath.getTokenWithPermission(), pipeline2.getId());
        assertEquals(pipeline1, storedPipeline1);
        assertEquals(pipeline2, storedPipeline2);

        // use pipelines
        String contents = "The Atomium in Brussels is the symbol of Belgium.";
        sendRequest(ath.getTokenWithPermission(), HttpStatus.SC_OK, pipeline1.getId(), contents, RDFConstants.RDFSerialization.PLAINTEXT);
        sendRequest(ath.getTokenWithPermission(), HttpStatus.SC_OK, pipeline2.getId(), contents, RDFConstants.RDFSerialization.PLAINTEXT);

        // update pipeline 1
        pipeline1.setVisibility(OwnedResource.Visibility.PRIVATE.name());
        updateTemplate(ath.getTokenWithPermission(), pipeline1, HttpStatus.SC_OK);
        storedPipeline1 = readTemplate(ath.getTokenWithPermission(), pipeline1.getId());
        assertEquals(pipeline1, storedPipeline1);

        // delete pipelines
        deleteTemplate(ath.getTokenWithPermission(), pipeline1.getId(), HttpStatus.SC_OK);
        deleteTemplate(ath.getTokenWithPermission(), pipeline2.getId(), HttpStatus.SC_OK);
    }

    @Test
    public void testDeleteNonExisting() throws UnirestException {
        lh.loggerIgnore("eu.freme.common.exception.OwnedResourceNotFoundException || EXCEPTION ~=eu.freme.broker.exception.TemplateNotFoundException");
        deleteTemplate(ath.getTokenWithPermission(), -5, HttpStatus.SC_NOT_FOUND);
        lh.loggerUnignore("eu.freme.common.exception.OwnedResourceNotFoundException || EXCEPTION ~=eu.freme.broker.exception.TemplateNotFoundException");

    }

    @Test
    public void testDeleteFromAnother() throws UnirestException {
        Pipeline_local pipeline = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
        lh.loggerIgnore("eu.freme.broker.exception.ForbiddenException");
        deleteTemplate(ath.getTokenWithOutPermission(), pipeline.getId(), HttpStatus.SC_FORBIDDEN);
        lh.loggerUnignore("eu.freme.broker.exception.ForbiddenException");
        deleteTemplate(ath.getTokenWithPermission(), pipeline.getId(), HttpStatus.SC_OK);
    }

    @Test
    public void testSimpleUpdate() throws UnirestException {
        Pipeline_local pipeline = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
        pipeline.setDescription("This is a new description!");
        pipeline.setLabel("And a new label too!");
        String serialized = updateTemplate(ath.getTokenWithPermission(), pipeline, HttpStatus.SC_OK);
        Pipeline_local newPipeline = Serializer.templateFromJson(serialized);
        assertEquals(pipeline, newPipeline);
        deleteTemplate(ath.getTokenWithPermission(), pipeline.getId(), HttpStatus.SC_OK);
    }

    @Test
    public void testExecuteTemplate() throws UnirestException {
        Pipeline_local pipeline = createDefaultTemplate(ath.getTokenWithPermission(), OwnedResource.Visibility.PUBLIC);
        long id = pipeline.getId();
        String contents = "The Atomium in Brussels is the symbol of Belgium.";
        HttpResponse<String> response = sendRequest(ath.getTokenWithPermission(), HttpStatus.SC_OK, id, contents, RDFConstants.RDFSerialization.PLAINTEXT);
        deleteTemplate(ath.getTokenWithPermission(), pipeline.getId(), HttpStatus.SC_OK);
    }

    @Test
    public void testPipelining(){

    }

    @Test
    public void testPipelineManagament(){
        
    }


}
