package io.promptspecj.runtime;

public class PromptSpecException extends RuntimeException {
    public PromptSpecException(String message) {
        super(message);
    }

    public PromptSpecException(String message, Throwable cause) {
        super(message, cause);
    }
}
