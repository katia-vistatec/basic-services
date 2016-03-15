package eu.freme.bservices.filter.proxy;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.starter.FREMEStarter;

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
	}
}
