pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "promptspecj"

include(
    "docs",
    "promptspec-model",
    "promptspec-parser",
    "promptspec-validator",
    "promptspec-runtime",
    "promptspec-codegen-java",
    "promptspec-spring-ai-adapter",
    "promptspec-junit5",
    "promptspec-maven-plugin",
    "promptspec-gradle-plugin",
    "examples:spring-boot-demo"
)
