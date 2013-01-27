package cz.cvut.fel.service;

import cz.cvut.fel.exception.InvalidPaymentException;
import cz.cvut.fel.exception.NoSuchReservationException;
import cz.cvut.fel.model.Payment;
import cz.cvut.fel.model.Reservation;
import cz.cvut.fel.utils.FlightSystemDatabase;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;


/** @author Karel Cemus */
@Slf4j
@Stateless
public class PaymentServiceImpl implements PaymentService {

    @Inject
    @FlightSystemDatabase
    private EntityManager em;

    @Inject
    private ReservationService reservationService;

    @Inject
    private PrintService printService;

    @Override
    public List<Payment> findPayments( final long reservationId, final String password ) throws SecurityException, NoSuchReservationException {
        // load reservation from which money should be transferred
        Reservation reservation = reservationService.find( reservationId, password );

        // load all attached payments if reservation exists
        reservation.getPayments().size();
        return reservation.getPayments();
    }

    @Override
    public Payment payVisa( final long reservationId, final String cardName, final long creditCard, final Date validUntil, final int verificationCode ) throws InvalidPaymentException, NoSuchReservationException {

        // load paid reservation
        Reservation reservation = em.find( Reservation.class, reservationId );
        if ( reservation == null ) {
            throw new NoSuchReservationException( String.format( "Reservation with id '%1$d' doesn't exist.", reservationId ) );
        }

        if ( reservation.isCancelled() ) { // cancelled reservation cannot be paid
            log.info( "Rejected credit card '{}' bound to '{}'. Reservation is cancelled.", creditCard, cardName );
            throw new InvalidPaymentException( "Reservation is cancelled." );
        }

        if ( creditCard % 1000 != verificationCode ) { // security check of verification code, it must match last 3 digits in card number
            log.info( "Rejected credit card '{}' bound to '{}'. Verification code is wrong.", creditCard, cardName );
            throw new InvalidPaymentException( "Verification code is wrong." );
        }

        if ( validUntil.getTime() < new Date().getTime() ) { // check of card expiration date
            log.info( "Rejected credit card '{}' bound to '{}'. Card expired.", creditCard, cardName );
            throw new InvalidPaymentException( "Credit card expired." );
        }

        // calculate the amount to pay as money left to reservation be fully paid
        int amount = Math.max( reservation.getCost() - reservation.getPaid(), 0 );

        if ( amount == 0 ) { // prevent paying already paid reservations
            log.info( "Payment for reservation with ID '{}' was ignored, reservation is already paid.", reservationId );
            throw new InvalidPaymentException( "Payment request was ignored, reservation is already paid." );
        }

        Payment payment = new Payment();
        payment.setCreditCardName( cardName );
        payment.setCreditCardNumber( creditCard % 10000 );
        payment.setReservation( reservation );
        payment.setTimestamp( new Date() );
        payment.setAmount( amount );
        em.persist( payment );

        // log credit card acceptance
        log.info( "Accepted credit card '{}' bound to '{}'. There was transferred '{}' CZK to reservation '{}'.", new Object[]{ creditCard, cardName, amount, reservationId } );

        // make transaction
        reservation.setPaid( reservation.getPaid() + amount );

        // log made changes
        log.info( "Reservation with ID '{}' received '{}' CZK. There is '{}' CZK.", new Object[]{ reservation.getId(), amount, reservation.getPaid() } );

        return payment;
    }

    @Override
    public Payment returnMoney( final long reservationId, final String password, final long account, final int bank ) throws SecurityException, InvalidPaymentException, NoSuchReservationException {

        // load reservation from which money should be transferred
        Reservation reservation = reservationService.find( reservationId, password );

        if ( !reservation.isCancelled() ) { // money can be transferred only from cancelled reservations
            log.info( "Money transaction from reservation with ID '{}' was rejected due to the reservation is not cancelled.", reservationId );
            throw new InvalidPaymentException( String.format( "Money transaction from reservation with ID '%d' was rejected due to the reservation is not cancelled.", reservationId ) );
        }

        if ( reservation.getPaid() == 0 ) { // transaction can be realised only when there are some money left
            log.info( "Money transaction from reservation with ID '{}' was rejected due to source account is empty.", reservationId );
            throw new InvalidPaymentException( String.format( "Money transaction from reservation with ID '%d' was rejected due to account is empty.", reservationId ) );
        }

        // can be transferred only as much money as is present
        int amount = Math.max( reservation.getPaid(), 0 );

        Payment payment = new Payment();
        payment.setCreditCardName( "Payment back to customer" );
        payment.setAccountNumber( account );
        payment.setBankCode( bank );
        payment.setReservation( reservation );
        payment.setTimestamp( new Date() );
        payment.setAmount( -amount );
        em.persist( payment );

        // log transaction acceptance and payment proceeding
        log.info( "Accepted credit transfer from reservation ID '{}' back to bank account '{}' int bank '{}'. There was transferred '{}' CZK.", new Object[]{ reservationId, account, bank, amount } );

        // make transaction
        reservation.setPaid( reservation.getPaid() - amount );

        log.info( "Reservation with ID '{}' sent '{}' CZK back to customer's bank account. There is '{}' CZK left.", new Object[]{ reservation.getId(), amount, reservation.getPaid() } );

        return payment;
    }

    @Override
    public byte[] printPaymentConfirmation( final long id ) {

        // try to find payment
        Payment payment = em.find( Payment.class, id );
        if ( payment == null ) throw new IllegalArgumentException( String.format( "Payment with ID '%1$d doesn't exist.", id ) );

        // if found, make confirmation
        return printService.createConfirmation( payment );
    }
}
