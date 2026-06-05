package org.kosit.validator.config;

import java.util.List;
import java.util.Map;

import org.kosit.validator.api.Configuration;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Scenario;

/**
 * Default implementation class for {@link Configuration}. This class contains all information to run a
 * {@link org.kosit.validator.impl.DefaultCheck}.
 * 
 * @author Andreas Penski
 */
public class DefaultConfiguration implements Configuration {

    private final List<Scenario> scenarios;

    private final Scenario fallbackScenario;

    private ContentRepository contentRepository;

    private String name;

    private String author;

    private String date;

    private Map<String, Object> additionalParameters;

    public DefaultConfiguration(final List<Scenario> scenarios, final Scenario fallbackScenario) {
        this.scenarios = scenarios;
        this.fallbackScenario = fallbackScenario;
    }

    public List<Scenario> getScenarios() {
        return this.scenarios;
    }

    public Scenario getFallbackScenario() {
        return this.fallbackScenario;
    }

    public ContentRepository getContentRepository() {
        return this.contentRepository;
    }

    public String getName() {
        return this.name;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getDate() {
        return this.date;
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public void setAdditionalParameters(final Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }
}
