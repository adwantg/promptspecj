dependencies {
    implementation(project(":promptspec-model"))
    implementation(project(":promptspec-parser"))
    implementation(project(":promptspec-runtime"))
    implementation(project(":promptspec-validator"))
    implementation(libs.jackson.databind)
    implementation(libs.java.poet)
}
