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

package de.kosit.validationtool.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.cmd.assertions.Assertions;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.model.reportInput.CreateReportInput;

/**
 * Testet das Assertion-Feature.
 * 
 * @author Andreas Penski
 */
public class CheckAssertionActionTest {

    private static final URL SAMPLE = CheckAssertionActionTest.class.getResource("/examples/assertions/ubl-0001.xml");

    private static final URL SAMPLE_REPORT = CheckAssertionActionTest.class.getResource("/examples/assertions/ubl-0001-report.xml");

    private static final URL SAMPLE_ASSERTIONS = CheckAssertionActionTest.class.getResource("/examples/assertions/tests-xrechnung.xml");

    private CommandLine commandLine;

    @Before
    public void setup() throws IOException {
        commandLine = new CommandLine();
        commandLine.activate();
    }

    @Test
    public void testEmptyInput() {
        CheckAssertionAction a = new CheckAssertionAction(new Assertions(), ObjectFactory.createProcessor());
        a.check(new CheckAction.Bag(InputFactory.read(SAMPLE), new CreateReportInput()));
        assertThat(commandLine.getErrorOutput()).contains("Can not find assertions for");
    }

    @Test
    public void testSimple() throws URISyntaxException {
        final CheckAction.Bag bag = new CheckAction.Bag(InputFactory.read(SAMPLE), new CreateReportInput());
        bag.setReport(Helper.load(SAMPLE_REPORT));

        final Assertions assertions = Helper.load(SAMPLE_ASSERTIONS, Assertions.class);
        CheckAssertionAction a = new CheckAssertionAction(assertions, ObjectFactory.createProcessor());
        a.check(bag);

        assertThat(commandLine.getErrorOutput()).contains("Assertion mismatch");
    }
}
