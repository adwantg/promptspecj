package io.promptspecj.validator;

import io.promptspecj.runtime.CompatibilityReport;
import io.promptspecj.runtime.PromptLockEntry;
import io.promptspecj.runtime.PromptLockfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CompatibilityChecker {
    public CompatibilityReport compare(PromptLockfile current, PromptLockfile previous) {
        if (previous == null) {
            return CompatibilityReport.success();
        }
        Map<String, PromptLockEntry> previousById = new HashMap<>();
        for (PromptLockEntry entry : previous.prompts()) {
            previousById.put(entry.id(), entry);
        }
        List<String> violations = new ArrayList<>();
        for (PromptLockEntry entry : current.prompts()) {
            PromptLockEntry old = previousById.get(entry.id());
            if (old == null) {
                continue;
            }
            if (!old.version().equals(entry.version())
                    && old.version().split("\\.")[0].equals(entry.version().split("\\.")[0])) {
                continue;
            }
            if (!old.templateHash().equals(entry.templateHash())
                    || !old.outputSchemaHash().equals(entry.outputSchemaHash())) {
                violations.add("Prompt " + entry.id() + " changed without a compatible version update");
            }
        }
        return violations.isEmpty() ? CompatibilityReport.success() : CompatibilityReport.failure(violations);
    }
}
