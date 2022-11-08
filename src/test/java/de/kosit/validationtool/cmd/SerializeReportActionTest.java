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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.TestObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.impl.tasks.TestProcessBuilder;

/**
 * @author Andreas Penski
 */
public class SerializeReportActionTest {

    private Path tmpDirectory;

    private SerializeReportAction action;

    @Before
    public void setup() throws IOException {
        this.tmpDirectory = Files.createTempDirectory("checktool");
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        this.action = new SerializeReportAction(this.tmpDirectory, TestObjectFactory.createConversionService(), namingStrategy);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(this.tmpDirectory.toFile());
    }

    @Test
    public void testSimpleSerialize() throws MalformedURLException {
        assertThat(this.action.isSkipped(TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID)).schemaValid().build())).isTrue();
        final CheckAction.Process b = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID)).schemaValid()
                .setCreateReport(Helper.load(Simple.SIMPLE_VALID)).build();
        assertThat(this.action.isSkipped(b)).isFalse();
        this.action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(this.tmpDirectory.toFile().listFiles()).hasSize(1);
    }

    // ERPT-83
    @Test
    public void testName() {
        final String name = "some.name.with.dots";
        final CheckAction.Process b = new CheckAction.Process(InputFactory.read("ega".getBytes(), name + ".xml"));
        assertThat(b.getName()).isEqualTo(name);
    }

}
