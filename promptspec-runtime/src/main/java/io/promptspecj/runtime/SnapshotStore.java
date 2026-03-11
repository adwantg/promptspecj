package io.promptspecj.runtime;

import java.util.Optional;

public interface SnapshotStore {
    Optional<String> read(String snapshotId);

    void write(String snapshotId, String content);
}
