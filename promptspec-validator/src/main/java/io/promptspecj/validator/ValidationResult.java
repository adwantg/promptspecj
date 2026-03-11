package io.promptspecj.validator;

import io.promptspecj.runtime.CompatibilityReport;
import java.util.List;

public record ValidationResult(
        List<PromptDiagnostic> diagnostics, CompatibilityReport compatibilityReport) {
    public boolean isSuccessful() {
        return diagnostics.stream().noneMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR);
    }
}
