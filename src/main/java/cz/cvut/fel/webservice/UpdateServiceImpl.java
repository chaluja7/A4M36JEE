package cz.cvut.fel.webservice;

import cz.cvut.fel.model.FlightStatus;
import org.jboss.ws.api.annotation.EndpointConfig;

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

    public void update( String flightNumber, Date departure, Date arrival, FlightStatus status ) {
        System.out.println( "updated" );
    }
}
