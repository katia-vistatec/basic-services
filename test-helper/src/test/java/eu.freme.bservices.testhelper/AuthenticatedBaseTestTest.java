package eu.freme.bservices.testhelper;

import org.junit.Test;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 12.01.2016.
 */
public class AuthenticatedBaseTestTest extends AuthenticatedBaseTest{

    public AuthenticatedBaseTestTest(){
        init(AuthenticatedBaseTestTest.class, "authenticated-base-test-test-package.xml");
    }

    @Test
    public void test(){
        logger.info("TEST DUMMY");
    }
}
