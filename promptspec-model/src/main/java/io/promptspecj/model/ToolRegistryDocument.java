package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record ToolRegistryDocument(@JsonProperty(value = "tools", required = true) List<ToolSpec> tools) {}
