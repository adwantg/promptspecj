plugins {
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("com.diffplug.spotless")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.25.2")
    }
    kotlinGradle {
        target("*.gradle.kts", "build-logic/**/*.gradle.kts")
    }
}

dependencies {
    "testImplementation"(platform("org.junit:junit-bom:5.12.0"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testImplementation"("org.assertj:assertj-core:3.27.3")
}

publishing {
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

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
