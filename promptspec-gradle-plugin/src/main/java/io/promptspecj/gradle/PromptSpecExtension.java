package io.promptspecj.gradle;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.file.DirectoryProperty;
import javax.inject.Inject;

public abstract class PromptSpecExtension {
    @Inject
    public PromptSpecExtension(ObjectFactory objects) {}

    public abstract DirectoryProperty getContractsDirectory();

    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    public abstract Property<String> getBasePackage();

    public abstract Property<Boolean> getWriteLockfile();
}
