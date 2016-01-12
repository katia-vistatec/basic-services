package eu.freme.bservices.filtercontroller;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.authenticatedtesthelper.AuthenticatedTestHelper;
import eu.freme.common.starter.FREMEStarter;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
//import eu.freme.bservices.

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 12.01.2016.
 */
public class FilterControllerTest {

    ConfigurableApplicationContext context;
    Logger logger = Logger.getLogger(FilterControllerTest.class);

    AuthenticatedTestHelper testHelper;

    @Before
    public void setup() throws UnirestException {
        context = FREMEStarter.startPackageFromClasspath("filter-controller-test-package.xml");
        testHelper = context.getBean(AuthenticatedTestHelper.class);
        testHelper.authenticateUsers();
    }

    @Test
    public void test() throws UnirestException{
        //dummy
    }

    @After
    public void after() throws UnirestException {
        testHelper.removeAuthenticatedUsers();
        context.stop();
        logger.info("test successful");
    }
}