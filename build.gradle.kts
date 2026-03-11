import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.signing.SigningExtension

plugins {
    base
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.nexusPublish)
    alias(libs.plugins.spotless) apply false
}

group = "io.promptspecj"
version = providers.gradleProperty("projectVersion").getOrElse("0.1.0-SNAPSHOT")

allprojects {
    group = rootProject.group
    version = rootProject.version
}

subprojects {
    if (path != ":docs" && path != ":examples:spring-boot-demo") {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        apply(plugin = "jacoco")
        apply(plugin = "com.diffplug.spotless")

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
            withSourcesJar()
            withJavadocJar()
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(17)
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }

        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                target("src/**/*.java")
                googleJavaFormat("1.25.2")
            }
            kotlinGradle {
                target("*.gradle.kts", "**/*.gradle.kts")
            }
        }

        dependencies {
            add("testImplementation", platform("org.junit:junit-bom:5.11.4"))
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
            add("testImplementation", "org.assertj:assertj-core:3.27.3")
        }

        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("PromptSpec-J module ${project.name}")
                        url.set("https://github.com/promptspecj/promptspecj")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/promptspecj/promptspecj.git")
                            developerConnection.set("scm:git:ssh://git@github.com:promptspecj/promptspecj.git")
                            url.set("https://github.com/promptspecj/promptspecj")
                        }
                    }
                }
            }
        }

        extensions.configure<SigningExtension> {
            useGpgCmd()
            sign(extensions.getByType(PublishingExtension::class.java).publications["mavenJava"])
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

jreleaser {
    project {
        name.set("PromptSpec-J")
        description.set("OpenAPI-style prompt contracts for Java and Spring AI.")
        website.set("https://github.com/promptspecj/promptspecj")
        authors.set(listOf("PromptSpec-J contributors"))
        license.set("Apache-2.0")
        copyright.set("2026")
    }
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }
    release {
        github {
            enabled.set(true)
            overwrite.set(false)
            repoOwner.set("promptspecj")
            name.set("promptspecj")
            tagName.set("v{{projectVersion}}")
            skipTag.set(false)
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

val promptSpecTargetProjects = listOf(
    rootProject,
    project(":examples:spring-boot-demo")
)

fun runtimeClasspath(projectPath: String) =
    project(projectPath).extensions.getByType(SourceSetContainer::class.java).named("main").get().runtimeClasspath

val promptSpecValidateTargets = mutableListOf<String>()
val promptSpecGenerateTargets = mutableListOf<String>()

promptSpecTargetProjects.forEach { candidate ->
    val suffix = if (candidate == rootProject) "Root" else candidate.path.split(":")
        .filter { it.isNotBlank() }
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }

    val validateTask = tasks.register("promptSpecValidate$suffix", JavaExec::class.java) {
        group = "verification"
        description = "Validates PromptSpec contracts for ${candidate.path}."
        dependsOn(":promptspec-codegen-java:classes")
        classpath = runtimeClasspath(":promptspec-codegen-java")
        mainClass.set("io.promptspecj.codegen.java.PromptSpecCli")
        args(
            "validate",
            candidate.layout.projectDirectory.dir("src/main/promptspec").asFile.absolutePath,
            candidate.layout.projectDirectory.file("promptspec.lock.json").asFile.absolutePath
        )
    }
    promptSpecValidateTargets += validateTask.name

    val generateTask = tasks.register("promptSpecGenerate$suffix", JavaExec::class.java) {
        group = "build"
        description = "Generates PromptSpec sources for ${candidate.path}."
        dependsOn(validateTask, ":promptspec-codegen-java:classes")
        classpath = runtimeClasspath(":promptspec-codegen-java")
        mainClass.set("io.promptspecj.codegen.java.PromptSpecCli")
        args(
            "generate",
            candidate.layout.projectDirectory.dir("src/main/promptspec").asFile.absolutePath,
            candidate.layout.buildDirectory.dir("generated/sources/promptspec/java").get().asFile.absolutePath,
            "io.promptspecj.generated"
        )
    }
    promptSpecGenerateTargets += generateTask.name
}

tasks.register("promptSpecValidate") {
    group = "verification"
    description = "Validates PromptSpec contracts found in this repository."
    dependsOn(promptSpecValidateTargets)
}

tasks.register("promptSpecGenerate") {
    group = "build"
    description = "Generates Java sources for PromptSpec contracts found in this repository."
    dependsOn(promptSpecGenerateTargets)
}
