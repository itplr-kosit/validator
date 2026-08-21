package org.kosit.validator.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.tasks.CheckTask;
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
        assertThat(this.action.isSkipped(TestProcessBuilder.create(VInputFactory.read(Simple.SIMPLE_VALID)).build())).isTrue();
        final CheckTask.Process process = TestProcessBuilder.create(VInputFactory.read(Simple.SIMPLE_VALID))
                .setCreateReport(TestHelper.load(Simple.SIMPLE_VALID)).build();
        this.action.check(process);
        assertThat(this.action.isSkipped(process)).isFalse();
        this.action.check(process);
        assertThat(process.isStopped()).isFalse();
        assertThat(Files.list(this.tmpDirectory).toList()).hasSize(1);
    }
}
