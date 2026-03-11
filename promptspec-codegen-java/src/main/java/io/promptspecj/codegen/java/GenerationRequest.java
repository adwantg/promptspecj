package io.promptspecj.codegen.java;

import io.promptspecj.model.PromptSpecDocument;
import java.nio.file.Path;

public record GenerationRequest(PromptSpecDocument document, String basePackage, Path outputDirectory) {}
