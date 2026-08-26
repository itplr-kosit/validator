package org.kosit.validator.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.kosit.base.string.StringHelper;
import org.kosit.validator.api.xvrl.compact.CompactXVRLReport;
import org.kosit.validator.api.xvrl.compact.CompactXVRLReportSummary;
import org.kosit.validator.api.xvrl.compact.ValidatorEngineInformation;
import org.kosit.validator.server.api.CompactResultDto;
import org.kosit.validator.server.api.CompactResultLayerDto;
import org.kosit.validator.server.api.CompactValidationResultsDto;
import org.kosit.validator.server.api.CompactViolationDto;
import org.kosit.validator.server.api.ValidatorEngineDto;
import org.kosit.xvrl.model.XVRLDetectionType;

public final class CompactXVRLReportSummaryMapper {

    private CompactXVRLReportSummaryMapper() {
    }

    public static CompactValidationResultsDto toDto(final CompactXVRLReportSummary report) {
        if (report == null) {
            return null;
        }

        final List<CompactResultDto> results = report.getReports().stream().map(CompactXVRLReportSummaryMapper::toDto).toList();

        return new CompactValidationResultsDto(StringHelper.nvl(report.getAcceptable()), StringHelper.nvl(report.getRejected()),
                StringHelper.nvl(report.getProcessingErrors()), results, toDto(report.getValidatorInformation()));
    }

    private static CompactResultDto toDto(final CompactXVRLReport r) {
        final String ref = Optional.ofNullable(r.getFilename()).orElse("unknown");

        final List<CompactResultLayerDto> layers = new ArrayList<>();

        // Schema-Layer
        final CompactXVRLReport.ValidationResult schValidation = r.getSchemaValidationResult();
        final List<CompactViolationDto> schemaViolations = r.getOriginal().getDetection().stream()
                .filter(d -> "xsd-violation".equals(d.getCode())).map(CompactXVRLReportSummaryMapper::toViolationDto).toList();
        layers.add(new CompactResultLayerDto("schema", r.isSchemaValid(), "XSD", schemaViolations));

        // group Schematron layers by schema (via Provenance/Location)
        final List<CompactXVRLReport.ValidationResult> schematronValidations = r.getSchematronValidationResult();
        schematronValidations.forEach(res -> {
            final List<CompactViolationDto> violations = res.violations().stream().map(CompactXVRLReportSummaryMapper::toViolationDto)
                    .toList();
            layers.add(new CompactResultLayerDto(res.type(), violations.isEmpty(), res.name(), violations));
        });

        // processingError is often omitted in the compact format or integrated into errorSummary
        return new CompactResultDto(ref, StringHelper.normalizeBlankToNull(r.getChecksum()), null,
                StringHelper.normalizeBlankToNull(r.getScenario()), r.getAcceptance() != null ? r.getAcceptance().name() : null,
                StringHelper.normalizeBlankToNull(r.getErrorSummary()), layers);
    }

    private static CompactViolationDto toViolationDto(final XVRLDetectionType d) {
        final String message = d.getMessages().stream().flatMap(m -> m.getMessageStrings().stream()).collect(Collectors.joining("; "));

        final String severity = d.getSeverity() != null ? d.getSeverity().value() : null;

        // extract detail (line/column or ID)
        final AtomicReference<Long> line = new AtomicReference<>();
        final AtomicReference<Long> col = new AtomicReference<>();
        d.getProvenances().stream().flatMap(p -> p.getLocation().stream()).map(l -> {
            line.set(l.getLine());
            col.set(l.getColumn());
            return null;
        }).filter(s -> s != null).findFirst().orElse(null);

        return new CompactViolationDto(StringHelper.normalizeBlankToNull(message), StringHelper.normalizeBlankToNull(severity), line.get(),
                col.get(), StringHelper.normalizeBlankToNull(d.getId()));
    }

    private static CompactViolationDto toViolationDto(final CompactXVRLReport.Violation v) {
        return new CompactViolationDto(StringHelper.normalizeBlankToNull(v.message()), StringHelper.normalizeBlankToNull(v.severity()),
                v.line(), v.row(), StringHelper.normalizeBlankToNull(v.id()));
    }

    private static ValidatorEngineDto toDto(final ValidatorEngineInformation info) {
        if (info == null) {
            return null;
        }
        return new ValidatorEngineDto(StringHelper.normalizeBlankToNull(info.name()), StringHelper.normalizeBlankToNull(info.version()), "",
                "");
    }

}
