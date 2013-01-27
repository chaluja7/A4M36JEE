package cz.cvut.fel.service;

import cz.cvut.fel.exception.NoSuchDestinationException;
import cz.cvut.fel.model.Destination;
import cz.cvut.fel.utils.FlightSystemDatabase;
import lombok.extern.slf4j.Slf4j;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.List;

/** @author Karel Cemus */
@Slf4j
@Stateless
@DeclareRoles( { "admin" } )
@SecurityDomain( "FlightSystem-policy" )
public class DestinationServiceImpl implements DestinationService {

    @Inject
    @FlightSystemDatabase
    private EntityManager em;


    @Override
    public Destination findByCode( final String code ) throws NoSuchDestinationException {

        // verify code is set
        if ( code == null ) throw new IllegalArgumentException( "Code is required parameter." );

        // try to find active entity with the code
        try {
            return em.createNamedQuery( "Destination.findByCode", Destination.class ).setParameter( "code", code ).getSingleResult();
        } catch ( javax.persistence.NoResultException ignored ) {
            throw new NoSuchDestinationException( String.format( "Destination with code '%s' doesn't exist.", code ) );
        }
    }

    @Override
    public List<Destination> findAllDestinations() {

        // return all currently active entities
        return em.createNamedQuery( "Destination.findAllValid", Destination.class ).getResultList();
    }

    @Override
    @RolesAllowed( { "admin" } )
    public Destination save( @Valid final Destination destination ) {

        // verify and validate entity
        if ( destination == null ) throw new IllegalArgumentException( "Destination is required parameter." );

        // persist or update
        return em.merge( destination );
    }

    @Override
    @RolesAllowed( { "admin" } )
    public void delete( final long id ) throws NoSuchDestinationException {

        if ( id <= 0 ) throw new IllegalArgumentException( "Illegal identifier. Value must be greater than 0." );

        int result = em.createNamedQuery( "Destination.invalidate" ).setParameter( "id", id ).executeUpdate();

        if ( result == 0 ) {
            throw new NoSuchDestinationException( String.format( "Destination with id '%d' doesn't exist.", id ) );
        } else if ( result > 1 ) {
            log.error( "Destination deletion affected too many rows. Transaction will be rollback." );
            throw new IllegalStateException( "Destination deletion affected too many rows." );
        }
    }
}
