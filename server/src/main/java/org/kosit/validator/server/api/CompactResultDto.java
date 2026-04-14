package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompactResultDto(@JsonProperty("ref") String ref,

        @JsonProperty("checksum") String checksum, @JsonProperty("processing-error") String processingError,
        @JsonProperty("scenario") String scenario,

        @JsonProperty("acceptance") String acceptance, @JsonProperty("error-summary") String errorSummary,

        @JsonProperty("layers") List<CompactResultLayerDto> layers) {
}