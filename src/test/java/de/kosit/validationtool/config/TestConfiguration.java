package de.kosit.validationtool.config;

import java.util.List;
import java.util.Map;

import lombok.Data;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * @author Andreas Penski
 */
@Data
public class TestConfiguration implements Configuration {

    private List<Scenario> scenarios;

    private Scenario fallbackScenario;

    private String author;

    private String name;

    private String date;

    private ContentRepository contentRepository;

    private Map<String, Object> additionalParameters;
}
