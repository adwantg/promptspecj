package io.promptspecj.runtime;

import java.util.List;

public record CompatibilityReport(boolean compatible, List<String> violations) {
    public static CompatibilityReport success() {
        return new CompatibilityReport(true, List.of());
    }

    public static CompatibilityReport failure(List<String> violations) {
        return new CompatibilityReport(false, List.copyOf(violations));
    }
}
