package io.promptspecj.codegen.java;

import static org.assertj.core.api.Assertions.assertThat;

import io.promptspecj.model.OutputSpec;
import io.promptspecj.model.PromptSpecDefinition;
import io.promptspecj.model.PromptSpecDocument;
import io.promptspecj.model.TemplateSpec;
import io.promptspecj.model.VariableSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaPromptSpecGeneratorTest {
    @TempDir
    Path tempDir;

    @Test
    void generatesTypedPromptSources() throws IOException {
        PromptSpecDefinition prompt = new PromptSpecDefinition(
                "article.summary",
                "1.0.0",
                new TemplateSpec("Summarize {article} using {format}", null),
                List.of(new VariableSpec("article", "string", true, null, null, null)),
                new OutputSpec("java-type", "java.lang.String", null),
                null,
                null,
                null);
        PromptSpecDocument document = new PromptSpecDocument("promptspec/v1alpha1", List.of(prompt));

        new JavaPromptSpecGenerator().generate(new GenerationRequest(document, "io.promptspecj.generated", tempDir));

        String client = Files.readString(tempDir.resolve("io/promptspecj/generated/ArticleSummaryPromptClient.java"));
        assertThat(client).contains("class ArticleSummaryPromptClient");
        assertThat(client).contains("PromptExecutionMetadata");
    }
}
