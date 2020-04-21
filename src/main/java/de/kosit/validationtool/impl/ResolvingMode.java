package de.kosit.validationtool.impl;

import java.net.URI;

import javax.xml.transform.URIResolver;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Defines how artefacts are resolved internally.
 * 
 * @author Andreas Penski
 */
public enum ResolvingMode {

    /**
     * Resolving using only the configured content repository. No furthing resolving allowed. This
     */
    STRICT_RELATIVE {

        @Override
        public URI resolve(final URI source, final URI repository) {
            return RelativeUriResolver.resolve(source, repository);
        }

        @Override
        public URIResolver createResolver(final URI repository) {
            return new RelativeUriResolver(repository);
        }
    },

    STRICT_LOCAL,

    JDK_SUPPORTED,

    CUSTOM;

    public URI resolve(final URI source, final URI repository) {
        throw new NotImplementedException("Not yet implemented");
    }

    public URIResolver createResolver(final URI repository) {
        throw new NotImplementedException("Not yet implemented");
    }
}
