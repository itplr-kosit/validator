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

package org.kosit.validator.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.kosit.validator.api.InputFactory;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.tasks.CheckAction;
import org.kosit.validator.impl.tasks.TestProcessBuilder;

/**
 * Tests the HTML extraction of the command line tool.
 *
 * @author Andreas Penski
 */
public class ExtractReportContentActionTest {

    private ExtractReportContentAction action;

    private Path tmpDirectory;

    @BeforeEach
    public void setup() throws IOException {
        this.tmpDirectory = Files.createTempDirectory("checktool");
        this.action = new ExtractReportContentAction(TestObjectFactory.createProcessor(), this.tmpDirectory);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(this.tmpDirectory.toFile());
    }

    @Test
    public void testSimple() throws IOException {
        assertThat(this.action.isSkipped(TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID)).build())).isTrue();
        final CheckAction.Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID))
                .setCreateReport(Helper.load(Simple.SIMPLE_VALID)).build();
        this.action.check(process);
        assertThat(this.action.isSkipped(process)).isFalse();
        this.action.check(process);
        assertThat(process.isStopped()).isFalse();
        assertThat(Files.list(this.tmpDirectory).collect(Collectors.toList())).hasSize(1);
    }
}
