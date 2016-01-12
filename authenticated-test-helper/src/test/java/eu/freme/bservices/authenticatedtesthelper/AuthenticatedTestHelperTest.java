package eu.freme.bservices.authenticatedtesthelper;

import static org.junit.Assert.assertTrue;

import eu.freme.common.starter.FREMEStarter;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.FREMECommonConfig;

public class AuthenticatedTestHelperTest{

    ConfigurableApplicationContext context;
    Logger logger = Logger.getLogger(AuthenticatedTestHelperTest.class);

    AuthenticatedTestHelper testHelper;

    @Before
    public void setup() throws UnirestException {
        context = FREMEStarter.startPackageFromClasspath("authenticated-test-helper-test-package.xml");
        testHelper = context.getBean(AuthenticatedTestHelper.class);
        testHelper.authenticateUsers();
    }

    @Test
    public void test() throws UnirestException{
        //dummy
        logger.info("TEST DUMMY");
    }

    @After
    public void after() throws UnirestException {
        testHelper.removeAuthenticatedUsers();
        context.stop();
        logger.info("test successful");
    }
}