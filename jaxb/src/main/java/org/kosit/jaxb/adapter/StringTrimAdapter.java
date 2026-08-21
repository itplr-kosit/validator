package org.kosit.jaxb.adapter;

import org.jspecify.annotations.Nullable;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String, String> {

    public static @Nullable String trim(final @Nullable String v) {
        return v == null ? null : v.trim();
    }

    @Override
    public @Nullable String unmarshal(final @Nullable String v) {
        return trim(v);
    }

    @Override
    public @Nullable String marshal(final @Nullable String v) {
        return trim(v);
    }
}