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

package de.kosit.validationtool.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Simple;

/**
 * Test {@link CheckConfiguration }.
 * 
 * @author Andreas Penski
 */
@Deprecated
public class CheckConfigurationTest {

    @Test
    public void testDelegation() {
        final CheckConfiguration config = new CheckConfiguration(Simple.SCENARIOS);
        config.setScenarioRepository(Simple.REPOSITORY_URI);
        assertThat(config.getScenarios()).isNotEmpty();
        assertThat(config.getContentRepository()).isNotNull();
        assertThat(config.getFallbackScenario()).isNotNull();
        assertThat(config.getAuthor()).isNotEmpty();
        assertThat(config.getDate()).isNotEmpty();
        assertThat(config.getName()).isNotEmpty();
        assertThat(config.getScenarioRepository()).isNotNull();
    }
}
