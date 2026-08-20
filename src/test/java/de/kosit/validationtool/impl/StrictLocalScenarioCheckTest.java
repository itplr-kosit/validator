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

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.Helper.Simple;

/**
 * Prüft den Validator im {@link ResolvingMode#STRICT_LOCAL}. Insbesondere muss eine Prüfung vollständig durchlaufen;
 * mit einem leeren Resolver schlugen Transformationen auf Saxon 12 mit einer {@link NullPointerException} fehl.
 */
public class StrictLocalScenarioCheckTest {

    private DefaultCheck implementation;

    @Before
    public void setup() {
        final Configuration config = Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI)
                .setResolvingMode(ResolvingMode.STRICT_LOCAL).build(Helper.getTestProcessor());
        this.implementation = new DefaultCheck(config);
    }

    @Test
    public void testValidationCompletes() throws MalformedURLException {
        final Result result = this.implementation.checkInput(InputFactory.read(Simple.SIMPLE_VALID.toURL()));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }
}
