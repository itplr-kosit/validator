/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import static de.kosit.validationtool.api.InputFactory.read;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;

/**
 * Test das Check-Interface
 * 
 * @author Andreas Penski
 */
public class DefaultCheckTest {

    private static final URL SCENARIO_DEFINITION = ScenarioRepositoryTest.class.getResource("/examples/UBLReady/scenarios-2.xml");

    private static final URL VALID_EXAMPLE = ScenarioRepositoryTest.class
            .getResource("/examples/UBLReady/UBLReady_EU_UBL-NL_20170102_FULL.xml");

    public static final int MULTI_COUNT = 5;

    private DefaultCheck implementation;

    @Before
    public void setup() throws URISyntaxException {
        CheckConfiguration d = new CheckConfiguration(SCENARIO_DEFINITION.toURI());
        d.setScenarioRepository(new File("src/test/resources/examples/repository").toURI());
        implementation = new DefaultCheck(d);
    }

    @Test
    public void testHappyCase() throws Exception {
        final Document doc = implementation.check(read(VALID_EXAMPLE));
        assertThat(doc).isNotNull();
    }

    @Test
    public void testMultipleCase() throws Exception {
        final List<Input> input = IntStream.range(0, MULTI_COUNT).mapToObj(i -> read(VALID_EXAMPLE)).collect(Collectors.toList());
        final List<Document> docs = implementation.check(input);
        assertThat(docs).isNotNull();
        assertThat(docs).hasSize(MULTI_COUNT);
    }

}
