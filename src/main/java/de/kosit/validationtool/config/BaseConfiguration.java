package de.kosit.validationtool.config;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.Scenario;

import net.sf.saxon.s9api.Processor;

/**
 * Base configuration class.
 * 
 * @author Andreas Penski
 */
@Slf4j
public abstract class BaseConfiguration implements Configuration {

    @RequiredArgsConstructor
    @Getter
    @Setter
    protected static class RuntimeArtefacts {

        private final List<Scenario> scenarios;

        private final Scenario fallbackScenario;

        private String name;

        private String author;

        private String date;
    }

    private RuntimeArtefacts artefacts;

    protected abstract RuntimeArtefacts buildArtefacts();

    @Override
    public void build() {
        if (this.artefacts != null) {
            log.warn("Configuration already complete. Will drop previous artefacts and build again");
        }
        this.artefacts = buildArtefacts();
    }

    @Override
    public List<Scenario> getScenarios() {
        assertBuild();
        return this.artefacts.getScenarios();
    }

    @Override
    public Scenario getFallbackScenario() {
        assertBuild();
        return this.artefacts.getFallbackScenario();
    }

    private void assertBuild() {
        if (this.artefacts == null) {
            throw new IllegalStateException("Configuration");
        }
    }

    @Override
    public Processor getProcessor() {
        return ObjectFactory.createProcessor();
    }

    @Override
    public String getAuthor() {
        assertBuild();
        return this.artefacts.getAuthor();
    }

    @Override
    public String getName() {
        assertBuild();
        return this.artefacts.getName();
    }

    @Override
    public String getDate() {
        assertBuild();
        return this.artefacts.getDate();
    }
}
