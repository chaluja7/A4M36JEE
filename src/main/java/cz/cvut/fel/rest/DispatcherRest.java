package cz.cvut.fel.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>RESP API provider for destination data source</p>
 *
 * @author Karel Cemus
 */
@Path( "/" )
@Produces( MediaType.APPLICATION_JSON )
public class DispatcherRest {

    @Context
    private UriInfo uri;

    @GET
    @Path( "/" )
    public List<String> dispatch() {
        List<String> urls = new ArrayList<String>( 4 );

        // destination API
        urls.add( uri.getBaseUriBuilder().path( "destination" ).build().toString() );

        // flight API
        urls.add( uri.getBaseUriBuilder().path( "flight" ).build().toString() );
        urls.add( uri.getBaseUriBuilder().path( "flight" ).path( "from" ).build().toString() );
        urls.add( uri.getBaseUriBuilder().path( "flight" ).path( "to" ).build().toString() );

        return urls;
    }
}
