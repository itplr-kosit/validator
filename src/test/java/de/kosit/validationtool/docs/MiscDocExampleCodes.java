package de.kosit.validationtool.docs;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.ResolvingMode;
import de.kosit.validationtool.impl.xml.ProcessorProvider;
import net.sf.saxon.lib.UnparsedTextURIResolver;

public class MiscDocExampleCodes {

    void m1() {
        final Configuration config = Configuration.load(URI.create("myscenarios.xml")).setResolvingMode(ResolvingMode.STRICT_LOCAL)
                .build(ProcessorProvider.getProcessor());
    }

    private static final class MyCustomResolvingConfigurationStrategy implements ResolvingConfigurationStrategy {

        public SchemaFactory createSchemaFactory() {
            // TODO
            return null;
        }

        public URIResolver createResolver(final URI scenarioRepository) {
            // TODO
            return null;
        }

        public UnparsedTextURIResolver createUnparsedTextURIResolver(final URI scenarioRepository) {
            // TODO
            return null;
        }

        public Validator createValidator(final Schema schema) {
            // TODO
            return null;
        }
    }

    void m2() {
        final Configuration config = Configuration.load(URI.create("myscenarios.xml"))
                .setResolvingStrategy(new MyCustomResolvingConfigurationStrategy()).build(ProcessorProvider.getProcessor());
    }

}
