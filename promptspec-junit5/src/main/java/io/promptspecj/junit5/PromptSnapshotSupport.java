package io.promptspecj.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.promptspecj.runtime.OutputNormalizer;
import io.promptspecj.runtime.SnapshotStore;

public final class PromptSnapshotSupport {
    private final SnapshotMode mode;
    private final SnapshotStore snapshotStore;
    private final OutputNormalizer normalizer;

    public PromptSnapshotSupport(SnapshotMode mode, SnapshotStore snapshotStore, OutputNormalizer normalizer) {
        this.mode = mode;
        this.snapshotStore = snapshotStore;
        this.normalizer = normalizer;
    }

    public void assertMatches(String snapshotId, String content) {
        String normalized = normalizer.normalize(content);
        switch (mode) {
            case RECORD -> snapshotStore.write(snapshotId, normalized);
            case REPLAY -> assertEquals(
                    snapshotStore.read(snapshotId)
                            .orElseThrow(() -> new AssertionError("Missing snapshot " + snapshotId)),
                    normalized,
                    "Snapshot drift detected for " + snapshotId);
            case LIVE -> {
                if (snapshotStore.read(snapshotId).isPresent()) {
                    assertEquals(snapshotStore.read(snapshotId).orElseThrow(), normalized);
                } else {
                    fail("LIVE mode requires a pre-recorded snapshot for " + snapshotId);
                }
            }
        }
    }
}
