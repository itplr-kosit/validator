package org.kosit.validator.server.impl;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.api.compact.CompactXVRLReport;
import org.kosit.validator.api.compact.CompactXVRLReportSummary;
import org.kosit.validator.api.compact.ValidatorEngineInformation;
import org.kosit.validator.impl.DefaultCheck;
import org.kosit.validator.impl.EngineInformation;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.tasks.ScenarioSelectionAction;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.model.xvrl.XVRLDetection;
import org.kosit.validator.server.config.ValidationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import net.sf.saxon.s9api.Processor;

@ApplicationScoped
@Startup
@Named("validationService")
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    private final Processor processor = ProcessorProvider.getProcessor();

    private final List<Configuration> configuration;

    private final EngineInformation engineInformation;

    private final DefaultCheck check;

    public ValidationService(final ValidationConfig cfg, final EngineInformation engineInformation) {
        this.configuration = getConfiguration(cfg, processor);
        this.engineInformation = engineInformation;
        check = new DefaultCheck(engineInformation, processor, configuration.toArray(new Configuration[0]));
        log.info("Validator started");
    }

    public List<Scenario> getScenarios() {
        return configuration != null ? configuration.stream().flatMap(c -> c.getScenarios().stream()).toList() : Collections.emptyList();
    }

    public Result validate(final Input input) {
        long t0 = System.currentTimeMillis();
        final Result result = check.checkInput(input);
        log.info("Validated {} input in {} ms", input.getName(), System.currentTimeMillis() - t0);
        return result;
    }

    public CompactXVRLReportSummary convertMinimalXvrl(final Input input, final Result defaultResult) {
        return convertMinimalXvrl(Map.of(input, defaultResult));
    }

    public CompactXVRLReportSummary convertMinimalXvrl(final Map<Input, Result> defaultResults) {
        final CompactXVRLReportSummary summary = CompactXVRLReportSummary.create();
        defaultResults.forEach((input, result) -> {
            final CompactXVRLReport report = CompactXVRLReport.create();
            report.setFilename(input.getName());
            report.setCreator("compact-report");
            report.setScenario(detectSelectedScenario(result));
            report.setAcceptance(result.getAcceptRecommendation());
            report.setErrorSummary(joinErrors(result));
            report.addSchemaValidationResult(result.getSchemaViolations());
            /*
             * report.addSchemaReference("xsd", "XSD"); if (!result.isSchemaValid()) {
             * result.getSchemaViolations().forEach(report::addSchemaViolation); }
             */
            // Schematron-Ausgaben und deren Titel als Schema-Referenzen
            report.addSchematronValidationResults(result.getSchematronResult());
            /*
             * if (result.getSchematronResult() != null) { result.getSchematronResult().forEach(so -> { String title =
             * so.getTitle() != null ? so.getTitle() : "Schematron"; report.addSchemaReference(title, "Schematron");
             * so.getFailedAsserts().forEach(fa -> report.addSchematronViolation(fa, title)); }); }
             */
            report.setChecksum(HexFormat.of().formatHex(input.getHashCode()));
            summary.addReport(report);
        });
        summary.setAcceptable(defaultResults.values().stream().filter(Result::isAcceptable).count());
        summary.setRejected(defaultResults.values().stream().filter(r -> !r.isAcceptable()).count());
        summary.setProcessingErrors(defaultResults.values().stream().filter(r -> !r.isProcessingSuccessful()).count());
        summary.setValidatorInformation(new ValidatorEngineInformation(engineInformation.getName(), engineInformation.getVersion()));
        return summary;
    }

    private String detectSelectedScenario(Result defaultResult) {
        return defaultResult.getReportSummary().getReports().stream()
                .filter(rep -> rep.getId().equals(ScenarioSelectionAction.METADATA.getId())).findFirst()
                .map(rep -> rep.getDetection().stream().filter(d -> d.getId() != null && d.getId().equals("scenario")).findFirst()
                        .map(XVRLDetection::getCode).orElse("null"))
                .orElse("null");
    }

    private static String joinErrors(final Result value) {
        final StringBuilder b = new StringBuilder();
        b.append(String.join(";", value.getProcessingErrors()));
        if (value.getSchemaViolations() != null && !value.getSchemaViolations().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchemaViolations().stream().map(XmlError::getMessage).collect(Collectors.joining(";")));
        }
        if (value.getSchematronResult() != null && !value.getSchematronResult().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchematronResult().stream().flatMap(e -> e.getMessages().stream()).collect(Collectors.joining(";")));
        }
        return b.toString();
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
                    String.format("Not a valid path for repository definition specified: \'%s\'", d.toAbsolutePath()));
        }
    }

    private static void assertFileExistance(final Path f, final String type) {
        if (!Files.isRegularFile(f)) {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for %s definition specified: \'%s\'", type, f.toAbsolutePath()));
        }
    }

    /**
     * Is used for Readiness Healthcheck.
     * 
     * @return if at least 1 configuration available and loaded
     */
    public boolean isReady() {
        return configuration != null && !configuration.isEmpty();
    }

    /**
     * Is used for Readiness Healthcheck.
     * 
     * @return amount of configurations available and loaded
     */
    public int getConfigurationCount() {
        return configuration != null ? configuration.size() : 0;
    }
}
