package org.kosit.validator.server.impl;

import org.kosit.validator.api.compact.CompactXVRLReport;
import org.kosit.validator.api.compact.CompactXVRLReportSummary;
import org.kosit.validator.api.compact.ValidatorEngineInformation;
import org.kosit.validator.model.xvrl.XVRLDetection;
import org.kosit.validator.server.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class CompactXVRLReportSummaryMapper {

    private CompactXVRLReportSummaryMapper() {
    }

    public static CompactValidationResultsDto toDto(CompactXVRLReportSummary report) {
        if (report == null) {
            return null;
        }

        List<CompactResultDto> results = report.getReports().stream().map(CompactXVRLReportSummaryMapper::toDto).toList();

        return new CompactValidationResultsDto(nvl(report.getAcceptable()), nvl(report.getRejected()), nvl(report.getProcessingErrors()),
                results, toDto(report.getValidatorInformation()));
    }

    private static CompactResultDto toDto(CompactXVRLReport r) {
        final String ref = Optional.ofNullable(r.getFilename()).orElse("unknown");

        List<CompactResultLayerDto> layers = new ArrayList<>();

        // Schema-Layer
        CompactXVRLReport.ValidationResult schValidation = r.getSchemaValidationResult();
        List<CompactViolationDto> schemaViolations = r.getOriginal().getDetection().stream()
                .filter(d -> "xsd-violation".equals(d.getCode())).map(CompactXVRLReportSummaryMapper::toViolationDto).toList();
        layers.add(new CompactResultLayerDto("schema", r.isSchemaValid(), "XSD", schemaViolations));

        // Schematron-Layer gruppieren nach Schema (via Provenance/Location)
        List<CompactXVRLReport.ValidationResult> schematronValidations = r.getSchematronValidationResult();
        schematronValidations.forEach(res -> {
            List<CompactViolationDto> violations = res.violations().stream().map(CompactXVRLReportSummaryMapper::toViolationDto).toList();
            layers.add(new CompactResultLayerDto(res.type(), violations.isEmpty(), res.name(), violations));
        });

        return new CompactResultDto(ref, normalizeBlankToNull(r.getChecksum()), null, // processingError wird im
                                                                                      // kompakten Format oft
                                                                                      // weggelassen oder in
                                                                                      // errorSummary integriert
                normalizeBlankToNull(r.getScenario()), r.getAcceptance() != null ? r.getAcceptance().name() : null,
                normalizeBlankToNull(r.getErrorSummary()), layers);
    }

    private static CompactViolationDto toViolationDto(XVRLDetection d) {
        String message = d.getMessages().stream().flatMap(m -> m.getMessageStrings().stream()).collect(Collectors.joining("; "));

        String severity = d.getSeverity() != null ? d.getSeverity().value() : null;

        // Detail extrahieren (Zeile/Spalte oder ID)
        AtomicReference<Long> line = new AtomicReference<>();
        AtomicReference<Long> col = new AtomicReference<>();
        d.getProvenances().stream().flatMap(p -> p.getLocation().stream()).map(l -> {
            line.set(l.getLine());
            col.set(l.getColumn());
            return null;
        }).filter(s -> s != null).findFirst().orElse(null);

        return new CompactViolationDto(normalizeBlankToNull(message), normalizeBlankToNull(severity), line.get(), col.get(),
                normalizeBlankToNull(d.getId()));
    }

    private static CompactViolationDto toViolationDto(CompactXVRLReport.Violation v) {
        return new CompactViolationDto(normalizeBlankToNull(v.message()), normalizeBlankToNull(v.severity()), v.line(), v.row(),
                normalizeBlankToNull(v.id()));
    }

    private static ValidatorEngineDto toDto(ValidatorEngineInformation info) {
        if (info == null) {
            return null;
        }
        return new ValidatorEngineDto(normalizeBlankToNull(info.getName()), normalizeBlankToNull(info.getVersion()), "", "");
    }

    private static String normalizeBlankToNull(String s) {
        if (s == null) {
            return null;
        }
        return s.isBlank() ? null : s;
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }
}
