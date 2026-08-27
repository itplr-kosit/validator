package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.tasks.CheckTask.Process;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

/**
 * Test for {@link CreateReportsTask}.
 * 
 * @author Andreas Penski
 */
public class CreateReportsTaskTest {

    private CreateReportsTask action;

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.repository = Simple.createContentRepository();
        this.action = new CreateReportsTask(this.repository.getProcessor());
    }

    @Test
    public void testSimpleCreate() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().build();
        final ProcessStepResult<List<BusinessReport>, SimpleError> result = this.action.check(process);
        assertThat(result).isNotNull();
    }

    @Test
    public void testExecutionException() throws SaxonApiException {
        final Processor p = mock(Processor.class);
        final DocumentBuilder documentBuilder = mock(DocumentBuilder.class);
        this.action = new CreateReportsTask(p);

        when(p.newDocumentBuilder()).thenReturn(documentBuilder);
        when(documentBuilder.build(any(Source.class))).thenThrow(new SaxonApiException("mocked"));
        final Process process = TestProcessBuilder.create(TestHelper.read(Simple.SIMPLE_VALID)).build();
        this.action.check(process);
        assertThat(process.isStopped()).isTrue();

    }
}
