package org.kosit.validator.cmd;

import static org.assertj.core.api.Assertions.assertThat;

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

        final CheckAction.Process b = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID))
                .setCreateReport(Helper.load(Simple.SIMPLE_VALID)).build();
        CommandLine.clear();
        assertThat(this.action.isSkipped(b)).isFalse();
        this.action.check(b);
        assertThat(b.isStopped()).isFalse();
        assertThat(CommandLine.getOutput()).isNotEmpty();
        assertThat(CommandLine.getOutput()).contains("<?xml version=\"1.0\" ");
        assertThat(CommandLine.getErrorOutput()).isEmpty();
    }

}
