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

package de.kosit.validationtool.cmd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link DefaultNamingStrategy}
 *
 * @author Andreas Penski
 */
class DefaultNamingStrategyTest {

    @Test
    void simple() {
        final DefaultNamingStrategy strategy = new DefaultNamingStrategy();
        assertThat(strategy.createName("test")).isEqualTo("test-report.xml");
        strategy.setPrefix("prefix");
        assertThat(strategy.createName("test")).isEqualTo("prefix-test.xml");
        strategy.setPostfix("postfix");
        assertThat(strategy.createName("test")).isEqualTo("prefix-test-postfix.xml");
        strategy.setPrefix(null);
        assertThat(strategy.createName("test")).isEqualTo("test-postfix.xml");
    }

    @Test
    void dotted() {
        final DefaultNamingStrategy strategy = new DefaultNamingStrategy();
        assertThat(strategy.createName("test.xml")).isEqualTo("test-report.xml");
        strategy.setPrefix("prefix");
        assertThat(strategy.createName("test.xml")).isEqualTo("prefix-test.xml");
        strategy.setPostfix("postfix");
        assertThat(strategy.createName("test.xml")).isEqualTo("prefix-test-postfix.xml");
        strategy.setPrefix(null);
        assertThat(strategy.createName("test.xml")).isEqualTo("test-postfix.xml");
    }

    @Test
    void doubleDotted() {
        final DefaultNamingStrategy strategy = new DefaultNamingStrategy();
        assertThat(strategy.createName("test.second.xml")).isEqualTo("test.second-report.xml");
        strategy.setPrefix("prefix");
        assertThat(strategy.createName("test.second.xml")).isEqualTo("prefix-test.second.xml");
        strategy.setPostfix("postfix");
        assertThat(strategy.createName("test.second.xml")).isEqualTo("prefix-test.second-postfix.xml");
        strategy.setPrefix(null);
        assertThat(strategy.createName("test.second.xml")).isEqualTo("test.second-postfix.xml");
    }

    @Test
    void unknownExtension() {
        final DefaultNamingStrategy strategy = new DefaultNamingStrategy();
        assertThat(strategy.createName("test.ext")).isEqualTo("test.ext-report.xml");
        strategy.setPrefix("prefix");
        assertThat(strategy.createName("test.ext")).isEqualTo("prefix-test.ext.xml");
        strategy.setPostfix("postfix");
        assertThat(strategy.createName("test.ext")).isEqualTo("prefix-test.ext-postfix.xml");
        strategy.setPrefix(null);
        assertThat(strategy.createName("test.ext")).isEqualTo("test.ext-postfix.xml");
    }

    @Test
    void emptyInput() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultNamingStrategy().createName(null));
    }
}
