package cz.cvut.fel.converter;

import cz.cvut.fel.beans.BeanBase;
import cz.cvut.fel.model.Flight;
import cz.cvut.fel.service.FlightService;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;


/** @author Karel Cemus */
@ApplicationScoped
@Named( "flightConverter" )
public class FlightConverter extends BeanBase implements Converter, Serializable {

    @Inject
    private FlightService service;

    @Override
    public Object getAsObject( final FacesContext context, final UIComponent component, final String value ) {
        try {
            if ( value == null ) return new Flight();
            return service.find( value );
        } catch ( Throwable ex ) {
            throw new ConverterException( new FacesMessage( processException( ex ) ) );
        }
    }

    @Override
    public String getAsString( final FacesContext context, final UIComponent component, final Object value ) {
        if ( value == null ) {
            return null;
        } else {
            return ( ( Flight ) value ).getNumber();
        }
    }
}
