package cz.cvut.fel.aos.service;

import cz.cvut.fel.aos.model.Flight;
import cz.cvut.fel.aos.model.Reservation;
import cz.cvut.fel.aos.service.reservation.FullFlightException;
import cz.cvut.fel.aos.service.reservation.ReservationService;
import cz.cvut.fel.aos.service.reservation.SecurityException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** @author Karel Cemus */
@Test( enabled = false )
public class ReservationTest {

    private static final String PASSWORD = "heslo12345";

    ReservationService service;

    private long reservationId;

    @BeforeTest
    public void setUp() {
        //        ReservationServiceImplService factory = new ReservationServiceImplService( ReservationServiceImplService.WSDL_LOCATION, ReservationServiceImplService.SERVICE );
        //        service = factory.getReservationServiceImplPort();
    }


    public void testCreate() throws FullFlightException {

        Reservation reservation = service.create( "F987545", PASSWORD, 5 );

        Assert.assertEquals( reservation.getCount(), 5 );
        Assert.assertEquals( reservation.getPaid(), 0 );
        Assert.assertEquals( reservation.getFlight().getNumber(), "F987545" );
        Assert.assertEquals( reservation.getCost(), 5000 * 5 );
        assertNotEquals( reservation.getPassword(), "heslo12345" );
        assertNotNull( reservation.getId() );

        // set up of another tests
        reservationId = reservation.getId();
    }

    @Test( dependsOnMethods = "testCreate" )
    public void testFind() throws SecurityException {
        Reservation reservation = service.find( reservationId, PASSWORD );

        assertNotNull( reservation.getId() );
        Assert.assertEquals( reservation.getCount(), 5 );
        Assert.assertEquals( reservation.getFlight().getNumber(), "F987545" );
        Assert.assertEquals( reservation.getCost(), 5000 * 5 );
    }

    @Test( dependsOnMethods = "testCreate", expectedExceptions = SecurityException.class )
    public void testFindWithException() throws SecurityException {
        service.find( reservationId, "wrongPassword" );
        fail( "Expected exception" );
    }

    @Test( dependsOnMethods = { "testFind", "testFindWithException" } )
    public void testCancel() throws SecurityException {
        Reservation reservation = service.find( reservationId, PASSWORD );
        Flight flight = reservation.getFlight();
        int freeSpace = flight.getCapacityLeft();

        // verify assumption
        assertFalse( reservation.isCanceled() );

        // execute && verify
        assertTrue( service.cancel( reservationId, PASSWORD ) );

        // verify freed capacity in the flight
        Reservation canceled = service.find( reservationId, PASSWORD );
        Flight updatedFlight = canceled.getFlight();

        Assert.assertEquals( updatedFlight.getCapacityLeft(), freeSpace + reservation.getCount() );
    }

    @Test( dependsOnMethods = "testCancel" )
    public void testCancelAlreadyCanceled() throws SecurityException {
        Reservation reservation = service.find( reservationId, PASSWORD );
        Flight flight = reservation.getFlight();
        int freeSpace = flight.getCapacityLeft();

        // verify assumption
        assertTrue( reservation.isCanceled() );

        // execute && verify
        assertTrue( service.cancel( reservationId, PASSWORD ) );

        // verify freed capacity in the flight
        Reservation canceled = service.find( reservationId, PASSWORD );
        Flight updatedFlight = canceled.getFlight();

        Assert.assertEquals( updatedFlight.getCapacityLeft(), freeSpace );
    }

}