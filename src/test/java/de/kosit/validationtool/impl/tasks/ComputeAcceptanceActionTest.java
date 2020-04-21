package de.kosit.validationtool.impl.tasks;

import static de.kosit.validationtool.impl.tasks.TestBagBuilder.createBag;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;

import net.sf.saxon.s9api.XPathExecutable;

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
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 0"));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testAcceptMatchNotSatisfied() {
        final Bag bag = createBag(true, true);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 1"));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.REJECT);
    }

    @Test
    public void testAcceptMatchOverridesSchematronErrors() {
        final Bag bag = createBag(true, false);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 0"));
        this.action.check(bag);
        assertThat(bag.getAcceptStatus()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testValidAcceptMatchOnSchemaFailed() {
        final Bag bag = createBag(false, true);
        bag.getScenarioSelectionResult().getObject().setAcceptExecutable(createXpath("count(//doesnotExist) = 0"));
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


    private static XPathExecutable createXpath(final String expression) {
        return new ContentRepository(ObjectFactory.createProcessor(), null).createXPath(expression, new HashMap<>());
    }
}
