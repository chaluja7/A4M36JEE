package cz.cvut.fel.util;

import cz.cvut.fel.utils.FlightSystemDatabase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

/**
 * <p>Parent class for all arquillian tests. It takes care of proper test package initialization.</p>
 *
 * @author Karel Cemus
 */
public class ArquillianTest extends Arquillian {

    /** determines whether the class is run inside of the container. suppose true */
    private static boolean inContainer = true;

    @Inject
    @FlightSystemDatabase
    protected EntityManager em;

    @Resource
    protected UserTransaction transaction;

    private static Archive<?> deployment;

    @Deployment
    public static Archive<?> getDeployment() {

        // create package and cache it
        if ( deployment == null ) {

            // deployment is run locally
            inContainer = false;

            deployment = ShrinkWrap.create( WebArchive.class, "web.war" )
                    // definition of persistence unit used in tests
                    .addAsResource( "test-persistence.xml", "META-INF/persistence.xml" )
                            // define the data-source with testing database
                    .addAsWebInfResource( "jboss-test-ds.xml", "jboss-test-ds.xml" )
                            // define the web-service configuration and security for server
                    .addAsWebInfResource( "jboss-wsse-server.xml", "jboss-wsse-server.xml" )
                            // define the web-service configration and security for client
                    .addAsResource( "jboss-wsse-client.xml", "jboss-wsse-client.xml" )
                            // set security domain description
                    .addAsWebInfResource( "jboss-web-test.xml", "jboss-web.xml" )
                            // define the unnecessary configuration
                    .addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
                            // logging configuration
                    .addAsResource( "log4j.xml", "log4j.xml" )
                            // describe required core dependencies
                    .addAsManifestResource( "MANIFEST.MF" )
                            // add all classes in the project
                    .addPackages( true, "cz.cvut.fel" );
        }
        return deployment;
    }

    protected static boolean isInContainer() {
        return inContainer;
    }
}
