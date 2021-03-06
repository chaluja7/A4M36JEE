package cz.cvut.fel.util;

import cz.cvut.fel.model.FlightStatus;
import cz.cvut.fel.webservice.UpdateService;
import org.jboss.ws.core.StubExt;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/** @author Karel Cemus */
public class UpdateServiceClient implements UpdateService {

    private UpdateService service;

    /**
     * Default constructor
     *
     * @param wsdlUrl The URL to the Hello World WSDL endpoint.
     */
    public UpdateServiceClient( final URL wsdlUrl ) {
        QName serviceName = new QName( "http://fel.cvut.cz/FlightSystem/ws/update", "UpdateService" );

        Service service = Service.create( wsdlUrl, serviceName );
        this.service = service.getPort( UpdateService.class );

        // configure jboss native security - load and set security configuration for client
        URL configuration = UpdateServiceClient.class.getClassLoader().getResource( "jboss-wsse-client.xml" );
        assert ( configuration != null );

        org.jboss.ws.core.StubExt stub = ( StubExt ) this.service;
        stub.setSecurityConfig( configuration.toExternalForm() );
        stub.setConfigName( "Standard WSSecurity Client" );

        assert ( this.service != null );
    }

    /**
     * Default constructor
     *
     * @param url The URL to the Hello World WSDL endpoint.
     *
     * @throws MalformedURLException if the WSDL url is malformed.
     */
    public UpdateServiceClient( final String url ) throws MalformedURLException {
        this( new URL( url ) );
    }

    @Override
    public void update( final String flightNumber, final Date departure, final Date arrival, final FlightStatus status ) {
        service.update( flightNumber, departure, arrival, status );
    }

    public void login( String username, String password ) {
        BindingProvider provider = ( BindingProvider ) service;
        provider.getRequestContext().put( BindingProvider.USERNAME_PROPERTY, username );
        provider.getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password );
    }
}
