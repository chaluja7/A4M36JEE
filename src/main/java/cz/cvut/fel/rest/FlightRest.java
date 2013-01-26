package cz.cvut.fel.rest;

import cz.cvut.fel.dto.FlightDTO;
import cz.cvut.fel.service.FlightService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.List;

/**
 * <p>RESP API provider for flight data source</p>
 *
 * @author Karel Cemus
 */
@Path( "/flight" )
@Produces( MediaType.APPLICATION_JSON )
public class FlightRest {

    @Inject
    private FlightService service;

    @Context
    private UriInfo uri;

    /** convert seconds to milliseconds */
    private static final long CONVERT_TO_MILLISECONDS = 1000;

    @GET
    @Path( "/" )
    public List<FlightDTO> findFlights( @QueryParam( "time-from" ) long timeFrom, @QueryParam( "time-to" ) long timeTo, @QueryParam( "code-from" ) String codeFrom, @QueryParam( "code-to" ) String codeTo ) {
        Date dateFrom = new Date( timeFrom * CONVERT_TO_MILLISECONDS );
        Date dateTo = new Date( timeTo * CONVERT_TO_MILLISECONDS );

        return FlightDTO.convert( uri, service.findFlights( dateFrom, dateTo, codeFrom, codeTo ) );
    }

    @GET
    @Path( "/from" )
    public List<FlightDTO> findFlightsFrom( @QueryParam( "time-from" ) long timeFrom, @QueryParam( "time-to" ) long timeTo, @QueryParam( "code-from" ) String codeFrom ) {
        Date dateFrom = new Date( timeFrom * CONVERT_TO_MILLISECONDS );
        Date dateTo = new Date( timeTo * CONVERT_TO_MILLISECONDS );

        return FlightDTO.convert( uri, service.findFlightsFrom( dateFrom, dateTo, codeFrom ) );
    }

    @GET
    @Path( "/to" )
    public List<FlightDTO> findFlightsTo( @QueryParam( "time-from" ) long timeFrom, @QueryParam( "time-to" ) long timeTo, @QueryParam( "code-from" ) String codeTo ) {

        Date dateFrom = new Date( timeFrom * CONVERT_TO_MILLISECONDS );
        Date dateTo = new Date( timeTo * CONVERT_TO_MILLISECONDS );

        return FlightDTO.convert( uri, service.findFlightsTo( dateFrom, dateTo, codeTo ) );
    }

    @GET
    @Path( "/{number}" )
    public Response getInstance( @PathParam( "number" ) String number ) {
        try {
            return Response.ok( new FlightDTO( uri, service.find( number ) ) ).build();
        } catch ( Exception ignored ) {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }
}
