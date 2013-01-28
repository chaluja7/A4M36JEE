package cz.cvut.fel.service;

import cz.cvut.fel.model.Flight;
import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.util.AuthorizedTest;
import cz.cvut.fel.util.UpdateServiceClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static cz.cvut.fel.utils.DateUtils.date;
import static org.testng.Assert.*;

/** @author Karel Cemus */
public class UpdateServiceTest extends AuthorizedTest {

    /** The path of the WSDL endpoint in relation to the deployed web application. */
    private static final String WSDL_PATH = "http://localhost:8080/FlightSystem/ws/update?wsdl";

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
    public void setUpData() throws MalformedURLException {
        if ( isInContainer() ) {

            // create WS client
            client = new UpdateServiceClient( new URL( WSDL_PATH ) );

            // backup data
            Flight flight = service.find( FLIGHT );
            backupDeparture = flight.getDeparture().getActual();
            backupArrival = flight.getArrival().getActual();
            backupStatus = flight.getStatus();
        }
    }

    @AfterMethod( groups = { "user-admin" } )
    public void cleanUpData() {
        if ( isInContainer() ) {

            // restore data
            Flight flight = service.find( FLIGHT );
            flight.getDeparture().setActual( backupDeparture );
            flight.getArrival().setActual( backupArrival );
            flight.setStatus( backupStatus );
            service.save( flight );
        }
    }

    @Test( expectedExceptions = Exception.class )
    public void testUpdate_NotLoggedIn() {

        // perform test
        client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

        fail( "Update is not supposed to pass." );
    }

    @Test( expectedExceptions = Exception.class )
    public void testUpdate_WrongPassword() {

        // perform test
        client.login( "karel", "wrong-password" );
        client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

        fail( "Update is not supposed to pass." );
    }


    @Test( expectedExceptions = Exception.class )
    public void testUpdate_NotEvenAdminCan() {

        // perform test
        client.login( "karel", "cemus" );
        client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

        fail( "Update is not supposed to pass." );
    }

    @Test( expectedExceptions = Exception.class )
    public void testUpdate_NotInRole() {

        // perform test
        client.login( "lubos", "matl" );
        client.update( FLIGHT, date( 1, 2, 2013, 10, 20 ), date( 1, 2, 2013, 13, 20 ), FlightStatus.DELAYED );

        fail( "Update is not supposed to pass." );
    }

    @Test( groups = { "user-admin" } )
    public void testUpdate() {

        // perform test
        client.login( "petr", "praus" );
        client.update( FLIGHT, date( 1, 2, 2014, 10, 20 ), date( 1, 2, 2014, 13, 20 ), FlightStatus.DELAYED );

        // verify results
        Flight flight = service.find( FLIGHT );
        assertEquals( flight.getDeparture().getActual(), date( 1, 2, 2014, 10, 20 ) );
        assertEquals( flight.getArrival().getActual(), date( 1, 2, 2014, 13, 20 ) );
        assertEquals( flight.getStatus(), FlightStatus.DELAYED );
    }

}
