package io.promptspecj.parser;

import io.promptspecj.runtime.PromptSpecException;

public final class PromptSpecParseException extends PromptSpecException {
    public PromptSpecParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
