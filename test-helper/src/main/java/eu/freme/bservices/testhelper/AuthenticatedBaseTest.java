package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import eu.freme.common.rest.BaseRestController;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 12.01.2016.
 */
@Component
public class AuthenticatedBaseTest extends BaseTest {
    private static String tokenWithPermission;
    private static String tokenWithOutPermission;
    private static String tokenAdmin;

    private static boolean authenticated = false;
    private static boolean authenticatedRemoved = false;

    private final String usernameWithPermission = "userwithpermission";
    private final String passwordWithPermission = "testpassword";
    private final String usernameWithoutPermission = "userwithoutpermission";
    private final String passwordWithoutPermission = "testpassword";


    @Before
    public void setup() throws UnirestException {
        super.setup();
        authenticateUsers();
    }


    /**
     * This method creates and authenticates two users, userwithpermission and userwithoutpermission.
     * Furthermore the admin token is created.
     *
     * @throws UnirestException
     */
    public void authenticateUsers() throws UnirestException {
        if (!authenticated) {
            //Creates two users, one intended to have permission, the other not
            createUser(usernameWithPermission, passwordWithPermission);
            tokenWithPermission = authenticateUser(usernameWithPermission, passwordWithPermission);
            createUser(usernameWithoutPermission, passwordWithoutPermission);
            tokenWithOutPermission = authenticateUser(usernameWithoutPermission, passwordWithoutPermission);
            //ConfigurableApplicationContext context = IntegrationTestSetup.getApplicationContext();
            tokenAdmin = authenticateUser(getAdminUsername(), getAdminPassword());
            authenticated = true;
        }
    }

    /**
     * This method deletes the users created for authentication purposes.
     *
     * @throws UnirestException
     */
    public void removeAuthenticatedUsers() throws UnirestException {
        if (!authenticatedRemoved) {
            deleteUser(usernameWithPermission, tokenWithPermission);
            deleteUser(usernameWithoutPermission, tokenWithOutPermission);
            authenticatedRemoved = true;
        }
    }

    /**
     * Use this method to add an authentication header to the request.
     * If the given token is null, the request will not be modified.
     *
     * @param request The request to add the authentication
     * @param token   The authentication Token
     * @param <T>
     * @return The modified request
     */
    @SuppressWarnings("unchecked")
    private <T extends HttpRequest> T addAuthentication(T request, String token) {
        if (token == null)
            return request;
        return (T) request.header("X-Auth-Token", token);
    }

    /**
     * Add authentication for the user intended to have permission to the given request.
     *
     * @param request the request to modify
     * @return the modified request
     */
    public <T extends HttpRequest> T addAuthentication(T request) {
        return addAuthentication(request, tokenWithPermission);
    }

    /**
     * Add authentication for the user intended to have no permission to the given request.
     *
     * @param request the request to modify
     * @return the modified request
     */
    public <T extends HttpRequest> T addAuthenticationWithoutPermission(T request) {
        return addAuthentication(request, tokenWithOutPermission);
    }

    /**
     * Add authentication for the admin user to the given request.
     *
     * @param request the request to modify
     * @return the modified request
     */
    public <T extends HttpRequest> T addAuthenticationWithAdmin(T request) {
        return addAuthentication(request, tokenAdmin);
    }


    public void createUser(String username, String password) throws UnirestException {
        logger.info("create user: " + username);
        HttpResponse<String> response = Unirest.post(getAPIBaseUrl() + "/user")
                .queryString("username", username)
                .queryString("password", password).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
    }

    public void deleteUser(String username, String token) throws UnirestException {
        logger.info("delete user: " + username);
        HttpResponse<String> response = addAuthentication(Unirest.delete(getAPIBaseUrl() + "/user/" + username), token).asString();
        assertTrue(response.getStatus() == HttpStatus.NO_CONTENT.value());
    }

    public String authenticateUser(String username, String password) throws UnirestException {
        HttpResponse<String> response;

        logger.info("login with new user / create token");
        response = Unirest
                .post(getAPIBaseUrl() + BaseRestController.authenticationEndpoint)
                .header("X-Auth-Username", username)
                .header("X-Auth-Password", password).asString();
        assertTrue(response.getStatus() == HttpStatus.OK.value());
        String token = new JSONObject(response.getBody()).getString("token");
        return token;
    }
}
