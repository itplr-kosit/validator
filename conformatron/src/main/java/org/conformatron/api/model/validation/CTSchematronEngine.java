/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.conformatron.api.model.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Defines the Schematron engine to be used.
 *
 * @author Philip Helger
 */
public enum CTSchematronEngine {

    /** Pure-Java XPath-only engine */
    PURE_XPATH("pure-xpath", Collections.emptySet(), false),

    /**
     * Pure-Java engine that generates an XSLT 3.0 stylesheet in Java (no external ISO Schematron stylesheet chain) and
     * runs it through Saxon s9api. Suitable both as a validation engine and as a {@code SCH -> XSLT} converter.
     */
    PURE_XSLT("pure-xslt", Collections.emptySet(), true),

    /**
     * ISO Schematron: SCH-to-XSLT preprocessing through the canonical ISO Schematron XSL stylesheet chain. The
     * {@code "schematron"} and {@code "sch"} aliases mirror the pre-v10 {@code ESchematronMode.SCHEMATRON} id.
     */
    ISO_SCHEMATRON("iso-schematron", Collections.emptySet(), true),

    /**
     * SchXslt v1 (XSLT 2). The {@code "schxslt-xslt2"} alias mirrors the pre-v10 {@code ESchematronMode.SCHXSLT_XSLT2}
     * id.
     */
    SCHXSLT1("schxslt", Collections.emptySet(), true),

    /** SchXslt v2 (XSLT 3). */
    SCHXSLT2("schxslt2", Collections.emptySet(), true),

    /**
     * Apply a pre-built XSLT stylesheet directly to the XML instance (does not perform any SCH-to-XSLT step). Used by
     * {@code SchematronResourceXSLT}. {@link #isXSLTBased()} returns {@code false} because this engine consumes
     * ready-made XSLT rather than generating it - the flag is consumed by {@code Schematron2XSLTMojo} which only
     * accepts engines that can produce XSLT from SCH.
     */
    XSLT_PREBUILT("xslt", Collections.emptySet(), false);

    private final @NonNull @Nonempty String id;

    private final @NonNull Set<String> ids = new HashSet<>();

    private final boolean generatesXslt;

    CTSchematronEngine(@NonNull @Nonempty final String id, @NonNull final Set<String> alternativeIDs, final boolean generatesXslt) {
        this.id = id;
        this.ids.add(id);
        if (alternativeIDs != null)
            this.ids.addAll(alternativeIDs);
        this.generatesXslt = generatesXslt;
    }

    @NonNull
    @Nonempty
    public String getID() {
        return id;
    }

    public boolean isGeneratingXslt() {
        return generatesXslt;
    }

    @Nullable
    public static CTSchematronEngine getFromIDOrNull(@Nullable final String id) {
        if (id != null && !id.isEmpty())
            for (final CTSchematronEngine e : values())
                if (e.ids.contains(id))
                    return e;
        return null;
    }
}
