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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * Testet die HTML-Extrkation des Kommondazeilenprogramms.
 * 
 * @author Andreas Penski
 */
public class ExtractHtmlActionTest {

    private static final URL REPORT = SerializeReportActionTest.class.getResource("/examples/results/report.xml");

    private ExtractHtmlContentAction action;

    private Path tmpDirectory;

    @Before
    public void setup() throws IOException {
        tmpDirectory = Files.createTempDirectory("checktool");
        action = new ExtractHtmlContentAction(Helper.loadTestRepository(), tmpDirectory);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDirectory.toFile());
    }

    @Test
    public void testSimple() throws IOException {
        CheckAction.Bag b = new CheckAction.Bag(InputFactory.read(REPORT));
        assertThat(action.isSkipped(b)).isTrue();
        b.setReport(Helper.load(REPORT));
        action.check(b);
        assertThat(action.isSkipped(b)).isFalse();
        action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(Files.list(tmpDirectory).collect(Collectors.toList())).hasSize(1);
    }
}
