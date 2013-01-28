package cz.cvut.fel.converter;

import cz.cvut.fel.beans.BeanBase;
import cz.cvut.fel.model.Destination;
import cz.cvut.fel.service.DestinationService;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/** @author Karel Cemus */
@ApplicationScoped
@Named( "destinationConverter" )
public class DestinationConverter extends BeanBase implements Converter, Serializable {

    @Inject
    private DestinationService service;

    @Override
    public Object getAsObject( final FacesContext context, final UIComponent component, final String value ) {
        try {

            if ( value == null ) return new Destination();
            return service.findByCode( value );

        } catch ( Throwable ex ) {
            throw new ConverterException( new FacesMessage( processException( ex ) ) );
        }
    }

    @Override
    public String getAsString( final FacesContext context, final UIComponent component, final Object value ) {
        if ( value == null ) {
            return "";
        } else {
            return ( ( Destination ) value ).getCode();
        }
    }
}
