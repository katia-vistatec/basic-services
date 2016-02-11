package eu.freme.bservices.cloud.testservice;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.springframework.context.ConfigurableApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.starter.FREMEStarter;

public class TestServiceTest {

	@Test
	public void test() throws UnirestException {

		String port = "5000";
		ConfigurableApplicationContext ctx = FREMEStarter.startPackageFromClasspath(
				"cloud-test-package.xml", new String[] { "--server.port="
						+ port });

		HttpResponse<String> req = Unirest.get(
				"http://localhost:" + port + "/e-cloud-test").asString();
		
		assertTrue(req.getBody().equals(port));
		
		ctx.close();
	}
}
