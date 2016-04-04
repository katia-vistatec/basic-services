package eu.freme.bservices.controllers.pipelines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Strings;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.controllers.pipelines.core.PipelineResponse;
import eu.freme.bservices.controllers.pipelines.core.PipelineService;
import eu.freme.bservices.controllers.pipelines.core.ServiceException;
import eu.freme.bservices.controllers.pipelines.core.WrappedPipelineResponse;
import eu.freme.bservices.controllers.pipelines.requests.RequestBuilder;
import eu.freme.bservices.controllers.pipelines.requests.RequestFactory;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.exception.TemplateNotFoundException;
import eu.freme.common.persistence.dao.OwnedResourceDAO;
import eu.freme.common.persistence.model.Pipeline;
import eu.freme.common.persistence.model.SerializedRequest;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.OwnedResourceManagingController;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 19.01.2016.
 */

@RestController
@RequestMapping("/pipelining")
public class PipelinesController extends BaseRestController {

    Logger logger = Logger.getLogger(PipelinesController.class);

    @Autowired
    PipelineService pipelineAPI;

    @Autowired
    OwnedResourceDAO<Pipeline> entityDAO;

    /**
     * <p>Calls the pipelining service.</p>
     * <p>Some predefined Requests can be formed using the class {@link RequestFactory}. It also converts request objects
     * from and to JSON.</p>
     * <p><To create custom requests, use the {@link RequestBuilder}.</p>
     * <p>Examples can be found in the unit tests in {@link eu/freme/broker/integration_tests/pipelines}.</p>
     * @param requests	The requests to send to the service.
     * @param stats		If "true": wrap the response of the last request and add timing statistics.
     * @return          The response of the last request.
     * @throws BadRequestException				The contents of the request is not valid.
     * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
     */
    @RequestMapping(value = "/chain",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3", "text/html"}
    )
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> pipeline(
            @RequestBody String requests,
            @RequestParam(value = "stats", defaultValue = "false", required = false) String stats
    ) {
        try {
            boolean wrapResult = Boolean.parseBoolean(stats);
            ObjectMapper mapper = new ObjectMapper();
            List<SerializedRequest> serializedRequests = mapper.readValue(requests,
                    TypeFactory.defaultInstance().constructCollectionType(List.class, eu.freme.common.persistence.model.SerializedRequest.class));
            //List<SerializedRequest> serializedRequests = //Serializer.fromJson(requests);
            WrappedPipelineResponse pipelineResult = pipelineAPI.chain(serializedRequests);
            MultiValueMap<String, String> headers = new HttpHeaders();

            if (wrapResult) {
                headers.add(HttpHeaders.CONTENT_TYPE, RDFConstants.RDFSerialization.JSON.contentType());
                ObjectWriter ow = new ObjectMapper().writer()
                        .withDefaultPrettyPrinter();
                String serialization = ow.writeValueAsString(pipelineResult);
                return new ResponseEntity<>(serialization, headers, HttpStatus.OK);
            } else {
                headers.add(HttpHeaders.CONTENT_TYPE, pipelineResult.getContent().getContentType());
                PipelineResponse lastResponse = pipelineResult.getContent();
                return new ResponseEntity<>(lastResponse.getBody(), headers, HttpStatus.OK);
            }

        } catch (ServiceException serviceError) {
            // TODO: see if this can be replaced by excsption(s) defined in the broker.
            logger.error(serviceError.getMessage(), serviceError);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, serviceError.getResponse().getContentType());
            return new ResponseEntity<>(serviceError.getMessage(), headers, serviceError.getStatus());
        } catch (JsonSyntaxException jsonException) {
            logger.error(jsonException.getMessage(), jsonException);
            String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
            throw new BadRequestException("Error detected in the JSON body contents: " + errormsg);
        } catch (UnirestException unirestException) {
            logger.error(unirestException.getMessage(), unirestException);
            throw new BadRequestException(unirestException.getMessage());
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            // throw an Internal Server exception if anything goes really wrong...
            throw new InternalServerErrorException(t.getMessage());
        }
    }

    /**
     * Calls the pipelining service using an existing template.
     * @param body	The contents to send to the pipeline. This can be a NIF or plain text document.
     * @param id	The id of the pipeline template to use.
     * @param stats		If "true": wrap the response of the last request and add timing statistics.
     * @return		The response of the latest request defined in the template.
     * @throws AccessDeniedException			The pipeline template is not visible by the current user.
     * @throws BadRequestException				The contents of the request is not valid.
     * @throws InternalServerErrorException		Something goes wrong that shouldn't go wrong.
     * @throws TemplateNotFoundException		The pipeline template does not exist.
     */
    @RequestMapping(value = "/chain/{id}",
            method = RequestMethod.POST,
            consumes = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3", "text/plain"},
            produces = {"text/turtle", "application/json", "application/ld+json", "application/n-triples", "application/rdf+xml", "text/n3"}
    )
    public ResponseEntity<String> pipeline(
            @RequestBody String body,
            @PathVariable String id,
            @RequestParam (value = "stats", defaultValue = "false", required = false) String stats
    ) throws IOException {
        try {
            Pipeline pipeline = entityDAO.findOneByIdentifier(id);
            List<SerializedRequest> serializedRequests = pipeline.getSerializedRequests();// Serializer.fromJson(pipeline.getSerializedRequests());
            serializedRequests.get(0).setBody(body);
            pipeline.setSerializedRequests(serializedRequests);
            return pipeline(pipeline.getRequests(), stats);
        } catch (org.springframework.security.access.AccessDeniedException | InsufficientAuthenticationException ex) {
            logger.error(ex.getMessage(), ex);
            throw new AccessDeniedException(ex.getMessage());
        } catch (JsonSyntaxException jsonException) {
            logger.error(jsonException.getMessage(), jsonException);
            String errormsg = jsonException.getCause() != null ? jsonException.getCause().getMessage() : jsonException.getMessage();
            throw new BadRequestException("Error detected in the JSON body contents: " + errormsg);
        } catch (OwnedResourceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            throw new TemplateNotFoundException("Could not find the pipeline template with id " + id);
        }
    }

}
