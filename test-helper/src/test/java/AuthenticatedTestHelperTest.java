package eu.freme.bservices.testhelper;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.FREMECommonConfig;
import eu.freme.common.rest.BaseRestController;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;

@ComponentScan({"eu.freme.bservices.testhelper"})
@Import(FREMECommonConfig.class)
public class AuthenticatedTestHelperTest{

    ConfigurableApplicationContext context;
    Logger logger = Logger.getLogger(AuthenticatedTestHelperTest.class);

    AuthenticatedTestHelper testHelper;

    @Before
    public void setup() throws UnirestException {
        context = SpringApplication.run(AuthenticatedTestHelperTest.class);
        AuthenticatedTestHelper testHelper = context.getBean(AuthenticatedTestHelper.class);
        testHelper.authenticateUsers();
    }

    @Test
    public void test() throws UnirestException{
        //dummy
    }

    @After
    public void after() throws UnirestException {
        testHelper.removeUsers();
        context.stop();
        logger.info("test successful");
    }
}