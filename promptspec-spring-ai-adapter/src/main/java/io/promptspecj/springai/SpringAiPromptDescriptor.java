package io.promptspecj.springai;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record SpringAiPromptDescriptor<I, O>(
        String promptId,
        String version,
        String template,
        String outputMode,
        Class<O> outputType,
        List<String> toolNames,
        boolean structuredOutput,
        Function<I, Map<String, Object>> inputMapper) {}
