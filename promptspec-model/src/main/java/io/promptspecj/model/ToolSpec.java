package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public record ToolSpec(@JsonProperty(value = "name", required = true) String name) {}
