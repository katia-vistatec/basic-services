package eu.freme.bservices.filters.proxy;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.bservices.testhelper.*;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 25.11.15.
 */

public class ProxyFilterTest {

    TestHelper th;

    public ProxyFilterTest() throws  UnirestException {
    	ApplicationContext context = IntegrationTestSetup.getContext("proxyfilter-test-package.xml");
        th = context.getBean(TestHelper.class);
    }


    HttpResponse<String> response;

    Logger logger = Logger.getLogger(ProxyFilterTest.class);

    @Test
    public void testProxy() throws UnirestException, IOException {
        logger.info("starting Proxy Test");

        response = Unirest.post( th.getAPIBaseUrl()+"/e-entity/freme-ner/documents")
                .queryString("informat","text")
                .queryString("outformat","turtle")
                .queryString("dataset","dbpedia")
                .queryString("language","en")
                .header("Content-Type","text/plain")
             //   .header("Accept","turtle")
                .body("This is some text to be enriched")
                .asString();
    }
}