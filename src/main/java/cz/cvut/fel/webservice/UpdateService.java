package cz.cvut.fel.webservice;

import cz.cvut.fel.model.FlightStatus;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Date;

/**
 * <p>Simple web service receiving new information about flights, eg. from the airport.</p>
 *
 * @author Karel Cemus
 */
@WebService( targetNamespace = "http://fel.cvut.cz/FlightSystem/ws/update" )
public interface UpdateService {

    @WebMethod
    void update(
            @WebParam( name = "flightNumber" ) String flightNumber,
            @WebParam( name = "departure" ) Date departure,
            @WebParam( name = "arrival" ) Date arrival,
            @WebParam( name = "status" ) FlightStatus status
    );
}
