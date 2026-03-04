package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompactResultLayerDto(@JsonProperty("type") String type, @JsonProperty("valid") boolean valid,
        @JsonProperty("name") String name, @JsonProperty("violations") List<CompactViolationDto> violations) {
}
