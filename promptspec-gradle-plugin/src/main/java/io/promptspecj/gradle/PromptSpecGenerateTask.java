package io.promptspecj.gradle;

import io.promptspecj.codegen.java.GenerationRequest;
import io.promptspecj.codegen.java.JavaPromptSpecGenerator;
import io.promptspecj.parser.ParsedDocument;
import io.promptspecj.validator.PromptSpecValidator;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
public abstract class PromptSpecGenerateTask extends AbstractPromptSpecTask {
    private final PromptSpecValidator validator = new PromptSpecValidator();
    private final JavaPromptSpecGenerator generator = new JavaPromptSpecGenerator();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedSourcesDirectory();

    @TaskAction
    void generateSources() throws Exception {
        var root = getContractsDirectory().get().getAsFile().toPath();
        for (var contract : contractFiles()) {
            ParsedDocument document = parser().parse(contract);
            var result = validator.validate(contract, document.document(), parseRegistry(root), null);
            failIfInvalid(result);
            generator.generate(new GenerationRequest(
                    document.document(),
                    getProject().getExtensions().getByType(PromptSpecExtension.class).getBasePackage().get(),
                    getGeneratedSourcesDirectory().get().getAsFile().toPath()));
        }
    }
}
