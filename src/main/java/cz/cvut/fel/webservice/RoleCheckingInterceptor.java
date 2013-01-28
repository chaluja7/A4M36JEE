package cz.cvut.fel.webservice;

import org.apache.cxf.interceptor.security.SecureAnnotationsInterceptor;

/**
 * <p></p>
 *
 * @author Karel Cemus
 */
public class RoleCheckingInterceptor extends SecureAnnotationsInterceptor {

    public RoleCheckingInterceptor() {

        // scan service for annotations
        setSecuredObject( new UpdateServiceImpl() );
    }
}
