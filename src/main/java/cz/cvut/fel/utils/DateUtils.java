package cz.cvut.fel.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * <p>Date utils </p>
 *
 * @author Karel Čemus <karel.cemus@gmail.com>
 */
@Slf4j
public class DateUtils {

    /** helping instance of calendar providing supporting tools */
    private static final Calendar CALENDAR = Calendar.getInstance();

    /** converting constant */
    private static final long MILLISECONDS_TO_DAYS = 1000 * 60 * 60 * 24;

    /** Crop date from hours, minutes etc. It can be understood as a round down */
    public static Date crop( final Date date ) {
        // set current value
        CALENDAR.setTime( date );

        // crop
        CALENDAR.set( Calendar.HOUR_OF_DAY, 0 );
        CALENDAR.set( Calendar.MINUTE, 0 );
        CALENDAR.set( Calendar.SECOND, 0 );
        CALENDAR.set( Calendar.MILLISECOND, 0 );

        // return cropped
        return CALENDAR.getTime();
    }

    /** creates instance of {@link Date} with given parameters */
    public static Date date( final int day, final int month, final int year ) {
        return date( day, month, year, 0, 0 );
    }

    /** creates instance of {@link Date} with given parameters */
    public static Date date( final int day, final int month, final int year, final int hour, final int minutes ) {
        //noinspection MagicConstant
        CALENDAR.set( year, month - 1, day, hour, minutes, 0 );
        CALENDAR.set( Calendar.MILLISECOND, 0 );

        // return the instance
        return CALENDAR.getTime();
    }

    /** distance of two dates in days */
    public static int distance( final Date from, final Date to ) {
        // diff in milliseconds
        final long differenceMilliseconds = from.getTime() - to.getTime();

        // difference in days
        final long differenceDays = differenceMilliseconds / MILLISECONDS_TO_DAYS;

        // return distance from the first
        return Long.valueOf( Math.abs( differenceDays ) ).intValue();
    }

    /** add number of days to given base date */
    public static Date add( final Date base, final int days ) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( base );
        calendar.add( Calendar.DAY_OF_MONTH, days );

        // return new date
        return calendar.getTime();
    }

    /** add number of months to given base date */
    public static Date addMonths( final Date base, final int months ) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( base );
        calendar.add( Calendar.MONTH, months );

        // return new date
        return calendar.getTime();
    }

    private static final SimpleDateFormat DATE_FORMATTER_FROM_GMT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    private static final SimpleDateFormat DATE_FORMATTER_TO_GMT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    static {
        // initialize the formatter
        final Calendar cal = Calendar.getInstance( new SimpleTimeZone( 0, "GMT" ) );
        DATE_FORMATTER_TO_GMT.setCalendar( cal );
    }

    public static Date toGMT( final Date base ) {
        try {
            // convert to string and after that convert it back
            final String date = DATE_FORMATTER_FROM_GMT.format( base );
            return DATE_FORMATTER_TO_GMT.parse( date );

        } catch ( ParseException e ) {
            DateUtils.log.error( "Date parsing failed. Conversion to GMT wasn't performed.", e );
            return base;
        }
    }

    private static final SimpleDateFormat STANDARD_DATE_FORMATTER = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );

    public static String toStandardFormat( final Date date ) {
        return STANDARD_DATE_FORMATTER.format( date );
    }
}
