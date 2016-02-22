package eu.freme.bservices.filters.cors;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.common.starter.FREMEStarter;
import static org.junit.Assert.assertTrue;

public class CORSFilterTest{

	@Test
	public void test() throws UnirestException{
		ConfigurableApplicationContext context = FREMEStarter.startPackageFromClasspath("cors-filter-test-package.xml");
		TestHelper testHelper = context.getBean(TestHelper.class);
		String url = testHelper.getAPIBaseUrl();
		HttpResponse<String> response = Unirest.post(url + "/mockups/file/response.txt").asString();
		String header = response.getHeaders().get("access-control-allow-origin").get(0);
		assertTrue(header.equals("*"));
	}
}
