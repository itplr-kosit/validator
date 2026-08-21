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
package org.kosit.validator.impl.conformatron.model;

import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.source.CTParsedValidationSourceXML;
import org.conformatron.api.model.source.CTValidationSource;
import org.kosit.validator.impl.conformatron.util.SourceDigest;
import org.w3c.dom.Document;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.type.Type;

/**
 * Validator implementation of {@link CTParsedValidationSource} backed by the Saxon {@link XdmNode} the legacy pipeline
 * works with. Facade: the existing Saxon parse result keeps doing the heavy lifting while downstream steps can already
 * consume the conformatron handshake type.
 * <p>
 * The DOM contract (ADR-002) is fulfilled without re-parsing: {@link #getAsDom()} exposes a read-only W3C DOM view over
 * the Saxon tree. The view carries no line numbers (ADR-001). The source bytes are defensively copied on construction
 * and cloned on access; the hash is computed once via the central {@link SourceDigest} helper (ADR-003).
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class XdmNodeValidationSource implements CTParsedValidationSourceXML {

    private final CTValidationSource source;

    private final byte[] sourceBytes;

    private final byte[] hashBytes;

    private final XdmNode node;

    public XdmNodeValidationSource(final CTValidationSource source, final byte[] sourceBytes, final XdmNode node) {
        if (source == null) {
            throw new IllegalArgumentException("source may not be null");
        }
        if (sourceBytes == null) {
            throw new IllegalArgumentException("sourceBytes may not be null");
        }
        if (node == null) {
            throw new IllegalArgumentException("node may not be null");
        }
        this.source = source;
        this.sourceBytes = sourceBytes.clone();
        this.hashBytes = SourceDigest.hashBytes(this.sourceBytes);
        this.node = node;
    }

    @Override
    public CTValidationSource getSource() {
        return this.source;
    }

    @Override
    public byte[] getSourceBytes() {
        return this.sourceBytes.clone();
    }

    @Override
    public String getHashAlgorithmName() {
        return SourceDigest.getAlgorithmName();
    }

    @Override
    public byte[] getHashBytes() {
        return this.hashBytes.clone();
    }

    @Override
    public XdmNode getParsedContent() {
        return this.node;
    }

    /**
     * @return a <b>read-only</b> W3C DOM view over the underlying Saxon tree (no re-parse, no line numbers)
     */
    public Document getAsDom() {
        final NodeInfo root = this.node.getUnderlyingNode().getRoot();
        if (root.getNodeKind() != Type.DOCUMENT) {
            throw new IllegalStateException("Underlying Saxon tree has no document root node");
        }
        return (Document) NodeOverNodeInfo.wrap(root);
    }

    /**
     * @return the Saxon node as used by the legacy pipeline steps
     */
    public XdmNode getNode() {
        return this.node;
    }
}
