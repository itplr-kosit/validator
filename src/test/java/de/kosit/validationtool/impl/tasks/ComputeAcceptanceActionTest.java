package de.kosit.validationtool.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ValidationResultsSchematron;
import de.kosit.validationtool.model.reportInput.ValidationResultsSchematron.Results;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the 'acceptMatch' functionality.
 * 
 * @author Andreas Penski
 */
public class ComputeAcceptanceActionTest {

    private final ComputeAcceptanceAction action = new ComputeAcceptanceAction();

    @Test
    public void simpleTest() {
        final Bag bag = createBag(true, true);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.UNDEFINED);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testSchemaFailed() {
        final Bag bag = createBag(false, true);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    public void testSchematronFailed() {
        final Bag bag = createBag(true, false);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    public void testValidAcceptMatch() {
        final Bag bag = createBag(true, true);
        bag.getScenarioSelectionResult().getObject().setAcceptMatch("count(//doesnotExist) = 0");
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testAcceptMatchNotSatisfied() {
        final Bag bag = createBag(true, true);
        bag.getScenarioSelectionResult().getObject().setAcceptMatch("count(//doesnotExist) = 1");
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    public void testValidAcceptMatchOnSchematronFailed() {
        final Bag bag = createBag(true, false);
        bag.getScenarioSelectionResult().getObject().setAcceptMatch("count(//doesnotExist) = 0");
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testValidAcceptMatchOnSchemaFailed() {
        final Bag bag = createBag(false, true);
        bag.getScenarioSelectionResult().getObject().setAcceptMatch("count(//doesnotExist) = 0");
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    public void testMissingSchemaCheck() {
        final Bag bag = createBag(null, Collections.emptyList());
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    public void testNoSchematronCheck() {
        final Bag bag = createBag(true, true);
        // remove schematron results
        bag.getReportInput().getValidationResultsSchematron().clear();
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testMissingReport() {
        final Bag bag = createBag(false, true);
        bag.setReport(null);
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    private static Bag createBag(final boolean schemaValid, final boolean schematronValid) {
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

    private static Bag createBag(final Result<Boolean, XMLSyntaxError> schemaResult,
            final Collection<ValidationResultsSchematron> schematronResult) {
        final ScenarioType t = new ScenarioType();
        t.initialize(new ContentRepository(ObjectFactory.createProcessor(), Simple.REPOSITORY), true);
        final CreateReportInput reportInput = new CreateReportInput();
        reportInput.getValidationResultsSchematron().addAll(schematronResult);
        final Bag b = new Bag(InputFactory.read("<someXml></someXml>".getBytes(), "someCheck"), reportInput);
        final Result<XdmNode, XMLSyntaxError> parseREsult = DocumentParseAction.parseDocument(b.getInput());
        b.setReport(parseREsult.getObject());
        b.setParserResult(parseREsult);
        b.setSchemaValidationResult(schemaResult);
        b.setScenarioSelectionResult(new Result<>(t));
        return b;
    }
}
