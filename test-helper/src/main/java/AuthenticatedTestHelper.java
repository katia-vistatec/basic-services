package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import eu.freme.common.rest.BaseRestController;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import eu.freme.bservices.testhelper.TestHelper;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 06.01.2016.
 */

public class AuthenticatedTestHelper extends TestHelper{

    private static String tokenWithPermission;
    private static String tokenWithOutPermission;
    private static String tokenAdmin;

    private static boolean authenticated = false;

    protected final String usernameWithPermission = "userwithpermission";
    protected final String passwordWithPermission = "testpassword";
    protected final String usernameWithoutPermission = "userwithoutpermission";
    protected final String passwordWithoutPermission = "testpassword";

    @Before
    public void setup()throws UnirestException{
        authenticateUsers();
    }

    @After
    public void exit() throws UnirestException{
        deleteUser(usernameWithPermission, tokenWithPermission);
        deleteUser(usernameWithoutPermission, tokenWithOutPermission);
    }

    public void createUser(String username, String password) throws UnirestException {
        HttpResponse<String> response = Unirest.post(getAPIBaseUrl() + "/user")
                .queryString("username", username)
                .queryString("password", password).asString();
        logger.debug("STATUS: " + response.getStatus());
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    public void deleteUser(String username, String token) throws UnirestException{
        HttpResponse<String> response = addAuthentication(Unirest.delete(getAPIBaseUrl() + "/user/"+username), token).asString();
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

    /**
     * This method creates and authenticats two users, userwithpermission and userwithoutpermission.
     * Furthermore the admin token is created.
     * @throws UnirestException
     */
    private void authenticateUsers() throws UnirestException {
        //Creates two users, one intended to have permission, the other not
        createUser(usernameWithPermission, passwordWithPermission);
        tokenWithPermission = authenticateUser(usernameWithPermission, passwordWithPermission);
        createUser(usernameWithoutPermission, passwordWithoutPermission);
        tokenWithOutPermission = authenticateUser(usernameWithoutPermission, passwordWithoutPermission);
        //ConfigurableApplicationContext context = IntegrationTestSetup.getApplicationContext();
        tokenAdmin = authenticateUser(getAdminUsername(), getAdminPassword());
        authenticated = true;
    }

    /**
     * Use this method to add an authentication header to the request.
     * If the given token is null, the request will not be modified.
     * @param request The request to add the authentication
     * @param token The authentication Token
     * @param <T>
     * @return The modified request
     */
    @SuppressWarnings("unchecked")
    protected <T extends HttpRequest> T addAuthentication(T request, String token){
        if(token==null)
            return request;
        return (T)request.header("X-Auth-Token", token);
    }

    protected <T extends HttpRequest> T addAuthentication(T request){
        return addAuthentication(request, tokenWithPermission);
    }

    protected <T extends HttpRequest> T addAuthenticationWithoutPermission(T request){
        return addAuthentication(request, tokenWithOutPermission);
    }
}
