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

package de.kosit.validationtool.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Tests {@link DefaultNamingStrategy}
 * 
 * @author Andreas Penski
 */
public class DefaultNamingStrategyTest {

    @Test
    public void testSimple() {
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
    public void testDotted() {
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
    public void testDoubleDotted() {
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
    public void testUnknownExtenson() {
        final DefaultNamingStrategy strategy = new DefaultNamingStrategy();
        assertThat(strategy.createName("test.ext")).isEqualTo("test.ext-report.xml");
        strategy.setPrefix("prefix");
        assertThat(strategy.createName("test.ext")).isEqualTo("prefix-test.ext.xml");
        strategy.setPostfix("postfix");
        assertThat(strategy.createName("test.ext")).isEqualTo("prefix-test.ext-postfix.xml");
        strategy.setPrefix(null);
        assertThat(strategy.createName("test.ext")).isEqualTo("test.ext-postfix.xml");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyInput() {
        new DefaultNamingStrategy().createName(null);
    }
}
