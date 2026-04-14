package org.kosit.validator.server.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidatorEngineDto(@JsonProperty("name") String name, @JsonProperty("version") String version,
        @JsonProperty("vendor") String vendor, @JsonProperty("build") String build) {
}
