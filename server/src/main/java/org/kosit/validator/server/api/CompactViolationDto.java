package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompactViolationDto(@JsonProperty("message") String message, @JsonProperty("severity") String severity,
        @JsonProperty("detail") String detail) {
}
