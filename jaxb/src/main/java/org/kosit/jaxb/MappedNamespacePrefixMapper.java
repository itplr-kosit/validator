package org.kosit.jaxb;

import java.util.Map;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

/**
 * {@link NamespacePrefixMapper} that delegates to a fixed namespace-URI-to-prefix map. A value of empty string
 * marks a URI as the default namespace.
 */
public final class MappedNamespacePrefixMapper extends NamespacePrefixMapper {

    private final Map<String, String> map;

    MappedNamespacePrefixMapper(final Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String getPreferredPrefix(final String namespaceUri, final String suggestion, final boolean requirePrefix) {
        final String prefix = this.map.get(namespaceUri);
        if (prefix == null) {
            return suggestion;
        }
        // empty string means default namespace, but if JAXB requires a prefix here we must not return ""
        if (prefix.isEmpty() && requirePrefix) {
            return suggestion;
        }
        return prefix;
    }
}