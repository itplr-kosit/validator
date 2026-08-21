package org.kosit.validator.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * Repository for the active scenarios of a validation instance.
 *
 * @author Andreas Penski
 */
public class ScenarioRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioRepository.class);

    public static final String DEFAULT = "default";

    public static final String DEFAULT_ID = DEFAULT + "_1";

    private final List<VConfiguration> configuration;

    public ScenarioRepository(final VConfiguration... configuration) {
        if (configuration.length == 0) {
            throw new IllegalArgumentException("Must provide at least one configuration");
        }
        this.configuration = Arrays.asList(configuration);
        this.configuration.forEach(v -> LOGGER.info("Loaded scenarios for {} by {} from {}.", v.getName(), v.getAuthor(), v.getDate()));
        LOGGER.info("The following scenarios are available:\n{}", summarizeScenarios());
    }

    public Scenario getFallbackScenario() {
        if (this.configuration.size() > 1) {
            LOGGER.warn("Multiple configurations found. Using fallback scenario from first configuration");
        }
        return this.configuration.get(0).getFallbackScenario();
    }

    public List<Scenario> getScenarios() {
        return this.configuration.stream().flatMap(c -> c.getScenarios().stream()).collect(Collectors.toList());
    }

    private String summarizeScenarios() {
        final StringBuilder b = new StringBuilder();
        getScenarios().forEach(s -> b.append(s.getName()).append('\n'));
        return b.toString();
    }

    /**
     * Determines <b>all</b> scenarios whose match expression fires for the provided document (conformatron-api step 3,
     * {@code DETECT_SCENARIOS}). Evaluation errors of single match expressions are logged and treated as non-match
     * (legacy parity).
     *
     * @param document input document
     * @return all matching scenarios, in configuration order; may be empty
     */
    public List<Scenario> findMatches(final XdmNode document) {
        return getScenarios().stream().filter(s -> match(document, s)).collect(Collectors.toList());
    }

    /**
     * Determine the matching Scenario for the provided input document
     *
     * @param document input document
     * @return a result object for further processing
     */
    public Result<Scenario, String> selectScenario(final XdmNode document) {
        final Result<Scenario, String> result;
        final List<Scenario> collect = findMatches(document);
        if (collect.size() == 1) {
            result = new Result<>(collect.get(0));
        } else if (collect.isEmpty()) {
            result = new Result<>(getFallbackScenario(),
                    Collections.singleton("None of the loaded scenarios matches the specified document"));
        } else {
            result = new Result<>(getFallbackScenario(), Collections.singleton("More than one scenario matches the specified document"));
        }
        return result;
    }

    private static boolean match(final XdmNode document, final Scenario scenario) {
        try {
            final XPathSelector selector = scenario.getMatchSelector();
            selector.setContextItem(document);
            return selector.effectiveBooleanValue();
        } catch (final SaxonApiException e) {
            LOGGER.error("Error evaluating xpath expression", e);
        }
        return false;
    }
}
