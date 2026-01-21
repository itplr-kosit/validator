package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompactResultDto(@JsonProperty("file") String file, @JsonProperty("schema") boolean schema,
        @JsonProperty("schematron") boolean schematron, @JsonProperty("acceptance") String acceptance,
        @JsonProperty("errorDescription") String errorDescription) {
}
