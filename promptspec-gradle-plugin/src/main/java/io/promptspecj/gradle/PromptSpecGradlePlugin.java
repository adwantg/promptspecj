package io.promptspecj.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;

public final class PromptSpecGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        PromptSpecExtension extension = project.getExtensions().create("promptSpec", PromptSpecExtension.class);
        extension.getContractsDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/promptspec"));
        extension.getGeneratedSourcesDirectory()
                .convention(project.getLayout().getBuildDirectory().dir("generated/sources/promptspec/java"));
        extension.getBasePackage().convention("io.promptspecj.generated");
        extension.getWriteLockfile().convention(true);

        PromptSpecValidateTask validateTask = project.getTasks().create("promptSpecValidate", PromptSpecValidateTask.class, task -> {
            task.setGroup("verification");
            task.getContractsDirectory().set(extension.getContractsDirectory());
            task.getLockfile()
                    .set(project.getLayout().getProjectDirectory().file("promptspec.lock.json"));
        });

        PromptSpecGenerateTask generateTask = project.getTasks().create("promptSpecGenerate", PromptSpecGenerateTask.class, task -> {
            task.setGroup("build");
            task.getContractsDirectory().set(extension.getContractsDirectory());
            task.getGeneratedSourcesDirectory().set(extension.getGeneratedSourcesDirectory());
            task.dependsOn(validateTask);
        });

        project.getPlugins().withId("java", ignored -> {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
            java.getSourceSets().getByName("main").getJava().srcDir(extension.getGeneratedSourcesDirectory());
            project.getTasks().named("compileJava").configure(task -> task.dependsOn(generateTask));
            project.getTasks().named("check").configure(task -> task.dependsOn(validateTask));
        });
    }
}
