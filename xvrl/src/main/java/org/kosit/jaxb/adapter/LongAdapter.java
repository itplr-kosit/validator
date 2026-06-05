package org.kosit.jaxb.adapter;

import java.math.BigInteger;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class LongAdapter extends XmlAdapter<BigInteger, Long> {

    @Override
    public Long unmarshal(final BigInteger bigInteger) throws Exception {
        return bigInteger != null ? bigInteger.longValue() : null;
    }

    @Override
    public BigInteger marshal(final Long integer) throws Exception {
        return integer != null ? BigInteger.valueOf(integer.longValue()) : null;
    }
}
