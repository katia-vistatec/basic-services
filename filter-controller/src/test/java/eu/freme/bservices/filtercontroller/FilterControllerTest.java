package eu.freme.bservices.filtercontroller;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.AuthenticatedBaseTest;
import eu.freme.common.conversion.rdf.JenaRDFConversionService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 12.01.2016.
 */
public class FilterControllerTest extends AuthenticatedBaseTest {
    public FilterControllerTest() {
        init(FilterControllerTest.class, "filter-controller-test-package.xml");
    }

    final String entityHeader = "entity";
    final String propertyIdentifier = "http://www.w3.org/2005/11/its/rdf#taIdentRef";
    final String resourceIdentifier = "http://dbpedia.org/resource/Berlin";

    final String filterSelect = "SELECT ?"+ entityHeader +" WHERE {[] <"+propertyIdentifier+"> ?"+ entityHeader +"}";
    final String filterConstruct = "CONSTRUCT {?s <"+propertyIdentifier+"> ?"+ entityHeader +"} WHERE {?s <"+propertyIdentifier+"> ?"+ entityHeader +"}";


    @Test
    public void testFilterManagement() throws UnirestException {
        logger.info("create filter1");
        String url = getAPIBaseUrl() + "/toolbox/filter/manage";
        HttpResponse<String> response = addAuthentication(Unirest.post(url))
                .queryString("entityId", "filter1")
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("get filter1");
        response = addAuthentication(Unirest.get(getAPIBaseUrl() + "/toolbox/filter/manage/filter1")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JSONObject json = new JSONObject(response.getBody());
        assertEquals(filterSelect, json.getString("query"));

        logger.info("update filter1");
        response = addAuthentication(Unirest.put(getAPIBaseUrl() + "/toolbox/filter/manage/filter1"))
                .body(filterConstruct)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("get updated filter1");
        response = addAuthentication(Unirest.get(getAPIBaseUrl() + "/toolbox/filter/manage/filter1")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        json = new JSONObject(response.getBody());
        assertEquals(filterConstruct, json.getString("query"));

        logger.info("create filter2");
        response = addAuthentication(Unirest.post(getAPIBaseUrl() + "/toolbox/filter/manage"))
                .queryString("entityId", "filter2")
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("get all filters");
        response = addAuthentication(Unirest.get(getAPIBaseUrl() + "/toolbox/filter/manage")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JSONArray jsonArray = new JSONArray(response.getBody());
        assertTrue((filterConstruct.equals(((JSONObject)jsonArray.get(0)).getString("query")) && filterSelect.equals(((JSONObject)jsonArray.get(1)).getString("query")))
                || (filterConstruct.equals(((JSONObject)jsonArray.get(1)).getString("query")) && filterSelect.equals(((JSONObject)jsonArray.get(0)).getString("query"))));

        logger.info("delete filter1");
        response = addAuthentication(Unirest.delete(getAPIBaseUrl() + "/toolbox/filter/manage/filter1")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        logger.info("delete filter2");
        response = addAuthentication(Unirest.delete(getAPIBaseUrl() + "/toolbox/filter/manage/filter2")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

    }

    @Test
    public void testFiltering() throws Exception {
        logger.info("create filter1");
        HttpResponse<String> response = addAuthentication(Unirest.post(getAPIBaseUrl() + "/toolbox/filter/manage"))
                .queryString("entityId", "filter1")
                .body(filterSelect)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        logger.info("create filter2");
        response = addAuthentication(Unirest.post(getAPIBaseUrl() + "/toolbox/filter/manage"))
                .queryString("entityId", "filter2")
                .body(filterConstruct)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String nifContent =
                " @prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
                        " @prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
                        "\n" +
                        "<http://127.0.0.1:9995/spotlight#char=0,15>\n" +
                        " a                     nif:Context , nif:Sentence , nif:RFC5147String ;\n" +
                        " nif:beginIndex        \"0\" ;\n" +
                        " nif:endIndex          \"15\" ;\n" +
                        " nif:isString          \"This is Berlin.\" ;\n" +
                        " nif:referenceContext  <http://127.0.0.1:9995/spotlight#char=0,15> .\n" +
                        "\n" +
                        "<http://127.0.0.1:9995/spotlight#char=8,14>\n" +
                        " a                     nif:Word , nif:RFC5147String ;\n" +
                        " nif:anchorOf          \"Berlin\" ;\n" +
                        " nif:beginIndex        \"8\" ;\n" +
                        " nif:endIndex          \"14\" ;\n" +
                        " nif:referenceContext  <http://127.0.0.1:9995/spotlight#char=0,15> ;\n" +
                        " <"+propertyIdentifier+">     <"+resourceIdentifier+"> .";


        logger.info("filter nif with filter1(select)");
        response = Unirest.post(getAPIBaseUrl() + "/toolbox/filter/documents/filter1")
                .queryString("informat", RDFConstants.RDFSerialization.TURTLE.contentType())
                .queryString("outformat", RDFConstants.RDFSerialization.JSON.contentType())
                .body(nifContent)
                .asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        InputStream stream = new ByteArrayInputStream(response.getBody().getBytes(StandardCharsets.UTF_8));
        ResultSet resultSet = ResultSetFactory.fromJSON(stream);
        // check resultSet content
        assertTrue(resultSet.nextSolution().get(entityHeader).asResource().equals(ResourceFactory.createResource(resourceIdentifier)));
        assertFalse(resultSet.hasNext());

        logger.info("filter nif with filter2(construct)");
        response = Unirest.post(getAPIBaseUrl() + "/toolbox/filter/documents/filter2")
                .queryString("informat", RDFConstants.RDFSerialization.TURTLE.contentType())
                .queryString("outformat", RDFConstants.RDFSerialization.TURTLE.contentType())
                .body(nifContent)
                .asString();
        //check status code
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        RDFConversionService rdfConversionService = new JenaRDFConversionService();
        Model resultModel = rdfConversionService.unserializeRDF(response.getBody(),RDFConstants.RDFSerialization.TURTLE);
        // check, if the result model contains the required triple
        Query askQuery = QueryFactory.create("ASK {[] <"+propertyIdentifier+"> <"+resourceIdentifier+">}");
        QueryExecution qexec = QueryExecutionFactory.create(askQuery, resultModel) ;
        assertTrue(qexec.execAsk());

        // check, if the result model contains no more triples
        Query countQuery = QueryFactory.create("SELECT (COUNT(*) as ?count) WHERE { ?s ?p ?o. }");
        qexec = QueryExecutionFactory.create(countQuery, resultModel) ;
        resultSet = qexec.execSelect();
        assertEquals(1,resultSet.nextSolution().getLiteral("count").getInt());

        logger.info("delete filter1");
        response = addAuthentication(Unirest.delete(getAPIBaseUrl() + "/toolbox/filter/manage/filter1")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        logger.info("delete filter2");
        response = addAuthentication(Unirest.delete(getAPIBaseUrl() + "/toolbox/filter/manage/filter2")).asString();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }


}