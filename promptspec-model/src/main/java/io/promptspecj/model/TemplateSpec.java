package io.promptspecj.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public record TemplateSpec(@JsonProperty("inline") String inline, @JsonProperty("resource") String resource) {
    public String value() {
        return inline != null ? inline : resource;
    }
}
