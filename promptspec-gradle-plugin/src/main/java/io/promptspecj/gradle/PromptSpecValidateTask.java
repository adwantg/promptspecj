package io.promptspecj.gradle;

import io.promptspecj.parser.ParsedDocument;
import io.promptspecj.validator.PromptSpecValidator;
import java.nio.file.Path;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.file.RegularFileProperty;

@CacheableTask
public abstract class PromptSpecValidateTask extends AbstractPromptSpecTask {
    private final PromptSpecValidator validator = new PromptSpecValidator();

    @OutputFile
    public abstract RegularFileProperty getLockfile();

    @TaskAction
    void validateContracts() throws Exception {
        Path root = getContractsDirectory().get().getAsFile().toPath();
        for (Path contract : contractFiles()) {
            ParsedDocument document = parser().parse(contract);
            var result = validator.validate(contract, document.document(), parseRegistry(root), getLockfile().getAsFile().get().toPath());
            failIfInvalid(result);
            validator.lockfileWriter().write(getLockfile().get().getAsFile().toPath(),
                    validator.lockfileWriter().create(document.document().prompts()));
        }
    }
}
