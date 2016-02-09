package eu.freme.bservices.filters.postprocessing;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.bservices.controllers.sparqlconverters.SparqlConverterController;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;
import eu.freme.bservices.testhelper.OwnedResourceManagingHelper;
import eu.freme.bservices.testhelper.SimpleEntityRequest;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.persistence.model.SparqlConverter;

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

    String filterSelect = "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#> SELECT ?entity WHERE {?charsequence itsrdf:taIdentRef ?entity}";
    String filterName = "extract-entities-only";
    String csvResponse = "entity\n" +
            "http://dbpedia.org/resource/Champ_de_Mars\n" +
            "http://dbpedia.org/resource/Eiffel_Tower\n" +
            "http://dbpedia.org/resource/France\n" +
            "http://dbpedia.org/resource/Paris\n" +
            "http://dbpedia.org/resource/Eiffel_(programming_language)";

    public PostprocessingFilterTest() throws UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("postprocessing-filter-test-package.xml");
        ath = context.getBean(AuthenticatedTestHelper.class);
        ath.authenticateUsers();
        ormh = new OwnedResourceManagingHelper<>(serviceUrl,SparqlConverter.class, ath, SparqlConverterController.identifierName);
    }

    @Test
    public void testPostprocessing() throws UnirestException, IOException {
        HttpResponse<String> response;

        logger.info("create filter "+filterName);
        ormh.createEntity(new SimpleEntityRequest(filterSelect).putParameter(SparqlConverterController.identifierParameterName, filterName),
                ath.getTokenWithPermission(),
                HttpStatus.OK);

        //String url = ath.getAPIBaseUrl() + "/toolbox/filter/manage";
        //response = ath.addAuthentication(Unirest.post(url))
         //       .queryString(SparqlConverterController.identifierParameterName, filterName)
         //       .body(filterSelect)
         //       .asString();
        //assertEquals(HttpStatus.OK.value(), response.getStatus());

        String filename = "postprocessing-data.ttl";
        logger.info("request file: "+filename);
        response = Unirest.post(ath.getAPIBaseUrl() + "/mockups/file/"+filename+"?filter="+filterName+"&outformat=csv").asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
        // clean line endings and check content
        assertEquals(csvResponse.trim(), response.getBody().trim().replaceAll("\r",""));


        logger.info("delete filter extract-entities-only");
        ormh.deleteEntity(filterName, ath.getTokenWithPermission(), HttpStatus.OK);
        //response = ath.addAuthentication(Unirest.delete(ath.getAPIBaseUrl() + "/toolbox/filter/manage/"+filterName)).asString();
        //assertEquals(HttpStatus.OK.value(), response.getStatus());
    }


}
