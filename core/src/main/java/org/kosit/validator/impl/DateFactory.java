package org.kosit.validator.impl;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Andreas Penski
 */
public class DateFactory {

    private DateFactory() {
        // hide
    }

    public static XMLGregorianCalendar createTimestamp() {
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (final DatatypeConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
