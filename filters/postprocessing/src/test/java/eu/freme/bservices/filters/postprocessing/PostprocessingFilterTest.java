package eu.freme.bservices.filters.postprocessing;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.controllers.sparqlconverters.SparqlConverterController;
import eu.freme.bservices.controllers.sparqlconverters.SparqlConverterManagingController;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;
import eu.freme.bservices.testhelper.LoggingHelper;
import eu.freme.bservices.testhelper.OwnedResourceManagingHelper;
import eu.freme.bservices.testhelper.SimpleEntityRequest;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.exception.AccessDeniedException;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.persistence.model.OwnedResource;
import eu.freme.common.persistence.model.SparqlConverter;
import eu.freme.common.rest.OwnedResourceManagingController;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 19.01.2016.
 */
public class PostprocessingFilterTest {

    AuthenticatedTestHelper ath;
    Logger logger = Logger.getLogger(PostprocessingFilterTest.class);
    private OwnedResourceManagingHelper<SparqlConverter> ormh;
    final static String serviceUrl = "/toolbox/convert";
    final static String managingURL = "/toolbox/convert/manage";

    String filterSelect = "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#> SELECT ?entity WHERE {?charsequence itsrdf:taIdentRef ?entity}";
    String filterName = "extract-entities-only";
    String csvResponse = "entity\n" +
            "http://dbpedia.org/resource/Champ_de_Mars\n" +
            "http://dbpedia.org/resource/Eiffel_Tower\n" +
            "http://dbpedia.org/resource/France\n" +
            "http://dbpedia.org/resource/Paris\n" +
            "http://dbpedia.org/resource/Eiffel_(programming_language)";

    public PostprocessingFilterTest() throws UnirestException, IOException {
        ApplicationContext context = IntegrationTestSetup.getContext("postprocessing-filter-test-package.xml");
        ath = context.getBean(AuthenticatedTestHelper.class);
        ath.authenticateUsers();
        ormh = new OwnedResourceManagingHelper<>(managingURL,SparqlConverter.class, ath);
    }

    @Test
    public void testPostprocessing() throws UnirestException, IOException {
        HttpResponse<String> response;

        logger.info("create filter "+filterName);
        ormh.createEntity(new SimpleEntityRequest(filterSelect).putParameter(SparqlConverterManagingController.identifierParameterName, filterName),
                ath.getTokenWithPermission(),
                HttpStatus.OK);

        String filename = "postprocessing-data.ttl";
        logger.info("request file: "+filename);
        response = Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv").asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        // clean line endings and check content
        assertEquals(csvResponse.trim(), response.getBody().trim().replaceAll("\r",""));


        logger.info("delete filter extract-entities-only");
        ormh.deleteEntity(filterName, ath.getTokenWithPermission(), HttpStatus.OK);
    }


    @Test
    public void testPostprocessingWithNotExisting() throws UnirestException, IOException {
        HttpResponse<String> response;

        String filename = "postprocessing-data.ttl";
        logger.info("request file: "+filename);
        LoggingHelper.loggerIgnore(OwnedResourceNotFoundException.class.getCanonicalName());
        response = Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv").asString();
        LoggingHelper.loggerUnignore(OwnedResourceNotFoundException.class.getCanonicalName());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void testPostprocessingWithPrivateFilter() throws UnirestException, IOException {
        HttpResponse<String> response;

        logger.info("create filter "+filterName);
        ormh.createEntity(new SimpleEntityRequest(filterSelect)
                .putParameter(SparqlConverterManagingController.identifierParameterName, filterName)
                .putParameter(OwnedResourceManagingController.visibilityParameterName, OwnedResource.Visibility.PRIVATE.toString()),
                ath.getTokenWithPermission(),
                HttpStatus.OK);

        String filename = "postprocessing-data.ttl";

        logger.info("request file and try to postprocess as anonymous user: "+filename +". Should fail.");
        LoggingHelper.loggerIgnore(AccessDeniedException.class.getCanonicalName());
        response = Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv")
                .asString();
        LoggingHelper.loggerUnignore(AccessDeniedException.class.getCanonicalName());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        logger.info("request file and try to postprocess as userWithoutPermission: "+filename +". Should fail.");
        LoggingHelper.loggerIgnore(AccessDeniedException.class.getCanonicalName());
        response = ath.addAuthenticationWithoutPermission(Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv"))
                .asString();
        LoggingHelper.loggerUnignore(AccessDeniedException.class.getCanonicalName());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        logger.info("request file and try to postprocess as owner: "+filename);
        response = ath.addAuthentication(Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv"))
                        .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        // clean line endings and check content
        assertEquals(csvResponse.trim(), response.getBody().trim().replaceAll("\r",""));

        logger.info("request file and try to postprocess as admin: "+filename);
        response = ath.addAuthenticationWithAdmin(Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv"))
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        // clean line endings and check content
        assertEquals(csvResponse.trim(), response.getBody().trim().replaceAll("\r",""));

        logger.info("delete filter extract-entities-only");
        ormh.deleteEntity(filterName, ath.getTokenWithPermission(), HttpStatus.OK);
    }

    @Test
    public void testPostprocessingWithWrongOutformat() throws UnirestException, IOException {
        HttpResponse<String> response;

        logger.info("create filter "+filterName);
        ormh.createEntity(new SimpleEntityRequest(filterSelect).putParameter(SparqlConverterManagingController.identifierParameterName, filterName),
                ath.getTokenWithPermission(),
                HttpStatus.OK);

        String filename = "postprocessing-data.ttl";
        logger.info("try to filter with outformat: blub. Should fail.");
        LoggingHelper.loggerIgnore(BadRequestException.class.getCanonicalName());
        response = Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=blub").asString();
        LoggingHelper.loggerUnignore(BadRequestException.class.getCanonicalName());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        logger.info("try to filter with accept header: blub. Should fail.");
        LoggingHelper.loggerIgnore(BadRequestException.class.getCanonicalName());
        response = Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName)
                .header("Accept", "blub")
                .asString();
        LoggingHelper.loggerUnignore(BadRequestException.class.getCanonicalName());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        logger.info("delete filter extract-entities-only");
        ormh.deleteEntity(filterName, ath.getTokenWithPermission(), HttpStatus.OK);
    }
}
