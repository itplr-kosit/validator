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
package org.kosit.validator.impl.conformatron;

import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.conformatron.api.model.source.ICTValidationSource;
import org.w3c.dom.Document;

/**
 * Validator implementation of {@link ICTParsedValidationSource} carrying the document as a W3C DOM (conformatron-api
 * ADR-002). The DOM is built without line numbering (ADR-001).
 * <p>
 * Instances are immutable: the source bytes are defensively copied on construction and cloned on access, the SHA-512
 * hash is computed once from the retained bytes (ADR-003). On a well-formedness failure the instance is created
 * {@link #unparsed(ICTValidationSource, byte[]) without a DOM} — bytes and hash are retained so the partial CVRL can
 * identify the document ({@link #isParsed()} returns {@code false}).
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class DomValidationSource implements ICTParsedValidationSource {

    private final ICTValidationSource source;

    private final byte[] sourceBytes;

    private final String sha512Hash;

    private final Document dom;

    public DomValidationSource(final ICTValidationSource source, final byte[] sourceBytes, final Document dom) {
        if (source == null) {
            throw new IllegalArgumentException("source may not be null");
        }
        if (sourceBytes == null) {
            throw new IllegalArgumentException("sourceBytes may not be null");
        }
        this.source = source;
        this.sourceBytes = sourceBytes.clone();
        this.sha512Hash = SourceDigest.sha512Hex(this.sourceBytes);
        this.dom = dom;
    }

    /**
     * Creates the well-formedness-failure representation (step 2 output path 2): source metadata, bytes and hash are
     * retained for document identity in the partial CVRL, but no parsed content is available.
     *
     * @param source the validation source metadata
     * @param sourceBytes the entire source document
     * @return a new source with {@link #isParsed()} {@code == false}
     */
    public static DomValidationSource unparsed(final ICTValidationSource source, final byte[] sourceBytes) {
        return new DomValidationSource(source, sourceBytes, null);
    }

    @Override
    public ICTValidationSource getSource() {
        return this.source;
    }

    @Override
    public byte[] getSourceBytes() {
        return this.sourceBytes.clone();
    }

    @Override
    public String getSha512Hash() {
        return this.sha512Hash;
    }

    @Override
    public Object getParsedContent() {
        return this.dom;
    }

    @Override
    public Document getDom() {
        return this.dom;
    }
}
