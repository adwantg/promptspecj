package io.promptspecj.junit5;

import io.promptspecj.runtime.SnapshotStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FileSnapshotStore implements SnapshotStore {
    private final Path directory;

    public FileSnapshotStore(Path directory) {
        this.directory = directory;
    }

    @Override
    public Optional<String> read(String snapshotId) {
        Path target = directory.resolve(snapshotId + ".json");
        if (Files.notExists(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(target));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read snapshot " + snapshotId, ex);
        }
    }

    @Override
    public void write(String snapshotId, String content) {
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(snapshotId + ".json"), content);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write snapshot " + snapshotId, ex);
        }
    }
}
