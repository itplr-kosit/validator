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
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * @author Andreas Penski
 */
public class PrintReportActionTest {

    private static final URL REPORT = SerializeReportActionTest.class.getResource("/examples/results/report.xml");

    private CommandLine commandLine;

    private PrintReportAction action;

    @Before
    public void setup() throws IOException {
        commandLine = new CommandLine();
        commandLine.activate();
        action = new PrintReportAction();
    }

    @After
    public void tearDownd() throws IOException {
        commandLine.deactivate();
    }

    @Test
    public void testSimpleSerialize() {
        CheckAction.Bag b = new CheckAction.Bag(InputFactory.read(REPORT));
        b.setReport(Helper.load(REPORT));
        assertThat(action.isSkipped(b)).isFalse();
        action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(commandLine.getOutput()).isNotEmpty();
        // assertThat(commandLine.getOutput()).contains("<?xml version=\"1.0\" ");
        // assertThat(commandLine.getErrorOutput()).isEmpty();
    }

}
