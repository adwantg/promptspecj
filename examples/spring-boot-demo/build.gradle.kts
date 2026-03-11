plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("io.promptspecj.examples.demo.DemoApplication")
}

dependencies {
    implementation(project(":promptspec-spring-ai-adapter"))
    implementation(project(":promptspec-runtime"))
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.ai.starter.model.openai)
    testImplementation(libs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}
