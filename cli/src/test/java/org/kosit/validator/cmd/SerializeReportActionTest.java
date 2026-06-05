package org.kosit.validator.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
 * @author Andreas Penski
 */
public class SerializeReportActionTest {

    private Path tmpDirectory;

    private SerializeReportAction action;

    @BeforeEach
    public void setup() throws IOException {
        this.tmpDirectory = Files.createTempDirectory("checktool");
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        this.action = new SerializeReportAction(this.tmpDirectory, TestObjectFactory.createXvrlConversionService(), namingStrategy);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(this.tmpDirectory.toFile());
    }

    @Test
    public void testSimpleSerialize() {
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
