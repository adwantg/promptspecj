package io.promptspecj.springai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.tool.ToolCallback;

class MapBackedToolRegistryTest {
    @Test
    void exposesRegisteredTools() {
        ToolCallback callback = Mockito.mock(ToolCallback.class);
        MapBackedToolRegistry registry = new MapBackedToolRegistry(Map.of("weather.lookup", callback));

        assertThat(registry.hasTool("weather.lookup")).isTrue();
        assertThat(registry.resolve("weather.lookup")).isSameAs(callback);
    }
}
