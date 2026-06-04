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

package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.tasks.CheckAction.Process;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.impl.XvrlConversionService;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

/**
 * Test for {@link CreateReportsAction}.
 * 
 * @author Andreas Penski
 */
public class CreateReportsActionTest {

    private CreateReportsAction action;

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.repository = Simple.createContentRepository();
        this.action = new CreateReportsAction(this.repository.getProcessor(), new XvrlConversionService());
    }

    @Test
    public void testSimpleCreate() {
        final Process process = TestProcessBuilder.create().schemaValid().schematronValid().build();
        final ProcessStepResult<List<BusinessReport>, XMLSyntaxError> result = this.action.check(process);
        assertThat(result).isNotNull();
    }

    @Test
    public void testExecutionException() throws SaxonApiException {
        final Processor p = mock(Processor.class);
        final DocumentBuilder documentBuilder = mock(DocumentBuilder.class);
        this.action = new CreateReportsAction(p, new XvrlConversionService());

        when(p.newDocumentBuilder()).thenReturn(documentBuilder);
        when(documentBuilder.build(any(Source.class))).thenThrow(new SaxonApiException("mocked"));
        final Process process = TestProcessBuilder.create(InputFactory.read(Simple.SIMPLE_VALID)).build();
        this.action.check(process);
        assertThat(process.isStopped()).isTrue();

    }
}
