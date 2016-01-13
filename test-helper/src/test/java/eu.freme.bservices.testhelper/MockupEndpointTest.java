package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne on 07.01.2016.
 */
public class MockupEndpointTest {

    Logger logger = Logger.getLogger(MockupEndpointTest.class);
    TestHelper testHelper = new TestHelper("mockup-endpoint-test-package.xml");

    @Test
    public void test() throws UnirestException {
        String filename = "ELINK.ttl";
        logger.info("request file: "+filename);
        HttpResponse<String> response = Unirest.post(testHelper.getAPIBaseUrl() + "/mockups/file/"+filename).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

}
