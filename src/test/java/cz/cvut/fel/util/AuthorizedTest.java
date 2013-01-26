package cz.cvut.fel.util;

import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

/** @author Lubos Matl */
public class AuthorizedTest extends ArquillianTest {

    @BeforeGroups( groups = "user-admin" )
    public void loginFlightManager() throws Exception {
        login( "karel", "cemus" );
    }

    @BeforeGroups( groups = "user-flight-manager" )
    public void loginAdmin() throws Exception {
        login( "lubos", "matl" );
    }

    @AfterGroups( groups = { "user-flight-manager", "user-admin" } )
    public void logout() throws Exception {
        final SecurityClient securityClient = SecurityClientFactory.getSecurityClient();
        securityClient.logout();
    }

    private void login( String username, String password ) throws Exception {
        final SecurityClient securityClient = SecurityClientFactory.getSecurityClient();
        securityClient.setSimple( username, password );
        securityClient.login();
    }

}
