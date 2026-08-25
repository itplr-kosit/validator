/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.source;

import java.util.Objects;

import org.conformatron.api.model.source.CTParsedValidationSourceXML;
import org.conformatron.api.model.source.CTValidationSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

/**
 * Validator implementation of {@link CTParsedValidationSource} carrying the document as a W3C DOM (conformatron-api
 * ADR-002). The DOM is built without line numbering (ADR-001).
 * <p>
 * Instances are immutable: the source bytes are defensively copied on construction and cloned on access, the hash is
 * computed once from the retained bytes via the central {@link SourceDigest} helper (ADR-003). On a well-formedness
 * failure the instance is created {@link #unparsed(CTValidationSource, byte[]) without a DOM} — bytes and hash are
 * retained so the partial CVRL can identify the document ({@link #isParsed()} returns {@code false}).
 * </p>
 *
 * @author Andreas Schmitz
 * @author Philip Helger
 */
public final class DomValidationSource implements CTParsedValidationSourceXML {

    private final CTValidationSource source;

    private final Document dom;

    /**
     * Creates the well-formedness-failure representation (step 2 output path 2): source metadata, bytes and hash are
     * retained for document identity in the partial CVRL, but no parsed content is available.
     *
     * @param source the validation source metadata
     * @return a new source with {@link #isParsed()} {@code == false}
     */
    public static DomValidationSource unparsed(final @NonNull CTValidationSource source) {
        return new DomValidationSource(source, null);
    }

    public DomValidationSource(final @NonNull CTValidationSource source, final @Nullable Document dom) {
        Objects.requireNonNull(source);
        this.source = source;
        this.dom = dom;
    }

    @Override
    public CTValidationSource getSource() {
        return this.source;
    }

    @Nullable
    public Document getAsDom() {
        return this.dom;
    }
}
