package io.promptspecj.validator;

import java.nio.file.Path;

public record PromptDiagnostic(
        String code,
        DiagnosticSeverity severity,
        String message,
        String remediation,
        Path file,
        Integer line,
        Integer column) {}
