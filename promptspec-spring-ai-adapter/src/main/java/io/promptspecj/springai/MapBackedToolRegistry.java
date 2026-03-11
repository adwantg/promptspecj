package io.promptspecj.springai;

import io.promptspecj.runtime.ToolRegistry;
import java.util.Collection;
import java.util.Map;
import org.springframework.ai.tool.ToolCallback;

public final class MapBackedToolRegistry implements ToolRegistry<ToolCallback> {
    private final Map<String, ToolCallback> delegates;

    public MapBackedToolRegistry(Map<String, ToolCallback> delegates) {
        this.delegates = Map.copyOf(delegates);
    }

    @Override
    public boolean hasTool(String name) {
        return delegates.containsKey(name);
    }

    @Override
    public ToolCallback resolve(String name) {
        return delegates.get(name);
    }

    @Override
    public Collection<String> toolNames() {
        return delegates.keySet();
    }
}
