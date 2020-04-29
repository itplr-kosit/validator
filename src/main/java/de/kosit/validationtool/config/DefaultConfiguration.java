package de.kosit.validationtool.config;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * Default implementation class for {@link Configuration}. This class contains all information to run a
 * {@link de.kosit.validationtool.impl.DefaultCheck}.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class DefaultConfiguration implements Configuration {

    private final List<Scenario> scenarios;

    private final Scenario fallbackScenario;

    private ContentRepository contentRepository;

    private String name;

    private String author;

    private String date;

    public Map<String, Object> additionalParameters;
}
