package io.promptspecj.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.promptspecj.model.PromptSpecDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PromptSpecParserTest {
    @TempDir
    Path tempDir;

    @Test
    void parsesYamlContract() throws IOException {
        Path contract = tempDir.resolve("example.promptspec.yaml");
        Files.writeString(
                contract,
                """
                apiVersion: promptspec/v1alpha1
                prompts:
                  - id: article.summary
                    version: 1.0.0
                    template:
                      inline: "Summarize {article} using {format}"
                    variables:
                      - name: article
                        type: string
                    output:
                      mode: java-type
                      javaType: java.lang.String
                """);

        PromptSpecDocument document = new PromptSpecParser().parse(contract).document();

        assertThat(document.prompts()).hasSize(1);
        assertThat(document.prompts().get(0).id()).isEqualTo("article.summary");
    }

    @Test
    void rejectsSchemaViolations() throws IOException {
        Path contract = tempDir.resolve("broken.promptspec.yaml");
        Files.writeString(contract, "apiVersion: promptspec/v1alpha1\nprompts: [ { id: bad } ]\n");

        assertThatThrownBy(() -> new PromptSpecParser().parse(contract))
                .isInstanceOf(PromptSpecParseException.class)
                .hasMessageContaining("schema validation failed");
    }
}
