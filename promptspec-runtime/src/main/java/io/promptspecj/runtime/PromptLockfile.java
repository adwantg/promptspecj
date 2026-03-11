package io.promptspecj.runtime;

import java.util.List;

public record PromptLockfile(List<PromptLockEntry> prompts) {}
