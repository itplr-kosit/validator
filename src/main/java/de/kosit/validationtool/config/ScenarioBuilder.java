package de.kosit.validationtool.config;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.xml.validation.Schema;

import org.w3c.dom.ls.LSResourceResolver;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.model.scenarios.ScenarioType;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * @author Andreas Penski
 */
public class ScenarioBuilder {

    private final ScenarioType scenario;

    private final ContentRepository contentRepository = new ContentRepository(ObjectFactory.createProcessor(), null);

    ScenarioBuilder(final String name) {
        this.scenario = new ScenarioType();
        this.scenario.setName(name);
    }

    public ScenarioBuilder matches(final String xpath) {
        return matches(xpath, Collections.emptyMap());
    }

    private ScenarioBuilder matches(final String xpath, final Map<String, String> namespaces) {
        // final XPathExecutable matchExecutable = this.contentRepository.createXPath(xpath, namespaces);
        // this.scenario.setMatchExecutable(matchExecutable);
        // this.scenario.setMatch(xpath);
        // if (namespaces != null) {
        // this.scenario.getNamespace().addAll(namespaces.entrySet().stream().map(e -> {
        // NamespaceType t = new NamespaceType();
        // t.setPrefix(e.getKey());
        // t.setValue(e.getValue());
        // return t;
        // }).collect(Collectors.toList()));
        // } else {
        // this.scenario.getNamespace().clear();
        // }
        return this;
    }

    public ScenarioBuilder schemaValidation(final Schema schema) {
        return this;
    }

    public ScenarioBuilder schemaValidation(final URL url) {
        return schemalidation(url, null);
    }

    private ScenarioBuilder schemalidation(final URL url, final LSResourceResolver resolver) {
        return this;
    }

    public ScenarioBuilder addSchematronValidation(final XsltExecutable executable) {
        return this;
    }

    public ScenarioBuilder withReportGenerator(final XsltExecutable executable) {
        return this;
    }

}
