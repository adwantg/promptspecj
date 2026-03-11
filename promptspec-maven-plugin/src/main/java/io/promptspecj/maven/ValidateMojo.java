package io.promptspecj.maven;

import io.promptspecj.parser.ParsedDocument;
import io.promptspecj.validator.PromptSpecValidator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class ValidateMojo extends AbstractPromptSpecMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/promptspec", required = true)
    private java.io.File contractsDirectory;

    @Parameter(defaultValue = "${project.basedir}/promptspec.lock.json", required = true)
    private java.io.File lockfile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        PromptSpecValidator validator = new PromptSpecValidator();
        try {
            for (java.io.File file : contractFiles(contractsDirectory)) {
                ParsedDocument document = parser.parse(file.toPath());
                var result = validator.validate(
                        file.toPath(), document.document(), toolRegistry(contractsDirectory.toPath()), lockfile.toPath());
                failIfInvalid(result);
                validator.lockfileWriter().write(
                        lockfile.toPath(), validator.lockfileWriter().create(document.document().prompts()));
            }
        } catch (Exception ex) {
            throw executionException(ex);
        }
    }
}
