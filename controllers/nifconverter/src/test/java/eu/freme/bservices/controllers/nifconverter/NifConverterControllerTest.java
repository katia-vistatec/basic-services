package eu.freme.bservices.controllers.nifconverter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertEquals;

/**
 * Created by Arne on 10.03.2016.
 */
public class NifConverterControllerTest {
    private Logger logger = Logger.getLogger(NifConverterControllerTest.class);
    String baseUrl;

    public NifConverterControllerTest() {
        ApplicationContext context = IntegrationTestSetup.getContext("nif-converter-test-package.xml");
        TestHelper th = context.getBean(TestHelper.class);
        baseUrl = th.getAPIBaseUrl() + "/toolbox/nif-converter";
    }

    @Test
    public void testPlaintextToTurtle() throws UnirestException {
        HttpResponse<String> response = Unirest.post(baseUrl)
                .queryString("informat", "text")
                .queryString("outformat", "turtle")
                .body("Hello world!")
                .asString();

        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }
}
