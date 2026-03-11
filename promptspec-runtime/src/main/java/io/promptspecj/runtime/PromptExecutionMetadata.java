package io.promptspecj.runtime;

public record PromptExecutionMetadata(
        String promptId, String promptVersion, String templateHash, String outputSchemaHash) {}
