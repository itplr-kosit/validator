package org.kosit.validator.docs;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.xml.ProcessorProvider;

import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;

public class MiscDocExampleCodes {

    void m1() {
        final VConfiguration config = VConfiguration.load(URI.create("myscenarios.xml")).setResolvingMode(ResolvingMode.STRICT_LOCAL)
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

        @Override
        public ResourceResolver createResourceResolver(URI scenarioRepository) {
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
        final VConfiguration config = VConfiguration.load(URI.create("myscenarios.xml"))
                .setResolvingStrategy(new MyCustomResolvingConfigurationStrategy()).build(ProcessorProvider.getProcessor());
    }

}
