/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kosit.validator.impl;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

/**
 * Ein generischer Namespace-Mapper, der den Root-Namespace als Default-Namespace setzt und für alle weiteren Namespaces
 * Präfixe dynamisch aus dem letzten Teil der URI generiert.
 */
public class DynamicNamespacePrefixMapper extends NamespacePrefixMapper {

    private final String rootNamespace;

    private final String[] preDeclaredNamespaces;

    /**
     * Erstellt einen neuen Mapper.
     *
     * @param rootNamespace der Namespace, der ohne Präfix (als Default) deklariert werden soll.
     * @param preDeclaredNamespaces die Liste der Namespaces, die am Root-Element deklariert werden sollen.
     */
    public DynamicNamespacePrefixMapper(final String rootNamespace, final String[] preDeclaredNamespaces) {
        this.rootNamespace = rootNamespace;
        this.preDeclaredNamespaces = preDeclaredNamespaces != null ? preDeclaredNamespaces : new String[0];
    }

    /**
     * Erstellt einen neuen Mapper.
     *
     * @param rootNamespace der Namespace, der ohne Präfix (als Default) deklariert werden soll.
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
