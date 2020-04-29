package de.kosit.validationtool.impl;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Andreas Penski
 */
public class DateFactory {

    public static XMLGregorianCalendar createTimestamp() {
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

        } catch (final DatatypeConfigurationException e) {
            throw new IllegalStateException("Can not create timestamp", e);
        }
    }
}
