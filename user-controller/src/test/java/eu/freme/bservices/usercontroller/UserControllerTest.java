package eu.freme.bservices.usercontroller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.common.rest.BaseRestController;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;
import eu.freme.common.starter.FREMEStarter;

public class UserControllerTest{

	ConfigurableApplicationContext context;
	
	String baseUrl = null;
	Logger logger = Logger.getLogger(UserControllerTest.class);

	String adminUsername;
	String adminPassword;

	@Before
	public void setup() {
		
		context = FREMEStarter.startPackageFromClasspath("user-controller-test-package.xml");

		String port = context.getEnvironment().getProperty("server.port");
		if( port == null){
			port = "8080";
		}
		baseUrl = "http://localhost:" + port;

		adminUsername = context.getEnvironment().getProperty("admin.username");
		adminPassword = context.getEnvironment().getProperty("admin.password");
	}
	
	@Test
	public void testAdmin() throws UnirestException{

		String username = "carlos";
		String password = "carlosss";
		logger.info("create user \"" + username + "\" and get token");

		HttpResponse<String> response = Unirest.post(baseUrl + "/user")
				.queryString("username", username)
				.queryString("password", password).asString();

		response = Unirest
				.post(baseUrl + BaseRestController.authenticationEndpoint)
				.header("X-Auth-Username", username)
				.header("X-Auth-Password", password).asString();
		String token = new JSONObject(response.getBody()).getString("token");

		logger.info("try to access /user endpoint from user account - should not work");
		//loggerIgnore(accessDeniedExceptions);
		response = Unirest
				.get(baseUrl + "/user")
				.header("X-Auth-Token", token).asString();

		assertTrue(response.getStatus() == HttpStatus.FORBIDDEN.value());
		//loggerUnignore(accessDeniedExceptions);

		logger.info("access /user endpoint with admin credentials");
		response = Unirest
				.post(baseUrl + BaseRestController.authenticationEndpoint)
				.header("X-Auth-Username", adminUsername)
				.header("X-Auth-Password", adminPassword).asString();
		token = new JSONObject(response.getBody()).getString("token");

		response = Unirest
				.get(baseUrl + "/user")
				.header("X-Auth-Token", token).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());

		logger.info("access user through access token passed via query string");
		response = Unirest
				.get(baseUrl + "/user")
				.queryString("token", token).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());


		logger.info("admin can delete carlos");
		response = Unirest
				.delete(baseUrl + "/user/" + username)
				.header("X-Auth-Token", token).asString();


		assertTrue(response.getStatus() == HttpStatus.NO_CONTENT.value());

		response = Unirest
				.get(baseUrl + "/user")
				.header("X-Auth-Token", token).asString();
		assertTrue(response.getStatus() == HttpStatus.OK.value());

	}
	
	@After
	public void after(){
		context.stop();
	}
}