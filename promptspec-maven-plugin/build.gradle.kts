dependencies {
    implementation(project(":promptspec-model"))
    implementation(project(":promptspec-runtime"))
    implementation(project(":promptspec-parser"))
    implementation(project(":promptspec-validator"))
    implementation(project(":promptspec-codegen-java"))
    compileOnly(libs.maven.plugin.api)
    compileOnly("org.apache.maven:maven-core:3.9.9")
    compileOnly(libs.maven.plugin.annotations)
    testImplementation(libs.maven.plugin.api)
    testImplementation("org.apache.maven:maven-core:3.9.9")
}
