package org.kosit.jaxb.adapter;

import java.math.BigInteger;

import org.jspecify.annotations.Nullable;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class LongAdapter extends XmlAdapter<BigInteger, Long> {

    @Override
    public @Nullable Long unmarshal(final @Nullable BigInteger bigInteger) throws Exception {
        return bigInteger != null ? Long.valueOf(bigInteger.longValue()) : null;
    }

    @Override
    public @Nullable BigInteger marshal(final @Nullable Long integer) throws Exception {
        return integer != null ? BigInteger.valueOf(integer.longValue()) : null;
    }
}
