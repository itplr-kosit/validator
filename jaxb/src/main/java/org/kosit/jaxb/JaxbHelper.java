package org.kosit.jaxb;

import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class JaxbHelper {

    public static @NonNull XMLGregorianCalendar createTimestamp() {
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (final DatatypeConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
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
        final int year = value.getYear();
        final int month = value.getMonth();
        final int day = value.getDay();
        if (year == DatatypeConstants.FIELD_UNDEFINED || month == DatatypeConstants.FIELD_UNDEFINED
                || day == DatatypeConstants.FIELD_UNDEFINED) {
            return null;
        }
        return LocalDate.of(year, month, day);
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
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(value.getYear(), value.getMonthValue(), value.getDayOfMonth(),
                    DatatypeConstants.FIELD_UNDEFINED);
        } catch (final DatatypeConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JaxbHelper() {
    }
}
