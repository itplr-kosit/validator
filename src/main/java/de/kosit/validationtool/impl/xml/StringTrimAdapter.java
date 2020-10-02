package de.kosit.validationtool.impl.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(final String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }

    @Override
    public String marshal(final String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }

    public static String trim(final String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }
}