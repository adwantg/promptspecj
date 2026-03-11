package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = false)
public record PromptSpecDefinition(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "version", required = true) String version,
        @JsonProperty(value = "template", required = true) TemplateSpec template,
        @JsonProperty(value = "variables", required = true) List<VariableSpec> variables,
        @JsonProperty(value = "output", required = true) OutputSpec output,
        @JsonProperty("tools") List<ToolSpec> tools,
        @JsonProperty("tests") List<PromptTestCase> tests,
        @JsonProperty("metadata") Map<String, String> metadata) {}
