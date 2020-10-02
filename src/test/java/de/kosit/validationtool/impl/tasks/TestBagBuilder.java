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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.validation.Schema;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.ResolvingMode;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ValidationResultsSchematron;
import de.kosit.validationtool.model.reportInput.ValidationResultsSchematron.Results;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.ValidateWithXmlSchema;

import net.sf.saxon.s9api.XdmNode;

/**
 * Utilities for creating test objects.
 * 
 * @author Andreas Penski
 */
public class TestBagBuilder {

    public static Bag createBag(final Input input) {
        return createBag(input, false);
    }

    public static Bag createBag(final Input input, final boolean parse) {
        return createBag(input, parse, new CreateReportInput());
    }

    public static Bag createBag(final Input input, final boolean parse, final CreateReportInput reportInput) {
        final Bag bag = new Bag(input, reportInput);
        if (parse) {
            bag.setParserResult(Helper.parseDocument(bag.getInput()));
        }
        bag.setScenarioSelectionResult(new Result<>(createScenario(Helper.Simple.getSchemaLocation())));
        return bag;
    }

    private static Scenario createScenario(final URI schemafile) {

        try {
            final ScenarioType t = new ScenarioType();
            final ValidateWithXmlSchema v = new ValidateWithXmlSchema();
            final ResourceType r = new ResourceType();
            r.setLocation(schemafile.getRawPath());
            r.setName("invoice");
            v.getResource().add(r);
            t.setValidateWithXmlSchema(v);
            final Scenario scenario = new Scenario(t);
            scenario.setSchema(createSchema(schemafile.toURL()));
            return scenario;
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Schema createSchema(final URL toURL) {
        final ContentRepository contentRepository = new ContentRepository(ResolvingMode.STRICT_RELATIVE.getStrategy(), null);
        return contentRepository.createSchema(toURL);
    }

    private static XdmNode createReport() {
        return Helper.parseDocument(InputFactory.read("<some>xml</some>".getBytes(), "someXml")).getObject();
    }

    static Bag createBag(final boolean schemaValid, final boolean schematronValid) {
        final Result<Boolean, XMLSyntaxError> schemaResult = schemaValid ? new Result<>(true)
                : new Result<>(Collections.singletonList(new XMLSyntaxError()));
        final List<ValidationResultsSchematron> schematronResult = schematronValid ? Collections.emptyList() : createSchematronError();
        return createBag(schemaResult, schematronResult);
    }

    private static List<ValidationResultsSchematron> createSchematronError() {
        final ValidationResultsSchematron v = new ValidationResultsSchematron();
        final SchematronOutput out = new SchematronOutput();
        final FailedAssert f = new FailedAssert();
        out.getActivePatternAndFiredRuleAndFailedAssert().add(f);
        final Results r = new Results();
        r.setSchematronOutput(out);
        v.setResults(r);
        return Collections.singletonList(v);
    }

    static Bag createBag(final Result<Boolean, XMLSyntaxError> schemaResult,
            final Collection<ValidationResultsSchematron> schematronResult) {
        final CreateReportInput reportInput = new CreateReportInput();
        reportInput.getValidationResultsSchematron().addAll(schematronResult);
        final Bag b = createBag(InputFactory.read("<someXml></someXml>".getBytes(), "someCheck"), true, reportInput);
        b.setSchemaValidationResult(schemaResult);
        b.setReport(createReport());
        return b;
    }
}
