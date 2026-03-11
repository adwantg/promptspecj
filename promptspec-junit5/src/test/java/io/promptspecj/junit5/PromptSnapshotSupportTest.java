package io.promptspecj.junit5;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.promptspecj.runtime.InMemorySnapshotStore;
import org.junit.jupiter.api.Test;

class PromptSnapshotSupportTest {
    @Test
    void replayModeDetectsSnapshotDrift() {
        InMemorySnapshotStore store = new InMemorySnapshotStore();
        store.write("summary", "expected");
        PromptSnapshotSupport support =
                new PromptSnapshotSupport(SnapshotMode.REPLAY, store, raw -> raw);

        assertThatThrownBy(() -> support.assertMatches("summary", "actual"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Snapshot drift detected");
    }
}
