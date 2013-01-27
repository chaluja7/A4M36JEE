package cz.cvut.fel.service;

import cz.cvut.fel.utils.FlightSystemDatabase;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * <p>Producer of type-safe persistence context</p>
 *
 * @author Karel Cemus
 */
public class EntityManagerProducer {

    @PersistenceContext
    private EntityManager em;

    @Produces
    @FlightSystemDatabase
    public EntityManager getEm() {
        return em;
    }
}
