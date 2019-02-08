/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.api;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

/**
 * Zentrale Schnittstellendefinition für das Prüf-Tool.
 * 
 * @author Andreas Penski
 */
public interface Check {

    /**
     * Führt die konfigurierte Prüfung für die übergebene Resource aus.
     *
     * @param input die Resource / XML-Datei, die geprüft werden soll.
     * @return ein Ergebnis-{@link Document}
     * @deprecated use {@link #checkInput(Input)}
     */
    @Deprecated
    default Document check(Input input) {
        final XdmNode node = checkInput(input);
        // readonly view of the document!!!
        return (Document) NodeOverNodeInfo.wrap(node.getUnderlyingNode());
    }

    /**
     * Führt die konfigurierte Prüfung für die übergebene Resource aus.
     *
     * @param input die Resource / XML-Datei, die geprüft werden soll.
     * @return ein Ergebnis-{@link Document}
     */
    XdmNode checkInput(Input input);

    /**
     * Führt eine Prüfung im Batch-Mode durch. Die Default-Implementierung führt die Prüfung sequentiell aus.
     * 
     * @param input die Eingabe
     * @return Liste mit Ergebnis-Dokumenten
     * @deprecated use {@link #checkInput(List)}
     */
    @Deprecated
    default List<Document> check(List<Input> input) {
        return input.stream().map(this::check).collect(Collectors.toList());
    }

    /**
     * Führt eine Prüfung im Batch-Mode durch. Die Default-Implementierung führt die Prüfung sequentiell aus.
     *
     * @param input die Eingabe
     * @return Liste mit Ergebnis-Dokumenten
     */
    default List<XdmNode> checkInput(List<Input> input) {
        return input.stream().map(this::checkInput).collect(Collectors.toList());
    }

}
