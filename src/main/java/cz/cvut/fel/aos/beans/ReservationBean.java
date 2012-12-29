package cz.cvut.fel.aos.beans;

import cz.cvut.fel.aos.model.Reservation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/** @author Karel Cemus */
@Slf4j
@Named
@Getter
@RequestScoped
public class ReservationBean implements Serializable {

    //    @Inject
    //    private FacadeService service;

    @Inject
    private SessionBean session;

    @Inject
    private FileBean file;

    private Reservation reservation;

    public Reservation getReservation() {
        if ( reservation == null ) {
            reservation = find();
        }
        return reservation;
    }

    public Reservation find() {
        //        try {
        //            if ( session.getIdentifier() == 0 ) return null;
        //            return service.findReservation( session.getIdentifier(), session.getPassword() );
        //        } catch ( SecurityException e ) {
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Heslo pro přístup k rezervaci není správné" ) );
        return null;
        //        }
    }

    public String createReservation( String flightNumber ) {
        //        try {
        //            int count = Integer.parseInt( FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get( flightNumber + "-count" ) );
        //            session.setPassword( generatePassword() );
        //
        //            SuccessfulReservation success = service.createReservation( flightNumber, session.getPassword(), count );
        //            session.setIdentifier( success.getReservation().getId() );
        //            file.setFile( "Stáhnout potvrzení o rezervaci. OBSAHUJE HESLO pro přístup.", "confirmation.txt", success.getConfirmation() );
        //
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Rezervace byla vytvořena." ) );
        //
        //            return "reservation";
        //        } catch ( FullFlightException e ) {
        //            session.setIdentifier( 0 );
        //            session.setPassword( null );
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Let je již plně obsazen." ) );
        return null;
        //        }
    }


    public String cancel() {
        //        try {
        //            DataHandler handler = service.cancelReservation( session.getIdentifier(), session.getPassword() );
        //            file.setFile( "Stáhnout potvrzení o zrušení rezervace", "cancel-confirmation.txt", handler );
        //
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Rezervace byla zrušena." ) );
        //
        //        } catch ( NoSuchReservationException e ) {
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Rezervace nebyla nalezena." ) );
        //        } catch ( SecurityException e ) {
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Přístup k rezervaci nebyl povolen. Zkontrolujte heslo a opakujte akci později." ) );
        //        }
        return "reservation";
    }

    public void printETicket() {
        //        try {
        //            DataHandler handler = service.printTicket( session.getIdentifier(), session.getPassword() );
        //            file.download( "eticket.txt", handler );
        //
        //        } catch ( NoSuchReservationException e ) {
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Rezervace nebyla nalezena." ) );
        //        } catch ( SecurityException e ) {
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Přístup k rezervaci nebyl povolen. Zkontrolujte heslo a opakujte akci později." ) );
        //        } catch ( ReservationNotPaidException e ) {
        //            FacesContext.getCurrentInstance().addMessage( null, new FacesMessage( "Rezervace ještě není zaplacena, nelze vytisknout platnou letenku." ) );
        //        }
    }

    private String generatePassword() {
        StringBuilder builder = new StringBuilder( 10 );
        for ( int i = 0; i < 10; ++i ) {
            int code = ( int ) ( ( Math.random() * 100 ) % 26 );
            builder.append( ( char ) ( 'a' + code ) );
        }
        return builder.toString();
    }
}
