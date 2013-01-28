package cz.cvut.fel.service;

import cz.cvut.fel.model.Flight;
import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.util.AuthorizedTest;
import cz.cvut.fel.util.UpdateServiceClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static cz.cvut.fel.utils.DateUtils.date;
import static org.testng.Assert.*;

/** @author Karel Cemus */
public class UpdateServiceTest extends AuthorizedTest {

    /** The path of the WSDL endpoint in relation to the deployed web application. */
    private static final String WSDL_PATH_LOCAL = "http://localhost:8080/FlightSystem/ws/update?wsdl";

    /** remote OpenShift URL */
    private static final String WSDL_PATH_REMOTE = "http://flightsystem-ctu.rhcloud.com/FlightSystem/ws/update?wsdl";

    private static final boolean TEST_LOCAL = true;

    /** used flight number */
    private static final String FLIGHT = "F987687";

    /** used WS client to test the service */
    private UpdateServiceClient client;

    @Inject
    private FlightService service;

    private Date backupDeparture;

    private Date backupArrival;

    private FlightStatus backupStatus;

    @BeforeMethod( groups = { "user-admin" } )
    public void setUpData() throws Exception {
        if ( isInContainer() ) {

            try {
                // create WS client
                client = new UpdateServiceClient( new URL( TEST_LOCAL ? WSDL_PATH_LOCAL : WSDL_PATH_REMOTE ) );
            } catch ( Exception ex ) {
                ex.printStackTrace();
                throw ex;
            }

            // backup data
            if ( TEST_LOCAL ) {
                Flight flight = service.find( FLIGHT );
                backupDeparture = flight.getDeparture().getActual();
                backupArrival = flight.getArrival().getActual();
                backupStatus = flight.getStatus();
            }
        }
    }

    @AfterMethod( groups = { "user-admin" } )
    public void cleanUpData() {
        if ( isInContainer() && TEST_LOCAL ) {

            // restore data
            Flight flight = service.find( FLIGHT );
            flight.getDeparture().setActual( backupDeparture );
            flight.getArrival().setActual( backupArrival );
            flight.setStatus( backupStatus );
            service.save( flight );
        }
    }

    @Test
    public void testUpdate_NotLoggedIn() {

        try {
            // perform test
            client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );
            fail( "Update is not supposed to pass." );
        } catch ( WebServiceException ex ) {
            Throwable inner = ex.getCause().getCause();
            assertNotNull( inner );
            assertTrue( inner.getMessage().contains( "[401] - Unauthorized" ) );
        }
    }

    @Test
    public void testUpdate_WrongPassword() {

        try {
            // perform test
            client.login( "petr", "wrong-password" );
            client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

            fail( "Update is not supposed to pass." );
        } catch ( WebServiceException ex ) {
            Throwable inner = ex.getCause().getCause();
            assertNotNull( inner );
            assertTrue( inner.getMessage().contains( "[401] - Unauthorized" ) );
        }
    }


    @Test
    public void testUpdate_NotEvenAdminCan() {

        try {
            // perform test
            client.login( "karel", "cemus" );
            client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

            fail( "Update is not supposed to pass." );
        } catch ( WebServiceException ex ) {
            Throwable inner = ex.getCause().getCause();
            assertNotNull( inner );
            assertTrue( inner.getMessage().contains( "[403] - Forbidden" ) );
        }
    }

    @Test
    public void testUpdate_NotInRole() {

        try {
            // perform test
            client.login( "lubos", "matl" );
            client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

            fail( "Update is not supposed to pass." );
        } catch ( WebServiceException ex ) {
            Throwable inner = ex.getCause().getCause();
            assertNotNull( inner );
            assertTrue( inner.getMessage().contains( "[403] - Forbidden" ) );
        }
    }

    @Test( groups = { "user-admin" } )
    public void testUpdate() {

        // perform test
        client.login( "petr", "praus" );
        client.update( FLIGHT, date( 1, 2, 2014, 10, 20 ), date( 1, 2, 2014, 13, 20 ), FlightStatus.DELAYED );

        // verify results if the service is accessible
        if ( TEST_LOCAL ) {
            Flight flight = service.find( FLIGHT );
            assertEquals( flight.getDeparture().getActual(), date( 1, 2, 2014, 10, 20 ) );
            assertEquals( flight.getArrival().getActual(), date( 1, 2, 2014, 13, 20 ) );
            assertEquals( flight.getStatus(), FlightStatus.DELAYED );
        }
    }

}
