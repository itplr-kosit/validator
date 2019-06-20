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


/**
 * Zentrale Schnittstellendefinition für das Prüf-Tool.
 *
 * @author Andreas Penski
 */
public interface Check {

    /**
     * Führt die konfigurierte Prüfung für die übergebene Resource aus. Das Ergebnis-{@link Document} ist readonly. Soll es
     * weiterverarbeitet werden, so muss es kopiert werden.
     *
     * @param input die Resource / XML-Datei, die geprüft werden soll.
     * @return ein Ergebnis-{@link Document} (readonly)
     */
    default Document check(final Input input) {
        final Result result = checkInput(input);
        // readonly view of the document!!!
        return result.getReportDocument();
    }

    /**
     * Führt die konfigurierte Prüfung für die übergebene Resource aus.
     *
     * @param input die Resource / XML-Datei, die geprüft werden soll.
     * @return ein Ergebnis-{@link Document}
     */
    Result checkInput(Input input);

    /**
     * Führt eine Prüfung im Batch-Mode durch. Die Default-Implementierung führt die Prüfung sequentiell aus. Die Ergebnis
     * -{@link Document Dokumente} sind readonly. Sollen sie weiterverarbeitet werden, so müssen Kopien erstellt werden.
     *
     * @param input die Eingabe
     * @return Liste mit Ergebnis-Dokumenten (readonly)
     */
    default List<Document> check(final List<Input> input) {
        return input.stream().map(this::check).collect(Collectors.toList());
    }

    /**
     * Führt eine Prüfung im Batch-Mode durch. Die Default-Implementierung führt die Prüfung sequentiell aus.
     *
     * @param input die Eingabe
     * @return Liste mit Ergebnis-Dokumenten
     */
    default List<Result> checkInput(final List<Input> input) {
        return input.stream().map(this::checkInput).collect(Collectors.toList());
    }


}
