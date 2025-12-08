package org.kosit.validator.server.impl;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.Processor;
import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.Result;
import org.kosit.validator.impl.DefaultCheck;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.server.config.ValidationConfig;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ValidationService {

    private final Processor processor = ProcessorProvider.getProcessor();

    private final List<Configuration> configuration;

    private final DefaultCheck check;

    public ValidationService(final ValidationConfig cfg) {
        this.configuration = getConfiguration(cfg, processor);
        check = new DefaultCheck(processor, configuration.toArray(new Configuration[0]));
    }

    /** Haupteinstieg für REST & CLI */
    public Result validate(final Input input) {
        long t0 = System.currentTimeMillis();

        // Steps zusammenstecken (war früher in processActions)
        final Result result = check.checkInput(input);

        log.info("Validated {} input in {} ms", input.getName(), System.currentTimeMillis() - t0);
        return result;
    }

    private static List<Configuration> getConfiguration(final ValidationConfig cfg, Processor processor) {
        return cfg.scenarios().stream().map(scenarioBundle -> {
            assertFileExistance(scenarioBundle.scenarioPath(), "scenario");
            final URI scenarioLocation = scenarioBundle.scenarioPath().toUri();
            final URI repositoryLocation = findRepository(scenarioLocation, scenarioBundle.repositoryOpt());

            return Configuration.load(scenarioLocation, repositoryLocation).build(processor);
        }).toList();
    }

    private static URI findRepository(final URI scenarioLocation, final Optional<Path> repositoryOpt) {
        final Path path = repositoryOpt.orElse(Paths.get(scenarioLocation).getParent());
        return determineRepository(path);
    }

    private static URI determineRepository(final Path d) {
        if (Files.isDirectory(d)) {
            return d.toUri();
        } else {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for repository definition specified: '%s'", d.toAbsolutePath()));
        }
    }

    private static void assertFileExistance(final Path f, final String type) {
        if (!Files.isRegularFile(f)) {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for %s definition specified: '%s'", type, f.toAbsolutePath()));
        }
    }
}
