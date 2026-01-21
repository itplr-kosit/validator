package org.kosit.validator.server.impl;

import org.kosit.validator.model.mvrl.MVRLCompactReport;
import org.kosit.validator.model.mvrl.ResultType;
import org.kosit.validator.server.api.CompactResultDto;
import org.kosit.validator.server.api.MVRLCompactReportDto;

import java.util.Collections;
import java.util.List;

public final class MVRLCompactReportMapper {

    private MVRLCompactReportMapper() {
    }

    public static MVRLCompactReportDto toDto(MVRLCompactReport report) {
        if (report == null) {
            return null;
        }

        List<ResultType> srcResults = report.getResult() != null ? report.getResult() : Collections.emptyList();

        List<CompactResultDto> results = srcResults.stream().map(MVRLCompactReportMapper::toDto).toList();

        return new MVRLCompactReportDto(nvl(report.getAcceptable()), nvl(report.getRejected()), nvl(report.getProcessingerrors()), results);
    }

    private static CompactResultDto toDto(ResultType r) {
        String error = r.getErrordescription();
        if (error != null && error.isBlank()) {
            error = null; // leere Strings unterdrücken
        }

        return new CompactResultDto(r.getFile(), r.isSchema(), r.isSchematron(),
                r.getAcceptance() != null ? r.getAcceptance().value() : null, error);
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }
}
