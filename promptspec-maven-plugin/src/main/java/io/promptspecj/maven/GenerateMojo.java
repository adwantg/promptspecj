package io.promptspecj.maven;

import io.promptspecj.codegen.java.GenerationRequest;
import io.promptspecj.codegen.java.JavaPromptSpecGenerator;
import io.promptspecj.parser.ParsedDocument;
import io.promptspecj.validator.PromptSpecValidator;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public final class GenerateMojo extends AbstractPromptSpecMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/promptspec", required = true)
    private File contractsDirectory;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/promptspec", required = true)
    private File generatedSourcesDirectory;

    @Parameter(defaultValue = "io.promptspecj.generated", required = true)
    private String basePackage;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        PromptSpecValidator validator = new PromptSpecValidator();
        JavaPromptSpecGenerator generator = new JavaPromptSpecGenerator();
        try {
            for (File file : contractFiles(contractsDirectory)) {
                ParsedDocument document = parser.parse(file.toPath());
                var result = validator.validate(file.toPath(), document.document(), toolRegistry(contractsDirectory.toPath()), null);
                failIfInvalid(result);
                generator.generate(new GenerationRequest(document.document(), basePackage, generatedSourcesDirectory.toPath()));
            }
            project.addCompileSourceRoot(generatedSourcesDirectory.getAbsolutePath());
        } catch (Exception ex) {
            throw executionException(ex);
        }
    }
}
