package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MVRLCompactReportDto(@JsonProperty("acceptable") long acceptable, @JsonProperty("rejected") long rejected,
        @JsonProperty("processingErrors") long processingErrors, @JsonProperty("results") List<CompactResultDto> results) {
}
