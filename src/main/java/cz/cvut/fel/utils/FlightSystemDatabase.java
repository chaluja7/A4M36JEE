package cz.cvut.fel.utils;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Qualifier to identify database resource.</p>
 *
 * @author Karel Cemus
 */
@Qualifier
@Documented
@Target( { TYPE, METHOD, PARAMETER, FIELD } )
@Retention( RUNTIME )
public @interface FlightSystemDatabase {

}
