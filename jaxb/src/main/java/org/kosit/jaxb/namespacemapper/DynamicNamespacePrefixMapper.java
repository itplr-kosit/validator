package org.kosit.jaxb.namespacemapper;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

/**
 * A generic namespace mapper that sets the root namespace as the default namespace and dynamically generates prefixes
 * for all other namespaces from the last part of the URI.
 */
public class DynamicNamespacePrefixMapper extends NamespacePrefixMapper {

    private final String rootNamespace;

    private final String[] preDeclaredNamespaces;

    /**
     * Creates a new mapper.
     *
     * @param rootNamespace the namespace that should be declared without prefix (as default).
     * @param preDeclaredNamespaces the list of namespaces to be declared on the root element.
     */
    public DynamicNamespacePrefixMapper(final String rootNamespace, final String[] preDeclaredNamespaces) {
        this.rootNamespace = rootNamespace;
        this.preDeclaredNamespaces = preDeclaredNamespaces != null ? preDeclaredNamespaces : new String[0];
    }

    /**
     * Creates a new mapper.
     *
     * @param rootNamespace the namespace that should be declared without prefix (as default).
     */
    public DynamicNamespacePrefixMapper(final String rootNamespace) {
        this(rootNamespace, null);
    }

    @Override
    public String getPreferredPrefix(final String namespaceUri, final String suggestion, final boolean requirePrefix) {
        if (namespaceUri.equals(this.rootNamespace) && !requirePrefix) {
            return "";
        }

        if (namespaceUri.contains("/")) {
            String part = namespaceUri.substring(namespaceUri.lastIndexOf("/") + 1);

            if (part.isEmpty() || !Character.isLetter(part.charAt(0))) {
                final String sub = namespaceUri.replaceAll("/$", "");
                if (sub.contains("/")) {
                    part = sub.substring(sub.lastIndexOf("/") + 1);
                }
            }

            if (!part.isEmpty() && Character.isLetter(part.charAt(0))) {
                return part.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            }
        }

        return suggestion;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return this.preDeclaredNamespaces;
    }
}
