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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.TestObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * Testet die HTML-Extrkation des Kommondazeilenprogramms.
 * 
 * @author Andreas Penski
 */
public class ExtractHtmlActionTest {

    private ExtractHtmlContentAction action;

    private Path tmpDirectory;

    @Before
    public void setup() throws IOException {
        this.tmpDirectory = Files.createTempDirectory("checktool");
        this.action = new ExtractHtmlContentAction(TestObjectFactory.createProcessor(), this.tmpDirectory);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(this.tmpDirectory.toFile());
    }

    @Test
    public void testSimple() throws IOException {
        final CheckAction.Bag b = new CheckAction.Bag(InputFactory.read(Simple.SIMPLE_VALID));
        assertThat(this.action.isSkipped(b)).isTrue();
        b.setReport(Helper.load(Simple.SIMPLE_VALID.toURL()));
        this.action.check(b);
        assertThat(this.action.isSkipped(b)).isFalse();
        this.action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(Files.list(this.tmpDirectory).collect(Collectors.toList())).hasSize(1);
    }
}
