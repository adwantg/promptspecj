package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = false)
public record VariableSpec(
        @JsonProperty(value = "name", required = true) String name,
        @JsonProperty(value = "type", required = true) String type,
        @JsonProperty("required") Boolean required,
        @JsonProperty("defaultValue") Object defaultValue,
        @JsonProperty("constraints") Map<String, Object> constraints,
        @JsonProperty("allowedValues") List<String> allowedValues) {
    public boolean isRequired() {
        return required == null || required;
    }
}
