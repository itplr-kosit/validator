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

package de.kosit.validationtool.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.Helper.Simple;

/**
 * Prüft die Funktionen des Validator auf Basis eines reduzierten Szenarios.
 *
 * @author Andreas Penski
 */
class SimpleScenarioCheckTest {

    private DefaultCheck implementation;

    @BeforeEach
    void setup() {
        final Configuration d = Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build(Helper.getTestProcessor());
        this.implementation = new DefaultCheck(d);
    }

    @Test
    void simple() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.SIMPLE_VALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    void invalid() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.SCHEMA_INVALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getSchemaViolations()).isNotEmpty();
    }

    @Test
    void unknown() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.UNKNOWN.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);

    }

    @Test
    void withoutAcceptMatch() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.FOO.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

}
