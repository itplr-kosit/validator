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

package de.kosit.validationtool.impl.tasks;

import static de.kosit.validationtool.config.TestConfigurationFactory.createScenario;
import static de.kosit.validationtool.impl.Helper.serialize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.transform.Source;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

/**
 * Test for {@link CreateReportAction}.
 * 
 * @author Andreas Penski
 */
public class CreateReportActionTest {

    private CreateReportAction action;

    private ContentRepository repository;

    @Before
    public void setup() {
        this.repository = Simple.createContentRepository();
        this.action = new CreateReportAction(this.repository.getProcessor(), new ConversionService(), this.repository.getResolver(),
                this.repository.getUnparsedTextURIResolver());
    }

    @Test
    public void testSimpleCreate() {
        final Bag bag = TestBagBuilder.createBag(true, true);
        final Scenario scenario = createScenario().build(this.repository).getObject();
        bag.setScenarioSelectionResult(new Result<>(scenario));
        bag.setReport(null);
        this.action.check(bag);
        assertThat(bag.getReport()).isNotNull();
    }

    @Test
    public void testNoValidParseResult() {
        // e.g. no valid xml file specified
        final Bag bag = TestBagBuilder.createBag(InputFactory.read("someBytes".getBytes(), "invalid"), true);
        final Scenario scenario = createScenario().build(this.repository).getObject();
        bag.setScenarioSelectionResult(new Result<>(scenario));
        assertThat(bag.getReport()).isNull();
        this.action.check(bag);
        assertThat(bag.getReport()).isNotNull();
        final String reportString = serialize(bag.getReport());
        assertThat(reportString).contains("SAXParseException");
    }

    @Test
    public void testExecutionException() throws SaxonApiException {
        final Processor p = mock(Processor.class);
        final DocumentBuilder documentBuilder = mock(DocumentBuilder.class);
        this.action = new CreateReportAction(p, new ConversionService(), null, null);

        when(p.newDocumentBuilder()).thenReturn(documentBuilder);
        when(documentBuilder.build(any(Source.class))).thenThrow(new SaxonApiException("mocked"));
        final Bag bag = TestBagBuilder.createBag(InputFactory.read(Simple.SIMPLE_VALID), true);
        this.action.check(bag);
        assertThat(bag.isStopped()).isTrue();

    }
}
