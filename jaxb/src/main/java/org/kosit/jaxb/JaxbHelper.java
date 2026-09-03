package org.kosit.jaxb;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jspecify.annotations.NonNull;

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
        if (calendar.get(GregorianCalendar.MILLISECOND) == 0) {
            ret.setFractionalSecond(null);
        }
        return ret;
    }

    private JaxbHelper() {
    }
}
