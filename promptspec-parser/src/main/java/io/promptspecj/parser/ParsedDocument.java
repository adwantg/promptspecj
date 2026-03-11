package io.promptspecj.parser;

import io.promptspecj.model.PromptSpecDocument;
import java.nio.file.Path;

public record ParsedDocument(Path source, PromptSpecDocument document) {}
