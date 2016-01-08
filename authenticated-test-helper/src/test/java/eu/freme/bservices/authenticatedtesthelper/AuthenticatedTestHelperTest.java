//package eu.freme.bservices.authenticatedtesthelper;
//
//import static org.junit.Assert.assertTrue;
//
//import org.apache.log4j.Logger;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.boot.SpringApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Import;
//
//import com.mashape.unirest.http.exceptions.UnirestException;
//
//import eu.freme.common.FREMECommonConfig;
//
//@ComponentScan({"eu.freme.bservices.authenticatedtesthelper", "eu.freme.bservices.usercontroller"})
//@Import(FREMECommonConfig.class)
//public class AuthenticatedTestHelperTest{
//
//    ConfigurableApplicationContext context;
//    Logger logger = Logger.getLogger(AuthenticatedTestHelperTest.class);
//
//    AuthenticatedTestHelper testHelper;
//
//    @Before
//    public void setup() throws UnirestException {
//        context = SpringApplication.run(AuthenticatedTestHelperTest.class);
//        testHelper = context.getBean(AuthenticatedTestHelper.class);
//        testHelper.authenticateUsers();
//    }
//
//    @Test
//    public void test() throws UnirestException{
//        //dummy
//    }
//
//    @After
//    public void after() throws UnirestException {
//        testHelper.removeAuthenticatedUsers();
//        context.stop();
//        logger.info("test successful");
//    }
//}