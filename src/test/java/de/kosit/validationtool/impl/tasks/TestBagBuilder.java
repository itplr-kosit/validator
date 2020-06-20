package de.kosit.validationtool.impl.tasks;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.ObjectFactory;
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
            bag.setParserResult(DocumentParseAction.parseDocument(bag.getInput()));
        }
        bag.setScenarioSelectionResult(new Result<>(createScenario(Helper.Simple.getSchemaLocation())));
        return bag;
    }

    private static ScenarioType createScenario(final URI schemafile) {
        final ContentRepository repository = new ContentRepository(ObjectFactory.createProcessor(), Simple.REPOSITORY);
        final ScenarioType t = new ScenarioType();
        final ValidateWithXmlSchema v = new ValidateWithXmlSchema();
        final ResourceType r = new ResourceType();
        r.setLocation(schemafile.getRawPath());
        r.setName("invoice");
        v.getResource().add(r);
        t.setValidateWithXmlSchema(v);
        t.initialize(repository, true);
        return t;
    }

    private static XdmNode createReport() {
        return DocumentParseAction.parseDocument(InputFactory.read("<some>xml</some>".getBytes(), "someXml")).getObject();
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
