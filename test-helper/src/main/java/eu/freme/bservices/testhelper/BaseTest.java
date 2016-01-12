package eu.freme.bservices.testhelper;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.common.starter.FREMEStarter;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 12.01.2016.
 */
public class BaseTest {
    private ConfigurableApplicationContext context;
    private String packeFN;

    protected Logger logger;

    public void init(Class clazz, String xmlPackageFilename){
        logger = Logger.getLogger(clazz);
        packeFN = xmlPackageFilename;
    }

    @Before
    public void setup() throws UnirestException {
        context = FREMEStarter.startPackageFromClasspath(packeFN);
    }

    @After
    public void after() throws UnirestException {
        context.stop();
        logger.info("test successful");
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }

    /**
     * Returns the base url of the API given the spring application context, e.g. http://localhost:8080
     * @return
     */
    public String getAPIBaseUrl(){
        String port = context.getEnvironment().getProperty("server.port");
        if( port == null){
            port = "8080";
        }
        return "http://localhost:" + port;
    }

    /**
     * Return the username of the administrator user of the REST API.
     *
     * @return
     */
    public String getAdminUsername(){
        return context.getEnvironment().getProperty("admin.username");
    }

    /**
     * Return the password of the administrator user of the REST API.
     *
     * @return
     */
    public String getAdminPassword(){
        return context.getEnvironment().getProperty("admin.password");
    }
}
