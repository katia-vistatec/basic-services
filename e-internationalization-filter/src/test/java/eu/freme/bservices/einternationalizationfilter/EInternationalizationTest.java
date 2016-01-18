/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.bservices.einternationalizationfilter;
import java.io.File;
import java.io.IOException;

import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.ValidationHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.common.conversion.rdf.RDFConstants;
import org.springframework.context.ApplicationContext;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 06.08.15.
 */
public class EInternationalizationTest {

    TestHelper th;
    ValidationHelper vh;
    
    public EInternationalizationTest() throws  UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("einternationalizationfilter-test-package.xml");// FREMEStarter.startPackageFromClasspath("ratelimiter-test-package.xml");
        th = context.getBean(TestHelper.class);
        vh = context.getBean(ValidationHelper.class);
    }

    String dataset = "dbpedia";
    String[] sample_xliff = {"e-internationalization/test1.xlf"};
    String[] sample_html = {"e-internationalization/aa324.html", "e-internationalization/test10.html", "e-internationalization/test12.html"};

    @Test
    public void TestEInternationalization() throws IOException,
            UnirestException {
        // See EInternationalizationFilter
        // for (String sample_file : sample_xliff) {
        // testContentTypeandInformat("application/x-xliff+xml",readFile(resourcepath+sample_file));
        // }
        ClassLoader classLoader = getClass().getClassLoader();
        for (String sample_file : sample_html) {
            testContentTypeandInformat("text/html", FileUtils.readFileToString(new File(classLoader.getResource(sample_file).getFile())));
        }
    }

    protected HttpRequestWithBody baseRequestPost() {
        return Unirest.post("").queryString("dataset", dataset);
    }

    private void testContentTypeandInformat(String format, String data)
            throws UnirestException, IOException {
        HttpResponse<String> response;
        // With Content-Type header
        response = Unirest.post(th.getAPIBaseUrl()).header("Content-Type", format)
                .queryString("language", "en").body(data).asString();

        vh.validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);

        // With informat QueryString
        response = Unirest.post(th.getAPIBaseUrl()).queryString("informat", format)
                .queryString("language", "en").body(data).asString();
        vh.validateNIFResponse(response, RDFConstants.RDFSerialization.TURTLE);
    }

    @Test
    public void testRoundTripping() throws UnirestException, IOException {
        HttpResponse<String> response = Unirest
                .post(th.getAPIBaseUrl() + "/mockups/file/NER-This-is-Germany.ttl")
                .queryString("language", "en")
                .queryString("dataset", "dbpedia")
                .queryString("informat", "text/html")
                .queryString("outformat", "text/html")
                .body("<p>Berlin is a city in Germany</p>").asString();

        assertEquals(200,response.getStatus());
        assertTrue(response.getBody().length() > 0);

        String xliff = FileUtils.readFileToString(new File(
                "e-internationalization/test1.xlf"));
        response = Unirest
                .post(th.getAPIBaseUrl() + "/mockups/file/NER-This-is-Germany.ttl")
                .queryString("language", "en")
                .queryString("dataset", "dbpedia")
                .queryString("informat", "text/html")
                .queryString("outformat", "text/html").body(xliff).asString();

        assertEquals(200,response.getStatus());
        assertTrue(response.getBody().length() > 0);

        String longHtml = FileUtils.readFileToString(new File(
                "e-internationalization/long-html.html"));
        response = Unirest
                .post(th.getAPIBaseUrl() + "/mockups/file/NER-This-is-Germany.ttl")
                .queryString("language", "en")
                .queryString("dataset", "dbpedia")
                .queryString("informat", "text/html")
                .queryString("outformat", "text/html").body(longHtml).asString();

        assertEquals(200,response.getStatus());
        assertTrue(response.getBody().length() > 0);
    }

    @Test
    public void testXml() throws UnirestException {
        HttpResponse<String> response = Unirest
                .post(th.getAPIBaseUrl() + "/mockups/file/NER-This-is-Germany.ttl")
                .queryString("language", "en")
                .queryString("dataset", "dbpedia")
                .queryString("informat", "text/xml")
                .body("<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>")
                .asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().length() > 0);
    }

    @Test
    public void testOdt() throws IOException, UnirestException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("e-internationalization/odt-test.odt").getFile());
        String data = FileUtils.readFileToString(file);

        HttpResponse<String> response = Unirest
                .post(th.getAPIBaseUrl() + "/mockups/file/NER-This-is-Germany.ttl")
                .queryString("language", "en")
                .queryString("dataset", "dbpedia")
                .queryString("informat", "application/x-openoffice")
                .body(data)
                .asString();

        assertEquals(200,response.getStatus());
        assertTrue(response.getBody().length() > 0);
    }

}

