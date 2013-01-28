package cz.cvut.fel.webservice;

import org.apache.cxf.interceptor.security.JAASLoginInterceptor;

/**
 * <p></p>
 *
 * @author Karel Cemus
 */
public class JAASAuthorizationInterceptor extends JAASLoginInterceptor {

    public JAASAuthorizationInterceptor() {
        setContextName( "FlightSystem-policy" );
    }
}
