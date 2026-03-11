package io.promptspecj.runtime;

public interface PromptContract<I, O> {
    O execute(I input);

    PromptExecutionMetadata metadata();
}
