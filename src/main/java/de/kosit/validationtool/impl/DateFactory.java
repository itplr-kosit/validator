package de.kosit.validationtool.impl;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.SneakyThrows;

/**
 * @author Andreas Penski
 */
public class DateFactory {

    private DateFactory() {
        // hide
    }

    @SneakyThrows
    public static XMLGregorianCalendar createTimestamp() {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

    }
}
