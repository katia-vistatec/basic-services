package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.starter.FREMEStarter;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne on 07.01.2016.
 */
public class TestHelperTest {

    Logger logger = Logger.getLogger(TestHelperTest.class);
    //TestHelper testHelper = new TestHelper("test-helper-test-package.xml");
 
   	ApplicationContext context = IntegrationTestSetup.getContext("test-helper-test-package.xml");//FREMEStarter.startPackageFromClasspath("test-helper-test-package.xml");

    @Test
    public void testAuthenticatedTestHelper() throws UnirestException {
        AuthenticatedTestHelper authenticatedTestHelper=context.getBean(AuthenticatedTestHelper.class);
        authenticatedTestHelper.authenticateUsers();
        authenticatedTestHelper.removeAuthenticatedUsers();
    }


    @Test
    public void testMockupEndpoint() throws UnirestException {
    	
    	TestHelper testHelper =context.getBean(TestHelper.class);
        String filename = "ELINK.ttl";
        logger.info("request file: "+filename);
        HttpResponse<String> response = Unirest.post(testHelper.getAPIBaseUrl() + "/mockups/file/"+filename).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }
 }
