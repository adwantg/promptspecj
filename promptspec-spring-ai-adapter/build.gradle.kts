dependencies {
    implementation(project(":promptspec-runtime"))
    implementation(libs.spring.ai.core)
    implementation(libs.slf4j.api)
    testImplementation(libs.mockito.core)
}
