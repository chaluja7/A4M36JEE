package cz.cvut.fel.webservice;

import cz.cvut.fel.model.FlightStatus;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Date;

/**
 * <p>Simple web service receiving new information about flights, eg. from the airport.</p>
 *
 * @author Karel Cemus
 */
@WebService( targetNamespace = "http://fel.cvut.cz/FlightSystem/Update" )
public interface UpdateService {

    @WebMethod
    void update( String flightNumber, Date departure, Date arrival, FlightStatus status );
}
