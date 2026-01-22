package org.kosit.validator.impl.xml;

import org.w3c.dom.ls.LSInput;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class LsResourceResolverUriResolver implements URIResolver {

    private final ClassPathResourceResolver lsResolver;

    public LsResourceResolverUriResolver(ClassPathResourceResolver lsResolver) {
        this.lsResolver = lsResolver;
    }

    @Override
    public Source resolve(String href, String base) {
        LSInput in = lsResolver.resolveResource(null, null, null, href, base);

        if (in == null) {
            throw new IllegalStateException("External resource loading disabled. Cannot resolve XSLT href=" + href + " base=" + base);
        }

        StreamSource s = new StreamSource(in.getByteStream());
        String sys = in.getSystemId();
        if (sys != null)
            s.setSystemId(sys);
        return s;
    }
}
