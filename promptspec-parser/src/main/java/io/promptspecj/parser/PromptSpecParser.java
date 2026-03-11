package io.promptspecj.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import io.promptspecj.model.PromptSpecDocument;
import io.promptspecj.model.ToolRegistryDocument;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class PromptSpecParser {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final JsonSchema schema;

    public PromptSpecParser() {
        this(loadSchema());
    }

    public PromptSpecParser(JsonSchema schema) {
        this.schema = schema;
    }

    public ParsedDocument parse(Path source) {
        try {
            ObjectMapper mapper = mapperFor(source);
            JsonNode tree = mapper.readTree(Files.newBufferedReader(source));
            validateSchema(tree, source);
            return new ParsedDocument(source, mapper.treeToValue(tree, PromptSpecDocument.class));
        } catch (IOException ex) {
            throw new PromptSpecParseException("Failed to parse PromptSpec file " + source, ex);
        }
    }

    public ToolRegistryDocument parseToolRegistry(Path source) {
        try {
            ObjectMapper mapper = mapperFor(source);
            return mapper.readValue(Files.newBufferedReader(source), ToolRegistryDocument.class);
        } catch (IOException ex) {
            throw new PromptSpecParseException("Failed to parse tool registry " + source, ex);
        }
    }

    private void validateSchema(JsonNode tree, Path source) {
        Set<com.networknt.schema.ValidationMessage> errors = schema.validate(tree);
        if (!errors.isEmpty()) {
            throw new PromptSpecParseException(
                    "PromptSpec schema validation failed for " + source + ": " + errors, null);
        }
    }

    private ObjectMapper mapperFor(Path source) {
        String fileName = source.getFileName().toString();
        return fileName.endsWith(".json") ? jsonMapper : yamlMapper;
    }

    private static JsonSchema loadSchema() {
        try (InputStream inputStream = PromptSpecParser.class.getClassLoader()
                .getResourceAsStream("schema/promptspec-v1alpha1.schema.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("PromptSpec schema resource is missing");
            }
            JsonNode schemaNode = new ObjectMapper().readTree(inputStream);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            return factory.getSchema(schemaNode);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load PromptSpec schema", ex);
        }
    }
}
