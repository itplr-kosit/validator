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

import java.net.MalformedURLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.TestObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * @author Andreas Penski
 */
public class PrintReportActionTest {

    private CommandLine commandLine;

    private PrintReportAction action;

    @Before
    public void setup() {
        this.commandLine = new CommandLine();
        CommandLine.activate();
        this.action = new PrintReportAction(TestObjectFactory.createProcessor());
    }

    @After
    public void tearDown() {
        CommandLine.deactivate();
    }

    @Test
    public void testSimpleSerialize() throws MalformedURLException {
        final CheckAction.Bag b = new CheckAction.Bag(InputFactory.read(Simple.SIMPLE_VALID));
        b.setReport(Helper.load(Simple.SIMPLE_VALID.toURL()));
        assertThat(this.action.isSkipped(b)).isFalse();
        this.action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(CommandLine.getOutput()).isNotEmpty();
        assertThat(CommandLine.getOutput()).contains("<?xml version=\"1.0\" ");
        assertThat(CommandLine.getErrorOutput()).isEmpty();
    }

}
