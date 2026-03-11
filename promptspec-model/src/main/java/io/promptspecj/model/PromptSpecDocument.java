package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record PromptSpecDocument(
        @JsonProperty(value = "apiVersion", required = true) String apiVersion,
        @JsonProperty(value = "prompts", required = true) List<PromptSpecDefinition> prompts) {}
