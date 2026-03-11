package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = false)
public record PromptTestCase(
        @JsonProperty(value = "name", required = true) String name,
        @JsonProperty(value = "inputs", required = true) Map<String, Object> inputs,
        @JsonProperty(value = "snapshot", required = true) String snapshot) {}
