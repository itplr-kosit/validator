package de.kosit.validationtool.impl.tasks;

import java.net.URI;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction.Bag;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.ValidateWithXmlSchema;

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
        final Bag bag = new Bag(input, new CreateReportInput());
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
}
