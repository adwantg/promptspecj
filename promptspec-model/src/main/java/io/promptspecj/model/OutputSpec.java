package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = false)
public record OutputSpec(
        @JsonProperty(value = "mode", required = true) String mode,
        @JsonProperty("javaType") String javaType,
        @JsonProperty("jsonSchema") JsonNode jsonSchema) {
    public boolean requiresStructuredOutput() {
        return "java-type".equals(mode) || "json-schema".equals(mode);
    }
}
