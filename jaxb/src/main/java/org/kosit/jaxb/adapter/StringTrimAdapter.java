package org.kosit.jaxb.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(final String v) {
        return trimInternal(v);
    }

    @Override
    public String marshal(final String v) {
        return trimInternal(v);
    }

    public static String trim(final String v) {
        return trimInternal(v);
    }

    private static String trimInternal(final String v) {
        if (v == null) {
            return null;
        }
        return v.trim();
    }
}