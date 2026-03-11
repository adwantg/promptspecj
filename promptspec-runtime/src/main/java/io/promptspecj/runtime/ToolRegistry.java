package io.promptspecj.runtime;

import java.util.Collection;

public interface ToolRegistry<T> {
    boolean hasTool(String name);

    T resolve(String name);

    Collection<String> toolNames();
}
