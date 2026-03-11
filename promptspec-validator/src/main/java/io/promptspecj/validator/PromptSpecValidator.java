package io.promptspecj.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.promptspecj.model.PromptSpecDefinition;
import io.promptspecj.model.PromptSpecDocument;
import io.promptspecj.model.ToolRegistryDocument;
import io.promptspecj.model.ToolSpec;
import io.promptspecj.model.VariableSpec;
import io.promptspecj.runtime.CompatibilityReport;
import io.promptspecj.runtime.PromptLockfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PromptSpecValidator {
    private static final Set<String> SUPPORTED_VARIABLE_TYPES =
            Set.of("string", "int", "long", "boolean", "number", "enum");

    private final PlaceholderExtractor placeholderExtractor = new PlaceholderExtractor();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CompatibilityChecker compatibilityChecker = new CompatibilityChecker();
    private final PromptLockfileWriter lockfileWriter = new PromptLockfileWriter();

    public ValidationResult validate(
            Path source, PromptSpecDocument document, ToolRegistryDocument toolRegistry, Path existingLockfile) {
        List<PromptDiagnostic> diagnostics = new ArrayList<>();
        Set<String> allowedTools = new HashSet<>();
        if (toolRegistry != null && toolRegistry.tools() != null) {
            for (ToolSpec tool : toolRegistry.tools()) {
                allowedTools.add(tool.name());
            }
        }

        for (PromptSpecDefinition prompt : document.prompts()) {
            validatePrompt(source, prompt, allowedTools, diagnostics);
        }

        PromptLockfile current = lockfileWriter.create(document.prompts());
        CompatibilityReport report = checkCompatibility(existingLockfile, current);
        for (String violation : report.violations()) {
            diagnostics.add(new PromptDiagnostic(
                    "PSJ-VAL-008",
                    DiagnosticSeverity.ERROR,
                    violation,
                    "Bump the contract version or restore the previous compatible schema.",
                    source,
                    1,
                    1));
        }
        return new ValidationResult(List.copyOf(diagnostics), report);
    }

    public PromptLockfileWriter lockfileWriter() {
        return lockfileWriter;
    }

    private CompatibilityReport checkCompatibility(Path existingLockfile, PromptLockfile current) {
        if (existingLockfile == null || Files.notExists(existingLockfile)) {
            return CompatibilityReport.success();
        }
        try {
            PromptLockfile previous = objectMapper.readValue(existingLockfile.toFile(), PromptLockfile.class);
            return compatibilityChecker.compare(current, previous);
        } catch (IOException ex) {
            return CompatibilityReport.failure(List.of("Failed to read lockfile: " + ex.getMessage()));
        }
    }

    private void validatePrompt(
            Path source, PromptSpecDefinition prompt, Set<String> allowedTools, List<PromptDiagnostic> diagnostics) {
        Set<String> placeholders = placeholderExtractor.extract(prompt.template().inline());
        Set<String> declaredVariables = new HashSet<>();
        for (VariableSpec variable : prompt.variables()) {
            declaredVariables.add(variable.name());
            if (!SUPPORTED_VARIABLE_TYPES.contains(variable.type())) {
                diagnostics.add(new PromptDiagnostic(
                        "PSJ-VAL-002",
                        DiagnosticSeverity.ERROR,
                        "Unsupported variable type '" + variable.type() + "' for " + variable.name(),
                        "Use one of " + SUPPORTED_VARIABLE_TYPES,
                        source,
                        1,
                        1));
            }
        }
        for (String placeholder : placeholders) {
            if (!declaredVariables.contains(placeholder) && !"format".equals(placeholder)) {
                diagnostics.add(new PromptDiagnostic(
                        "PSJ-VAL-001",
                        DiagnosticSeverity.ERROR,
                        "Placeholder {" + placeholder + "} is not declared in variables",
                        "Add the variable declaration or remove the placeholder from the template.",
                        source,
                        1,
                        1));
            }
        }
        for (String declaredVariable : declaredVariables) {
            if (!placeholders.contains(declaredVariable)) {
                diagnostics.add(new PromptDiagnostic(
                        "PSJ-VAL-003",
                        DiagnosticSeverity.WARNING,
                        "Variable '" + declaredVariable + "' is declared but not used",
                        "Remove the variable or reference it in the template.",
                        source,
                        1,
                        1));
            }
        }
        if (prompt.output().requiresStructuredOutput() && !placeholders.contains("format")) {
            diagnostics.add(new PromptDiagnostic(
                    "PSJ-VAL-004",
                    DiagnosticSeverity.ERROR,
                    "Structured output prompt '" + prompt.id() + "' must declare a {format} placeholder",
                    "Add {format} to the template text.",
                    source,
                    1,
                    1));
        }
        if ("java-type".equals(prompt.output().mode())) {
            if (prompt.output().javaType() == null || prompt.output().javaType().isBlank()) {
                diagnostics.add(new PromptDiagnostic(
                        "PSJ-VAL-005",
                        DiagnosticSeverity.ERROR,
                        "Prompt '" + prompt.id() + "' must declare output.javaType",
                        "Provide a fully qualified Java type.",
                        source,
                        1,
                        1));
            } else {
                try {
                    Class.forName(prompt.output().javaType(), false, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException ex) {
                    diagnostics.add(new PromptDiagnostic(
                            "PSJ-VAL-006",
                            DiagnosticSeverity.ERROR,
                            "Output type '" + prompt.output().javaType() + "' is not resolvable",
                            "Add the type to the project classpath or change the contract.",
                            source,
                            1,
                            1));
                }
            }
        }
        if (prompt.tools() != null) {
            for (ToolSpec tool : prompt.tools()) {
                if (!tool.name().matches("^[a-zA-Z0-9._-]+$")) {
                    diagnostics.add(new PromptDiagnostic(
                            "PSJ-VAL-007",
                            DiagnosticSeverity.ERROR,
                            "Tool name '" + tool.name() + "' is malformed",
                            "Use only letters, numbers, dots, underscores, and dashes.",
                            source,
                            1,
                            1));
                } else if (!allowedTools.isEmpty() && !allowedTools.contains(tool.name())) {
                    diagnostics.add(new PromptDiagnostic(
                            "PSJ-VAL-009",
                            DiagnosticSeverity.ERROR,
                            "Tool '" + tool.name() + "' is not present in the tool registry",
                            "Register the tool in src/main/promptspec/tools.yaml.",
                            source,
                            1,
                            1));
                }
            }
        }
    }
}
