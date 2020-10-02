/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.validation.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Setter
@Getter
public class Scenario {

    /**
     * Runtime objects for a transformation e.g. schematron or report.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Transformation {

        private XsltExecutable executable;

        private ResourceType resourceType;
    }

    private final ScenarioType configuration;

    private Schema schema;

    private boolean fallback;

    private XPathExecutable matchExecutable;

    private XPathExecutable acceptExecutable;

    @Setter
    private List<Transformation> schematronValidations;

    private Transformation reportTransformation;

    public List<Transformation> getSchematronValidations() {
        return this.schematronValidations == null ? Collections.emptyList() : this.schematronValidations;
    }

    public String getName() {
        return this.configuration.getName();
    }

    public XPathSelector getMatchSelector() {
        if (this.matchExecutable == null) {
            throw new IllegalStateException("No match executable supplied");
        }
        return this.matchExecutable.load();
    }

    /**
     * Liefert einen neuen XPath-Selector zur Evaluierung der {@link de.kosit.validationtool.api.AcceptRecommendation}.
     *
     * @return neuer Selector
     */
    public Optional<XPathSelector> getAcceptSelector() {
        final XPathSelector selector = this.acceptExecutable != null ? this.acceptExecutable.load() : null;
        return Optional.ofNullable(selector);
    }

}
