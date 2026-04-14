package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompactValidationResultsDto(@JsonProperty("acceptable") long acceptable, @JsonProperty("rejected") long rejected,
        @JsonProperty("processing-errors") long processingErrors, @JsonProperty("results") List<CompactResultDto> results,
        @JsonProperty("validator-engine") ValidatorEngineDto validatorEngine) {
}
