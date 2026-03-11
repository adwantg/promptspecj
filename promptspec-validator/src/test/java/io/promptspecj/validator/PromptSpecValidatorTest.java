package io.promptspecj.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.promptspecj.model.OutputSpec;
import io.promptspecj.model.PromptSpecDefinition;
import io.promptspecj.model.PromptSpecDocument;
import io.promptspecj.model.TemplateSpec;
import io.promptspecj.model.ToolRegistryDocument;
import io.promptspecj.model.ToolSpec;
import io.promptspecj.model.VariableSpec;
import io.promptspecj.runtime.PromptLockfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PromptSpecValidatorTest {
    @TempDir
    Path tempDir;

    @Test
    void rejectsUnknownToolAndMissingFormatPlaceholder() {
        PromptSpecDefinition prompt = new PromptSpecDefinition(
                "demo.summary",
                "1.0.0",
                new TemplateSpec("Summarize {content}", null),
                List.of(new VariableSpec("content", "string", true, null, null, null)),
                new OutputSpec("java-type", "java.lang.String", null),
                List.of(new ToolSpec("weather.lookup")),
                List.of(),
                null);
        PromptSpecDocument document = new PromptSpecDocument("promptspec/v1alpha1", List.of(prompt));
        ToolRegistryDocument registry = new ToolRegistryDocument(List.of(new ToolSpec("calendar.lookup")));

        ValidationResult result =
                new PromptSpecValidator().validate(tempDir.resolve("demo.promptspec.yaml"), document, registry, null);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.diagnostics()).extracting(PromptDiagnostic::code).contains("PSJ-VAL-004", "PSJ-VAL-009");
    }

    @Test
    void reportsCompatibilityViolations() throws IOException {
        Path lockfile = tempDir.resolve("promptspec.lock.json");
        PromptLockfile oldLock = new PromptLockfile(List.of(new io.promptspecj.runtime.PromptLockEntry(
                "demo.summary", "1.0.0", "old", "old")));
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(lockfile.toFile(), oldLock);

        PromptSpecDefinition prompt = new PromptSpecDefinition(
                "demo.summary",
                "1.0.0",
                new TemplateSpec("Summarize {content} as JSON {format}", null),
                List.of(new VariableSpec("content", "string", true, null, null, null)),
                new OutputSpec("java-type", "java.lang.String", null),
                null,
                null,
                null);
        PromptSpecDocument document = new PromptSpecDocument("promptspec/v1alpha1", List.of(prompt));

        ValidationResult result =
                new PromptSpecValidator().validate(tempDir.resolve("demo.promptspec.yaml"), document, null, lockfile);

        assertThat(result.compatibilityReport().compatible()).isFalse();
        assertThat(result.diagnostics()).extracting(PromptDiagnostic::code).contains("PSJ-VAL-008");
        assertThat(Files.exists(lockfile)).isTrue();
    }
}
