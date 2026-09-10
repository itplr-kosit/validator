package org.kosit.jaxb;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class JaxbHelper {

    private static final DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (final DatatypeConfigurationException ex) {
            throw new IllegalStateException("Can not create the XML datatype factory", ex);
        }
    }

    public static @NonNull XMLGregorianCalendar createTimestamp() {
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        return createTimestamp(cal);
    }

    /**
     * Converts a {@link GregorianCalendar} into the JAXB representation of {@code xs:dateTime}.
     *
     * @param zdt the zoned DateTime to convert. May not be <code>null</code>.
     * @return the converted value. Never <code>null</code>.
     */
    public static @NonNull XMLGregorianCalendar createTimestamp(final @NonNull ZonedDateTime zdt) {
        return createTimestamp(GregorianCalendar.from(zdt));
    }

    /**
     * Converts a {@link GregorianCalendar} into the JAXB representation of {@code xs:dateTime}.
     *
     * @param calendar the calendar to convert. May not be <code>null</code>.
     * @return the converted value. Never <code>null</code>.
     */
    public static @NonNull XMLGregorianCalendar createTimestamp(final @NonNull GregorianCalendar calendar) {
        final XMLGregorianCalendar ret = DATATYPE_FACTORY.newXMLGregorianCalendar(calendar);
        if (calendar.get(Calendar.MILLISECOND) == 0) {
            ret.setFractionalSecond(null);
        }
        return ret;
    }

    /**
     * Convert an XML date to a {@link LocalDate}. The timezone of the provided value is ignored.
     *
     * @param value the value to convert. May be <code>null</code>.
     * @return <code>null</code> if the provided value is <code>null</code> or if it has no year, month or day.
     */
    public static @Nullable LocalDate getAsLocalDate(final @Nullable XMLGregorianCalendar value) {
        if (value == null) {
            return null;
        }

        try {
            final int year = value.getYear();
            final int month = value.getMonth();
            final int day = value.getDay();

            return LocalDate.of(year, month, day);
        } catch (final DateTimeException ex) {
            // Thrown if the value ranges are invalid
            return null;
        }
    }

    /**
     * Convert a {@link LocalDate} to an XML date without timezone.
     *
     * @param value the value to convert. May be <code>null</code>.
     * @return <code>null</code> if the provided value is <code>null</code>.
     */
    public static @Nullable XMLGregorianCalendar getAsXmlDate(final @Nullable LocalDate value) {
        if (value == null) {
            return null;
        }
        return DATATYPE_FACTORY.newXMLGregorianCalendarDate(value.getYear(), value.getMonthValue(), value.getDayOfMonth(),
                DatatypeConstants.FIELD_UNDEFINED);
    }

    private JaxbHelper() {
    }
}
