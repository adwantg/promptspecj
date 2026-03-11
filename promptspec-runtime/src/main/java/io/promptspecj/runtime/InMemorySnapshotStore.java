package io.promptspecj.runtime;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySnapshotStore implements SnapshotStore {
    private final Map<String, String> snapshots = new ConcurrentHashMap<>();

    @Override
    public Optional<String> read(String snapshotId) {
        return Optional.ofNullable(snapshots.get(snapshotId));
    }

    @Override
    public void write(String snapshotId, String content) {
        snapshots.put(snapshotId, content);
    }
}
