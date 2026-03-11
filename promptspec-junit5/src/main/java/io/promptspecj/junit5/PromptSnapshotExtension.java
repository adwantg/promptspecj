package io.promptspecj.junit5;

import io.promptspecj.runtime.BasicOutputNormalizer;
import io.promptspecj.runtime.OutputNormalizer;
import io.promptspecj.runtime.SnapshotStore;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class PromptSnapshotExtension implements BeforeEachCallback {
    private final SnapshotMode mode;
    private final SnapshotStore snapshotStore;
    private final OutputNormalizer normalizer;

    public PromptSnapshotExtension(SnapshotMode mode, Path directory) {
        this(mode, new FileSnapshotStore(directory), new BasicOutputNormalizer());
    }

    public PromptSnapshotExtension(SnapshotMode mode, SnapshotStore snapshotStore, OutputNormalizer normalizer) {
        this.mode = mode;
        this.snapshotStore = snapshotStore;
        this.normalizer = normalizer;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.create(getClass(), context.getUniqueId()))
                .put(PromptSnapshotSupport.class, new PromptSnapshotSupport(mode, snapshotStore, normalizer));
    }
}
