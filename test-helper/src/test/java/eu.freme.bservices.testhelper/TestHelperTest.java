package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne on 07.01.2016.
 */
public class TestHelperTest {

    Logger logger = Logger.getLogger(TestHelperTest.class);
    TestHelper testHelper = new TestHelper("mockup-endpoint-test-package.xml");

    @Test
    public void testMockupEndpoint() throws UnirestException {
        String filename = "ELINK.ttl";
        logger.info("request file: "+filename);
        HttpResponse<String> response = Unirest.post(testHelper.getAPIBaseUrl() + "/mockups/file/"+filename).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

}
