package io.promptspecj.springai;

import io.promptspecj.runtime.PromptSpecException;
import io.promptspecj.runtime.ToolRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.tool.ToolCallback;

public final class SpringAiPromptExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAiPromptExecutor.class);

    private final ChatClient chatClient;

    public SpringAiPromptExecutor(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public <I, O> O execute(
            SpringAiPromptDescriptor<I, O> descriptor, I input, ToolRegistry<ToolCallback> toolRegistry) {
        Map<String, Object> variables = new LinkedHashMap<>(descriptor.inputMapper().apply(input));

        ChatClient.CallResponseSpec responseSpec;
        if (descriptor.structuredOutput() && !"json-schema".equals(descriptor.outputMode())) {
            BeanOutputConverter<O> converter = new BeanOutputConverter<>(descriptor.outputType());
            variables.put("format", converter.getFormat());
            responseSpec = chatClient.prompt()
                    .user(render(descriptor.template(), variables))
                    .toolCallbacks(resolveTools(descriptor, toolRegistry))
                    .call();
            LOGGER.debug("Executing structured prompt {}", descriptor.promptId());
            return responseSpec.entity(converter);
        }
        if ("json-schema".equals(descriptor.outputMode())) {
            MapOutputConverter converter = new MapOutputConverter();
            variables.put("format", converter.getFormat());
            responseSpec = chatClient.prompt()
                    .user(render(descriptor.template(), variables))
                    .toolCallbacks(resolveTools(descriptor, toolRegistry))
                    .call();
            return descriptor.outputType().cast(responseSpec.entity(converter));
        }

        responseSpec = chatClient.prompt()
                .user(render(descriptor.template(), variables))
                .toolCallbacks(resolveTools(descriptor, toolRegistry))
                .call();
        return descriptor.outputType().cast(responseSpec.content());
    }

    private String render(String template, Map<String, Object> variables) {
        return PromptTemplate.builder().template(template).variables(variables).build().render(variables);
    }

    private ToolCallback[] resolveTools(
            SpringAiPromptDescriptor<?, ?> descriptor, ToolRegistry<ToolCallback> toolRegistry) {
        return descriptor.toolNames().stream()
                .map(toolName -> {
                    if (!toolRegistry.hasTool(toolName)) {
                        throw new PromptSpecException("Unknown tool '" + toolName + "' for prompt " + descriptor.promptId());
                    }
                    return toolRegistry.resolve(toolName);
                })
                .toArray(ToolCallback[]::new);
    }
}
