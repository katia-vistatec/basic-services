package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.FREMECommonConfig;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne on 07.01.2016.
 */
@ComponentScan({"eu.freme.bservices.testhelper"})
@Import(FREMECommonConfig.class)
public class MockupEndpointTest {
    ConfigurableApplicationContext context;
    String baseUrl = null;
    Logger logger = Logger.getLogger(MockupEndpointTest.class);

    @Before
    public void setup() throws UnirestException {
        context = SpringApplication.run(MockupEndpointTest.class);
        SimpleTestHelper testHelper = context.getBean(SimpleTestHelper.class);
        baseUrl = testHelper.getAPIBaseUrl();
    }

    @Test
    public void test() throws UnirestException {
        String filename = "ELINK.ttl";
        logger.info("request file: "+filename);
        HttpResponse<String> response = Unirest.post(baseUrl + "/mockups/file/"+filename).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

}
