plugins {
    `java-gradle-plugin`
    alias(libs.plugins.pluginPublish)
}

dependencies {
    implementation(project(":promptspec-model"))
    implementation(project(":promptspec-runtime"))
    implementation(project(":promptspec-parser"))
    implementation(project(":promptspec-validator"))
    implementation(project(":promptspec-codegen-java"))
}

gradlePlugin {
    plugins {
        create("promptspec") {
            id = "io.github.adwantg.promptspec"
            implementationClass = "io.promptspecj.gradle.PromptSpecGradlePlugin"
            displayName = "PromptSpec-J Gradle Plugin"
            description = "Validates and generates PromptSpec contracts."
        }
    }
}
