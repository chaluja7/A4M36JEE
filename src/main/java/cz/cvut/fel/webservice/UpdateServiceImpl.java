package cz.cvut.fel.webservice;

import cz.cvut.fel.exception.NoSuchFlightException;
import cz.cvut.fel.model.Flight;
import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.service.FlightService;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.InInterceptors;
import org.jboss.ws.api.annotation.EndpointConfig;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import java.util.Date;

import static cz.cvut.fel.utils.DateUtils.toStandardFormat;

/** @author Karel Cemus */
@Slf4j
@WebServlet( urlPatterns = "/ws/update" )
@WebService(
        serviceName = "UpdateService", portName = "update", name = "update",
        endpointInterface = "cz.cvut.fel.webservice.UpdateService",
        targetNamespace = "http://fel.cvut.cz/FlightSystem/ws/update"
)
@EndpointConfig( configFile = "WEB-INF/jaxws-endpoint-config.xml", configName = "WS-Security Endpoint" )
@InInterceptors( interceptors = {
        "cz.cvut.fel.webservice.JAASAuth",
        //        "org.apache.cxf.interceptor.security.SecureAnnotationsInterceptor"
        "cz.cvut.fel.webservice.RoleCheckingInterceptor"
} )
//        "org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingPolicyInterceptor",
//        "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.POJOEndpointAuthorizationInterceptor"}
//,
//        "cz.cvut.fel.webservice.xPOJOEndpointAuthorizationInterceptor"
//        "org.apache.cxf.interceptor.security.SecureAnnotationsInterceptor"
public class UpdateServiceImpl implements UpdateService {

    @Inject
    private FlightService service;

    @RolesAllowed( "webservice" )
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

        log.info( "Flight '{}' updated on departure = '{}', arrival = '{}', status = '{}'.",
                new Object[]{ flightNumber, toStandardFormat( departure ), toStandardFormat( arrival ), status } );
    }
}
