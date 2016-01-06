package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.rest.BaseRestController;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import eu.freme.bservices.testhelper.TestHelper;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 06.01.2016.
 */

public class AuthenticatedTestHelper extends TestHelper{

    public void createUser(String username, String password) throws UnirestException {

        HttpResponse<String> response = Unirest.post(getAPIBaseUrl() + "/user")
                .queryString("username", username)
                .queryString("password", password).asString();
        logger.debug("STATUS: " + response.getStatus());
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    public String authenticateUser(String username, String password) throws UnirestException{
        HttpResponse<String> response;

        logger.info("login with new user / create token");
        response = Unirest
                .post(getAPIBaseUrl()  + BaseRestController.authenticationEndpoint)
                .header("X-Auth-Username", username)
                .header("X-Auth-Password", password).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
        String token = new JSONObject(response.getBody()).getString("token");
        return token;
    }
}
