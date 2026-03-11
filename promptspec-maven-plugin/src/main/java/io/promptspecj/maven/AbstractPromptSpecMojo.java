package io.promptspecj.maven;

import io.promptspecj.model.ToolRegistryDocument;
import io.promptspecj.parser.PromptSpecParser;
import io.promptspecj.validator.ValidationResult;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

abstract class AbstractPromptSpecMojo extends AbstractMojo {
    protected final PromptSpecParser parser = new PromptSpecParser();

    protected List<File> contractFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".promptspec.yaml") || name.endsWith(".promptspec.json"));
        return files == null ? List.of() : Arrays.asList(files);
    }

    protected ToolRegistryDocument toolRegistry(Path directory) {
        Path registry = directory.resolve("tools.yaml");
        return registry.toFile().exists() ? parser.parseToolRegistry(registry) : null;
    }

    protected void failIfInvalid(ValidationResult result) throws MojoFailureException {
        if (result.isSuccessful()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        result.diagnostics().forEach(diagnostic -> builder.append(diagnostic.code())
                .append(": ")
                .append(diagnostic.message())
                .append('\n'));
        throw new MojoFailureException(builder.toString());
    }

    protected MojoExecutionException executionException(Exception ex) {
        return new MojoExecutionException(ex.getMessage(), ex);
    }
}
