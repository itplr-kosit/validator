package org.kosit.validator.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.tasks.CheckTask;
import org.kosit.validator.impl.tasks.TestProcessBuilder;

/**
 * @author Andreas Penski
 */
public class PrintReportActionTest {

    private PrintReportAction action;

    @BeforeEach
    public void setup() {
        CommandLine.activate();
        this.action = new PrintReportAction(TestObjectFactory.createProcessor());
    }

    @AfterEach
    public void tearDown() {
        CommandLine.deactivate();
    }

    @Test
    public void testSimpleSerialize() {

        final CheckTask.Process b = TestProcessBuilder.create(TestHelper.read(Simple.SIMPLE_VALID))
                .setCreateReport(TestHelper.load(Simple.SIMPLE_VALID)).build();
        CommandLine.clear();
        assertThat(this.action.isSkipped(b)).isFalse();
        this.action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(CommandLine.getOutput()).isNotEmpty();
        assertThat(CommandLine.getOutput()).contains("<?xml version=\"1.0\" ");
        assertThat(CommandLine.getErrorOutput()).isEmpty();
    }

}
