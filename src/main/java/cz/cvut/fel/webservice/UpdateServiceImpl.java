package cz.cvut.fel.webservice;

import cz.cvut.fel.exception.NoSuchFlightException;
import cz.cvut.fel.model.Flight;
import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.service.FlightService;
import org.jboss.ws.api.annotation.EndpointConfig;

import javax.inject.Inject;
import javax.jws.WebService;
import java.util.Date;

/** @author Karel Cemus */
@WebService(
        serviceName = "UpdateService", portName = "Update", name = "Update",
        endpointInterface = "cz.cvut.fel.webservice.UpdateService",
        targetNamespace = "http://fel.cvut.cz/FlightSystem/Update"
)
@EndpointConfig( configName = "Standard WSSecurity Endpoint" )
public class UpdateServiceImpl implements UpdateService {

    @Inject
    private FlightService service;

    public void update( String flightNumber, Date departure, Date arrival, FlightStatus status ) throws NoSuchFlightException {

        // find flight, if doesn't exists throw an exception
        Flight flight = service.find( flightNumber );

        // if flight is already invalid, throw an exception
        if ( !flight.isValid() ) throw new NoSuchFlightException( "Flight is already deleted." );

        // update entity and save changes
        flight.getDeparture().setActual( departure );
        flight.getArrival().setActual( arrival );
        flight.setStatus( status );
        service.save( flight );
    }
}
