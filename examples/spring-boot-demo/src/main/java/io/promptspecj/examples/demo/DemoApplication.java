package io.promptspecj.examples.demo;

import io.promptspecj.springai.MapBackedToolRegistry;
import io.promptspecj.springai.SpringAiPromptDescriptor;
import io.promptspecj.springai.SpringAiPromptExecutor;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    SpringAiPromptDescriptor<Map<String, Object>, String> samplePromptDescriptor() {
        return new SpringAiPromptDescriptor<>(
                "article.summary",
                "1.0.0",
                "Summarize {article} using {format}",
                "java-type",
                String.class,
                List.of("weather.lookup"),
                true,
                input -> input);
    }

    @Bean
    SpringAiPromptExecutor springAiPromptExecutor(ChatModel chatModel) {
        return new SpringAiPromptExecutor(ChatClient.builder(chatModel).build());
    }

    @Bean
    MapBackedToolRegistry toolRegistry() {
        return new MapBackedToolRegistry(Map.<String, ToolCallback>of());
    }
}
