package org.kosit.jaxb;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public final class JaxbHelper {

    public static XMLGregorianCalendar createTimestamp() {
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (final DatatypeConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JaxbHelper() {
    }
}
