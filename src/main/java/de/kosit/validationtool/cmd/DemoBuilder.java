package de.kosit.validationtool.cmd;

import static de.kosit.validationtool.config.ConfigurationBuilder.defaultFallback;
import static de.kosit.validationtool.config.ConfigurationBuilder.report;
import static de.kosit.validationtool.config.ConfigurationBuilder.scenario;
import static de.kosit.validationtool.config.ConfigurationBuilder.schema;
import static de.kosit.validationtool.config.ConfigurationBuilder.schematron;

import java.net.URI;

import javax.xml.validation.Schema;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.config.FallbackBuilder;
import de.kosit.validationtool.config.ScenarioBuilder;
import de.kosit.validationtool.impl.ResolvingMode;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * @author Andreas Penski
 */
public class DemoBuilder {

    public static void main(final String[] args) {
        final XPathExecutable xpath = null;
        // @formatter:off
        Configuration
                .create()
                    .name("some config")
                    .resolvingMode(ResolvingMode.JDK_SUPPORTED)
                    .with(scenario("s1").match("//name").validate(schema("http://some.schema.url")).description("some desc"))
                    .with(scenario("s2")
                              .match(xpath)
                              .acceptWith(xpath)
                              .validate(schema(URI.create("http://some.other.schema.url")))
                              .validate(schematron("some checks").source("some-schematron.xsl"))
                              .with(report("myReport").source(URI.create("some.xsl")))
                              .description("some desc"))
                    .with(defaultFallback())

                .build();
        
        Configuration
                .create()
                    .name("xrechnung")
                    .resolvingMode(ResolvingMode.STRICT_LOCAL)  
                    .with( ubl() )
                    .with(cii())
                    .with( myFallback())
                .build();
    // @formatter:on 
    }

    private static ScenarioBuilder cii() {
        return null;
    }

    private static FallbackBuilder myFallback() {
        return new FallbackBuilder();
    }

    private static ScenarioBuilder ubl() {
        final Schema schema = null; // load somehow
        final ScenarioBuilder ubl = scenario("ubl");
        ubl.validate(schema("someSchema", schema));
        return ubl;
    }
}
