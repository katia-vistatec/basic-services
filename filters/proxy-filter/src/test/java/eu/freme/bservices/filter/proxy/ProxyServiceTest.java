package eu.freme.bservices.filter.proxy;

import eu.freme.bservices.testhelper.LoggingHelper;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.starter.FREMEStarter;

import java.net.UnknownHostException;

public class ProxyServiceTest {

	@Test
	public void testProxy() throws UnirestException {

		FREMEStarter.startPackageFromClasspath("proxy-filter-test-package.xml");

		HttpResponse<String> response = Unirest
				.post("http://localhost:8080/e-proxy/test")
				.queryString("test", "value")
				.header("my-header", "header-value").body("body\nbody")
				.asString();

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().equals("response"));

		response = Unirest.get("http://localhost:8080/e-proxy/test2")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().equals("response"));

		response = Unirest.get("http://localhost:8080/e-proxy/test2/")
				.asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().equals("response"));

		LoggingHelper.loggerIgnore(UnknownHostException.class.getCanonicalName());
		response = Unirest.get("http://localhost:8080/e-proxy/test3")
				.asString();
		LoggingHelper.loggerUnignore(UnknownHostException.class.getCanonicalName());
		assertTrue(response.getStatus() == 502);
	}
}
