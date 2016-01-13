package eu.freme.bservices.filter.ratelimiter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.*;
import eu.freme.common.FREMECommonConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.io.File;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 25.11.15.
 */

public class RateLimitingFilterTest {

    AuthenticatedTestHelper testHelper;

    public RateLimitingFilterTest() throws  UnirestException {
        testHelper = new AuthenticatedTestHelper("ratelimiter-test-package.xml");
    }


    HttpResponse<String> response;

    Logger logger = Logger.getLogger(RateLimitingFilterTest.class);


    String testusername="ratelimitertestuser";
    String testpassword="ratelimiterpassword";


    @Test
    public void testRatelimiting() throws UnirestException, IOException {
        logger.info("starting ratelimiter test");

        logger.info("creating User for ratelimiter test");
        testHelper.createUser(testusername, testpassword);
        logger.info("authenticating this user");
        String ratelimiterToken = testHelper.authenticateUser(testusername, testpassword);
        logger.info("trying /e-link/templates call as ratelimitertestuser - should work the first time");

        response = testHelper.addAuthentication(Unirest.get(testHelper.getAPIBaseUrl()+"/mockups/ratelimiting"), ratelimiterToken).asString();
        logger.info(response.getBody());
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS.value(),response.getStatus());
        logger.info("trying /e-entity/freme-ner/datasets call as ratelimitertestuser - should not work the second time");
        LoggingHelper.loggerIgnore("TooManyRequestsException");
        response = testHelper.addAuthentication(Unirest.get(testHelper.getAPIBaseUrl()+"/mockups/ratelimiting"), ratelimiterToken).asString();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(),response.getStatus());
        LoggingHelper.loggerUnignore("TooManyRequestsException");

        logger.info("trying /e-translate/tilde test with huge size as ratelimitertestuser - should not work");
        LoggingHelper.loggerIgnore("TooManyRequestsException");
        response = testHelper.addAuthentication(Unirest.post(testHelper.getAPIBaseUrl()+"/mockups/ratelimiting2"), ratelimiterToken)
                .queryString("informat", "text")
                .queryString("outformat","turtle")
                .queryString("source-lang","en")
                .queryString("target-lang","de")
                .body(FileUtils.readFileToString(new File("src/test/resources/rdftest/e-translate/ELINK.ttl")))
                .asString();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(),response.getStatus());
        LoggingHelper.loggerUnignore("TooManyRequestsException");

        logger.info("trying anoter call for which there is no rate-limiting, should work");
        response = Unirest.get(testHelper.getAPIBaseUrl()+"/e-link/templates").asString();
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatus());
    }
}