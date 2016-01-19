package eu.freme.bservices.controller.pipeliningcontroller;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.bservices.testhelper.AuthenticatedTestHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 19.01.2016.
 */
public class PipeliningControllerTest {
    private Logger logger = Logger.getLogger(PipeliningControllerTest.class);
    AuthenticatedTestHelper ath;

    public PipeliningControllerTest() throws UnirestException {
        ApplicationContext context = IntegrationTestSetup.getContext("pipelining-controller-test-package.xml");
        ath = context.getBean(AuthenticatedTestHelper.class);
        ath.authenticateUsers();
    }

    @Test
    public void testPipelining(){

    }

    @Test
    public void testPipelineManagament(){
        
    }


}
