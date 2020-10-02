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

import static de.kosit.validationtool.impl.tasks.TestBagBuilder.createBag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.Scenario.Transformation;
import de.kosit.validationtool.impl.xml.RelativeUriResolver;
import de.kosit.validationtool.model.scenarios.ResourceType;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Tests {@link SchematronValidationAction}.
 * 
 * @author Andreas Penski
 */
public class SchematronValidationActionTest {

    private SchematronValidationAction action;

    @Before
    public void setup() {
        this.action = new SchematronValidationAction(new RelativeUriResolver(Simple.REPOSITORY_URI), new ConversionService());
    }

    @Test
    public void testProcessingError() throws IOException, SaxonApiException {
        final CheckAction.Bag bag = createBag(InputFactory.read(Simple.SIMPLE_VALID.toURL()), true);

        final Scenario scenario = bag.getScenarioSelectionResult().getObject();
        final XsltExecutable exec = mock(XsltExecutable.class);
        final XsltTransformer transformer = mock(XsltTransformer.class);
        doThrow(new SaxonApiException("invalid")).when(transformer).transform();
        when(exec.load()).thenReturn(transformer);
        final ResourceType resourceType = new ResourceType();
        resourceType.setName("invalid internal");
        scenario.setSchematronValidations(Collections.singletonList(new Transformation(exec, resourceType)));
        this.action.check(bag);
        assertThat(bag.getReportInput().getProcessingError().getError()).isNotEmpty();
    }
}
