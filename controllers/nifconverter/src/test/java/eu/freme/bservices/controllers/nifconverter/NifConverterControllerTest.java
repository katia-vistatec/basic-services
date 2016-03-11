package eu.freme.bservices.controllers.nifconverter;

import com.mashape.unirest.http.HttpResponse;
import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.RestHelper;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Arne on 10.03.2016.
 */
public class NifConverterControllerTest {
    private Logger logger = Logger.getLogger(NifConverterControllerTest.class);
    String url;
    RestHelper restHelper;

    public NifConverterControllerTest() {
        ApplicationContext context = IntegrationTestSetup.getContext("nif-converter-test-package.xml");
        TestHelper th = context.getBean(TestHelper.class);
        restHelper =  context.getBean(RestHelper.class);
        url = th.getAPIBaseUrl() + "/toolbox/nif-converter";
    }

    public void testConversion(RDFSerialization informat, RDFSerialization outformat) throws Exception {
        logger.info("check conversion: " + informat.name() + " -> " + outformat.name());

        String sendSerialization = "Hello world!";
        // convert text to input format
        if(informat != RDFSerialization.PLAINTEXT) {
            NIFParameterSet convertParameters = new NIFParameterSet(sendSerialization, RDFSerialization.PLAINTEXT, informat, restHelper.getDefaultPrefix());
            sendSerialization = restHelper.serializeNif(restHelper.convertInputToRDFModel(convertParameters), convertParameters.getOutformat());
        }
        NIFParameterSet parameters = new NIFParameterSet(sendSerialization, informat, outformat, restHelper.getDefaultPrefix());
        HttpResponse<String> response = restHelper.sendNifRequest(parameters, url);

        assertEquals(HttpStatus.SC_OK, response.getStatus());

        String expectedSerialization = restHelper.serializeNif(restHelper.convertInputToRDFModel(parameters), parameters.getOutformat());
        assertEquals(expectedSerialization, response.getBody());
    }

    @Test
    public void testAllConversions() throws Exception {
        List<RDFSerialization> informats = new ArrayList<>();
        List<RDFSerialization> outformats = new ArrayList<>();
        outformats.add(RDFSerialization.TURTLE);
        outformats.add(RDFSerialization.JSON_LD);
        outformats.add(RDFSerialization.N3);
        outformats.add(RDFSerialization.N_TRIPLES);
        outformats.add(RDFSerialization.RDF_XML);

        informats.addAll(outformats);
        informats.add(RDFSerialization.PLAINTEXT);

        for(RDFSerialization informat: informats){
            for(RDFSerialization outformat: outformats){
                testConversion(informat, outformat);
            }
        }

    }
}
