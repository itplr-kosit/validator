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

package de.kosit.validationtool.impl.tasks;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.ResolvingMode;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;
import net.sf.saxon.s9api.XPathExecutable;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static de.kosit.validationtool.impl.tasks.TestBagBuilder.createBag;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the 'acceptMatch' functionality.
 *
 * @author Andreas Penski
 */
class ComputeAcceptanceActionTest {

    private static final String DOES_NOT_EXIST = "count(//doesnotExist) = 0";

    private final ComputeAcceptanceAction action = new ComputeAcceptanceAction();

    @Test
    void simpleTest() {
        final Bag bag = createBag(true, true);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.UNDEFINED);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    void schemaFailed() {
        final Bag bag = createBag(false, true);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    void schematronFailed() {
        final Bag bag = createBag(true, false);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    void validAcceptMatch() {
        final Bag bag = createBag(true, true);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath(DOES_NOT_EXIST));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    void acceptMatchNotSatisfied() {
        final Bag bag = createBag(true, true);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 1"));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    void acceptMatchOverridesSchematronErrors() {
        final Bag bag = createBag(true, false);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath(DOES_NOT_EXIST));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    void validAcceptMatchOnSchemaFailed() {
        final Bag bag = createBag(false, true);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath(DOES_NOT_EXIST));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    void missingSchemaCheck() {
        final Bag bag = createBag(null, Collections.emptyList());
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    void noSchematronCheck() {
        final Bag bag = createBag(true, true);
        // remove schematron results
        bag.getReportInput().getValidationResultsSchematron().clear();
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    void missingReport() {
        final Bag bag = createBag(false, true);
        bag.setReport(null);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    private static XPathExecutable createXpath(final String expression) {
        return new ContentRepository(Helper.getTestProcessor(), ResolvingMode.STRICT_RELATIVE.getStrategy(), null).createXPath(expression,
                new HashMap<>());
    }
}
