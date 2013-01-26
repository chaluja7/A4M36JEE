package cz.cvut.fel.service;

import cz.cvut.fel.exception.NoSuchFlightException;
import cz.cvut.fel.model.Flight;
import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.model.Time;
import cz.cvut.fel.util.AuthorizedTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.*;

import static cz.cvut.fel.util.ArquillianDataProvider.provide;
import static cz.cvut.fel.utils.DateUtils.date;
import static java.lang.Thread.sleep;
import static org.testng.Assert.*;

/** @author Karel Cemus */
@Test( groups = "services" )
public class FlightServiceTest extends AuthorizedTest {

    private static final String FLIGHT_NUMBER = "F111111";

    @Inject
    private FlightService service;

    @Inject
    private DestinationService destinationService;

    @AfterMethod
    public void cleanUp() throws Exception {
        if ( isInContainer() ) {
            transaction.begin();
            em.createQuery( "DELETE FROM Flight f WHERE f.number = 'F111111'" ).executeUpdate();
            transaction.commit();
        }
    }

    @Test( dataProvider = "findProvider", groups = "user-unlogged" )
    public void testFind( String number, boolean exists ) {
        try {
            service.find( number );
            if ( !exists ) fail( "Flight is supposed not to exist." );
        } catch ( javax.ejb.EJBException ignored ) {
            if ( exists ) {
                fail( "Flight is supposed to exist." );
            } else if ( !( ignored.getCause() instanceof NoSuchFlightException ) ) {
                fail( "It is supposed to throw NoSuchFlightException but throws " + ignored.getCause().getClass() );
            }
        }
    }

    @DataProvider
    public Object[][] findProvider() {
        return provide(
                "FlightTest#findProvider",
                new Object[][]{
                        new Object[]{ "F987987", true },
                        new Object[]{ "F987545", true },
                        new Object[]{ "F987981", true },
                        new Object[]{ "F987687", true },
                        new Object[]{ "F987972", true },
                        new Object[]{ "F987111", false },
                        new Object[]{ "F643354", false }
                } );
    }

    @Test( dataProvider = "flightsFromProvider", groups = "user-unlogged" )
    public void testFindFlightsFrom( Date from, Date to, String destination, String[] expected ) {

        Collection<Flight> flights = service.findFlightsFrom( from, to, destination );

        assertEquals( flights.size(), expected.length );
        assertFlights( flights, expected );
    }

    @DataProvider
    public Object[][] flightsFromProvider() {
        return provide(
                "FlightTest#flightsFromProvider",
                new Object[][]{
                        new Object[]{ date( 1, 1, 2012, 0, 0 ), date( 1, 1, 2012, 23, 59 ), "PRG", new String[]{ "F987545", "F987987" } },
                        new Object[]{ date( 1, 1, 2012, 8, 30 ), date( 2, 1, 2012, 23, 59 ), "VIE", new String[]{ "F987687", "F987981" } },
                        new Object[]{ date( 3, 1, 2012, 0, 0 ), date( 3, 1, 2012, 23, 59 ), "INN", new String[]{ "F987972" } }
                } );
    }

    @Test( dataProvider = "flightsToProvider", groups = "user-unlogged" )
    public void testFindFlightsTo( Date from, Date to, String destination, String[] expected ) {

        Collection<Flight> flights = service.findFlightsTo( from, to, destination );

        assertEquals( flights.size(), expected.length );
        assertFlights( flights, expected );
    }

    @DataProvider
    public Object[][] flightsToProvider() {
        return provide(
                "FlightTest#flightsToProvider",
                new Object[][]{
                        new Object[]{ date( 1, 1, 2012, 0, 0 ), date( 1, 1, 2012, 23, 59 ), "PRG", new String[]{ "F987687" } },
                        new Object[]{ date( 1, 1, 2012, 8, 30 ), date( 2, 1, 2012, 23, 59 ), "MAD", new String[]{ "F987545", "F987126", "F987358" } },
                        new Object[]{ date( 3, 1, 2012, 0, 0 ), date( 3, 1, 2012, 23, 59 ), "LHR", new String[]{ "F987963" } }
                } );
    }

    @Test( dataProvider = "flightsProvider", groups = "user-unlogged" )
    public void testFindFlights( Date from, Date to, String destinationFrom, String destinationTo, String[] expected ) {
        Collection<Flight> flights = service.findFlights( from, to, destinationFrom, destinationTo );

        assertEquals( flights.size(), expected.length );
        assertFlights( flights, expected );
    }

    @DataProvider
    public Object[][] flightsProvider() {
        return provide(
                "FlightTest#flightsProvider",
                new Object[][]{
                        new Object[]{ date( 1, 1, 2012, 0, 0 ), date( 1, 1, 2012, 23, 59 ), "PRG", "LHR", new String[]{ "F987987" } },
                        new Object[]{ date( 1, 1, 2012, 8, 30 ), date( 3, 1, 2012, 23, 59 ), "PRG", "MAD", new String[]{ "F987545", "F987126", "F987358" } },
                        new Object[]{ date( 3, 1, 2012, 0, 0 ), date( 3, 1, 2012, 23, 59 ), "PRG", "LHR", new String[]{ "F987963" } }
                } );
    }

    @Test( groups = "user-admin" )
    public void testCreateAdmin() {
        testCreate();
    }

    @Test( groups = "user-flight-manager" )
    public void testCreateFlightManager() {
        testCreate();
    }

    @Test( groups = "user-unlogged", expectedExceptions = javax.ejb.EJBAccessException.class )
    public void testCreateAllUsers() {
        testCreate();
    }

    @Test( enabled = false )
    public void testCreate() {
        Flight flight = createFlight();

        // perform test
        flight = service.save( flight );

        // verify results
        assertTrue( flight.getId() > 0 );
        assertNotNull( service.find( FLIGHT_NUMBER ) );
    }

    @Test( groups = "user-admin" )
    public void testCreateInvalid() {
        try {
            Flight flight = createFlight();
            flight.setNumber( "F21456" ); // number is too short

            // perform test
            service.save( flight );
            fail( "Instance is invalid, it supposed to throw validation exception." );
        } catch ( EJBException ex ) {
            if ( !( ex.getCause() instanceof ConstraintViolationException ) ) {
                fail( "Expected validation exception." );
            }
        }
    }

    @Test( dependsOnMethods = "testCreateAdmin", groups = { "user-admin" } )
    public void testUpdate() {

        // create test instance
        testCreate();

        // update model
        Flight flight = service.find( FLIGHT_NUMBER );
        em.detach( flight );

        // verify assumption
        assertEquals( flight.getStatus(), FlightStatus.SCHEDULED );

        // perform test
        flight.setStatus( FlightStatus.LANDED );
        service.save( flight );

        // verify results
        flight = service.find( FLIGHT_NUMBER );
        assertEquals( flight.getStatus(), FlightStatus.LANDED );
    }

    @Test( dependsOnMethods = "testCreateAdmin", groups = "user-admin", expectedExceptions = javax.ejb.EJBException.class )
    public void testDeleteAdmin() throws InterruptedException {
        testDelete();
    }

    @Test( groups = "user-flight-manager", expectedExceptions = javax.ejb.EJBException.class )
    public void testDeleteFlightManager() throws InterruptedException {
        service.delete( "1" );
    }

    @Test( groups = "user-unlogged", expectedExceptions = javax.ejb.EJBAccessException.class )
    public void testDeleteAllUsers() throws InterruptedException {
        service.delete( "1" );
    }

    @Test( enabled = false )
    public void testDelete() throws InterruptedException {

        // create test instance
        testCreate();

        Flight flight = service.find( FLIGHT_NUMBER );

        // perform test
        service.delete( flight.getNumber() );

        // wait for new timestamp
        sleep( 1000 );

        // verify results, it is supposed not to exist - it should throw an exception
        service.find( FLIGHT_NUMBER );
    }

    private void assertFlights( Collection<Flight> flights, String... model ) {
        // convert values to set
        Set<String> modelSet = new HashSet<String>( Arrays.asList( model ) );

        // extract flight numbers
        Set<String> codes = new HashSet<String>();
        for ( Flight Flight : flights ) {
            codes.add( Flight.getNumber() );
        }

        assertEquals( codes, modelSet );
    }

    private Flight createFlight() {
        Flight flight = new Flight();
        flight.setNumber( FLIGHT_NUMBER );
        flight.setArrival( new Time( date( 1, 1, 2012, 10, 20 ) ) );
        flight.setDeparture( new Time( date( 1, 1, 2012, 14, 20 ) ) );
        flight.setFrom( destinationService.findByCode( "PRG" ) );
        flight.setTo( destinationService.findByCode( "MAD" ) );
        flight.setCapacity( 100 );
        flight.setSeatsTaken( 0 );
        flight.setCost( 5000 );
        flight.setStatus( FlightStatus.SCHEDULED );

        // set validity for couple minutes, after that the instance gets invalid
        Calendar calendar = Calendar.getInstance();
        calendar.add( Calendar.MINUTE, 2 );
        flight.setValidUntil( calendar.getTime() );

        return flight;
    }
}
