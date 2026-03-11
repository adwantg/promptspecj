package io.promptspecj.gradle;

import io.promptspecj.model.ToolRegistryDocument;
import io.promptspecj.parser.PromptSpecParser;
import io.promptspecj.validator.ValidationResult;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

abstract class AbstractPromptSpecTask extends DefaultTask {
    private final PromptSpecParser parser = new PromptSpecParser();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getContractsDirectory();

    @Internal
    protected PromptSpecParser parser() {
        return parser;
    }

    protected List<Path> contractFiles() {
        File root = getContractsDirectory().get().getAsFile();
        File[] files = root.listFiles((dir, name) -> name.endsWith(".promptspec.yaml") || name.endsWith(".promptspec.json"));
        if (files == null) {
            return List.of();
        }
        List<Path> paths = new ArrayList<>();
        for (File file : files) {
            paths.add(file.toPath());
        }
        return paths;
    }

    protected ToolRegistryDocument parseRegistry(Path root) {
        Path registry = root.resolve("tools.yaml");
        return registry.toFile().exists() ? parser.parseToolRegistry(registry) : null;
    }

    protected void failIfInvalid(ValidationResult result) {
        if (result.isSuccessful()) {
            return;
        }
        StringBuilder builder = new StringBuilder("PromptSpec validation failed:\n");
        result.diagnostics().forEach(diagnostic -> builder.append(diagnostic.code())
                .append(": ")
                .append(diagnostic.message())
                .append('\n'));
        throw new GradleException(builder.toString());
    }
}
