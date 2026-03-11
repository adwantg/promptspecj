package io.promptspecj.runtime;

public record PromptLockEntry(
        String id, String version, String templateHash, String outputSchemaHash) {}
