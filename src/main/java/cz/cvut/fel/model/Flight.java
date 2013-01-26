package cz.cvut.fel.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

import static cz.cvut.fel.utils.DateUtils.date;

/**
 * Model reflects all flights which is possible to find and book in the system.
 *
 * @author Karel Cemus
 */
@Data
@Entity
@ToString
@EqualsAndHashCode( of = "id" )
@Table( uniqueConstraints = @UniqueConstraint( columnNames = { "number", "validUntil" } ) )
@NamedQueries( {

        /** finds the flight by its number */
        @NamedQuery( name = "Flight.findByNumber", query = "SELECT f FROM Flight f WHERE f.number = :number AND f.validUntil >= current_timestamp()" ),

        /** finds all scheduled but not realised flights from the location. */
        @NamedQuery( name = "Flight.findByFrom", query = "SELECT f FROM Flight f WHERE " +
                "f.departure.actual >= :intervalFrom AND " +
                "f.departure.actual <= :intervalTo AND " +
                "f.from.code = :codeFrom AND " +
                "f.validUntil >= current_timestamp() "
                + "ORDER BY f.departure.scheduled, f.from.code" ),

        /** finds all scheduled but not realised flights to the location. */
        @NamedQuery( name = "Flight.findByTo", query = "SELECT f FROM Flight f WHERE " +
                "f.departure.actual >= :intervalFrom AND " +
                "f.departure.actual <= :intervalTo AND " +
                "f.to.code = :codeTo AND " +
                "f.validUntil >= current_timestamp() "
                + "ORDER BY f.departure.scheduled, f.from.code" ),

        /** finds all scheduled but not realised flights between the locations. */
        @NamedQuery( name = "Flight.findByFromTo", query = "SELECT f FROM Flight f WHERE " +
                "f.departure.actual >= :intervalFrom AND " +
                "f.departure.actual <= :intervalTo AND " +
                "f.from.code = :codeFrom AND " +
                "f.to.code = :codeTo AND " +
                "f.validUntil >= current_timestamp() "
                + "ORDER BY f.departure.scheduled, f.from.code" ),

        /** invalidate given instance */
        @NamedQuery( name = "Flight.invalidate", query = "UPDATE Flight f SET f.validUntil = current_timestamp() WHERE f.number = :number AND f.validUntil >= current_timestamp()" )
} )
public class Flight implements Serializable {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private long id;

    /** flight number */
    @NotBlank
    @Pattern( regexp = "F[0-9]{6}" )
    @Column( length = 20, nullable = false )
    private String number;

    /** departure date and time */
    @Embedded
    @AttributeOverrides( {
            @AttributeOverride( name = "actual", column = @Column( name = "departure_actual" ) ),
            @AttributeOverride( name = "scheduled", column = @Column( name = "departure_scheduled" ) )
    } )
    private Time departure;

    /** arrival date and time */
    @Embedded
    @AttributeOverrides( {
            @AttributeOverride( name = "actual", column = @Column( name = "arrival_actual" ) ),
            @AttributeOverride( name = "scheduled", column = @Column( name = "arrival_scheduled" ) )
    } )
    private Time arrival;

    /** flight price per seat */
    @Min( 0 )
    @Column( nullable = false )
    private int cost;

    /** departure destination */
    @NotNull
    @ManyToOne( optional = false )
    @JoinColumn( name = "DEPARTURE_FROM" )
    private Destination from;

    /** arrival destination */
    @NotNull
    @ManyToOne( optional = false )
    @JoinColumn( name = "ARRIVAL_TO" )
    private Destination to;

    /** total plane capacity */
    @Min( 0 )
    private int capacity;

    /** number of taken seats */
    @Min( 0 )
    private int seatsTaken;

    /** current status of flight */
    @NotNull
    @Enumerated( EnumType.STRING )
    private FlightStatus status;

    /** instance is valid until deletion date */
    @Temporal( TemporalType.TIMESTAMP )
    private Date validUntil;

    /** optimistic lock */
    @Version
    private long version;

    /** default validation date */
    private static final Date VALID_UNTIl = date( 1, 1, 2200 );

    public Flight() {
        arrival = new Time();
        departure = new Time();
    }

    @AssertTrue( message = "Cannot be reserved more seats than it is flight capacity." )
    boolean isNotFlightOverloaded() {
        // cannot be reserved more seats than it is flight capacity
        return seatsTaken <= capacity;
    }

    @PrePersist
    void prePersist() {
        // hack due to unique constraint - code && NULL never violates it
        if ( validUntil == null ) validUntil = VALID_UNTIl;
    }

    @PreUpdate
    void preUpdate() {
        // hack due to unique constraint - code && NULL never violates it
        if ( validUntil == null ) validUntil = VALID_UNTIl;
    }

    /** mark instance as deleted */
    public void invalidate() {
        validUntil = new Date();
    }

    /** determines whether this flight is available for booking */
    @Transient
    public boolean isBookable() {
        // deleted
        if ( validUntil.getTime() < new Date().getTime() ) return false;

        // no free seat
        if ( capacity <= seatsTaken ) return false;

        // bookable only in these statuses
        return status == FlightStatus.SCHEDULED || status == FlightStatus.DELAYED;
    }

    @Transient
    public int getCapacityLeft() {
        return capacity - seatsTaken;
    }

    @Transient
    public boolean isValid() {
        return validUntil.getTime() >= new Date().getTime();
    }
}
