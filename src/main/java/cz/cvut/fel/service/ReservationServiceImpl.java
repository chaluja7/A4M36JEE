package cz.cvut.fel.service;

import cz.cvut.fel.exception.*;
import cz.cvut.fel.model.Flight;
import cz.cvut.fel.model.Reservation;
import cz.cvut.fel.utils.FlightSystemDatabase;
import cz.cvut.fel.utils.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;


/** @author Karel Cemus */
@Slf4j
@Stateless
public class ReservationServiceImpl implements ReservationService {

    @Inject
    @FlightSystemDatabase
    private EntityManager em;

    @Inject
    private SecurityProvider security;

    @Inject
    private FlightService flightService;

    @Inject
    private PrintService printService;

    @Override
    public Reservation find( long id, String password ) throws SecurityException {

        // verify code is set
        if ( id == 0 ) throw new IllegalArgumentException( "Reservation ID is required parameter." );

        // try to find active entity with the code
        Reservation reservation = em.find( Reservation.class, id );

        if ( reservation == null ) {
            throw new NoSuchReservationException( String.format( "Reservation with ID '%d' doesn't exist.", id ) );
        }

        // verify access, if denied SecurityException will be thrown
        security.verifyAccess( id, reservation.getPassword(), password );

        // verification passed, access permitted
        return reservation;
    }

    @Override
    public Reservation create( String flightNumber, String password, int seats ) throws FullFlightException, NoSuchFlightException {

        if ( flightNumber == null ) { // missing input parameter
            throw new IllegalArgumentException( "Flight number is required parameter." );
        }
        Flight flight = flightService.find( flightNumber );

        if ( password == null || password.isEmpty() ) { // missing input parameter
            throw new IllegalArgumentException( "Password is missing. Reservation has to be password protected." );
        }

        if ( seats <= 0 ) {
            throw new IllegalArgumentException( "Requested number of seats has to be at least 1." );
        }

        if ( flight.getCapacityLeft() < seats ) { // not enough seats left
            throw new FullFlightException( String.format( "Flight '%s' doesn't have enough capacity left.", flight.getNumber() ) );
        }

        // update flight remaining capacity information
        flight.setSeatsTaken( flight.getSeatsTaken() + seats );

        // make a reservation
        Reservation entity = new Reservation();
        entity.setFlight( flight );
        entity.setCount( seats );
        entity.setPaid( 0 );
        entity.setCost( flight.getCost() * seats );
        entity.setCancelled( false );
        em.persist( entity );

        // set salted password
        entity.setPassword( security.hash( entity.getId(), password ) );

        log.info( "Reservation with ID '{}' was successfully created on '{}' seats in flight '{}'.", new Object[]{ entity.getId(), seats, flightNumber } );

        return entity;
    }

    @Override
    public Reservation cancel( long id, String password ) throws SecurityException {

        // find reservation
        Reservation reservation = find( id, password );

        // if already cancelled, do nothing
        if ( reservation.isCancelled() ) return reservation;

        // cancel reservation
        reservation.setCancelled( true );

        // release taken seats in chosen flight
        Flight flight = reservation.getFlight();
        flight.setSeatsTaken( flight.getSeatsTaken() - reservation.getCount() );

        log.info( "Reservation with ID '{}' was successfully cancelled.", reservation.getId() );

        return reservation;
    }

    @Override
    public byte[] printReservationConfirmation( final long id, final String password ) throws ReservationCancelledException {

        // verify input parameters and try to find reservation including access verification
        Reservation reservation = find( id, password );

        if ( !reservation.isCancelled() ) {
            return printService.createConfirmation( reservation, password );
        } else {
            throw new ReservationCancelledException( "Reservation is cancelled, confirmation cannot be printed." );
        }
    }

    @Override
    public byte[] printCancelConfirmation( final long id, final String password ) throws ReservationNotCancelledException {

        // verify input parameters and try to find reservation including access verification
        Reservation reservation = find( id, password );

        if ( reservation.isCancelled() ) {
            return printService.createCancelConfirmation( reservation );
        } else {
            throw new ReservationNotCancelledException( "Reservation is cancelled, confirmation cannot be printed." );
        }
    }

    @Override
    public byte[] printETicket( final long id, final String password ) throws ReservationCancelledException, ReservationNotPaidException {


        // verify input parameters and try to find reservation including access verification
        Reservation reservation = find( id, password );

        if ( reservation.isCancelled() ) {
            throw new ReservationCancelledException( "Reservation is cancelled, e-ticket cannot be printed." );
        } else if ( reservation.getPaid() < reservation.getCost() ) {
            throw new ReservationNotPaidException( "Reservation is not fully paid, e-ticket cannot be printed." );
        } else {
            return printService.createTicket( reservation );
        }
    }
}
