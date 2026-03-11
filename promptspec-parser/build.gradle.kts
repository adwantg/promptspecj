dependencies {
    implementation(project(":promptspec-model"))
    implementation(project(":promptspec-runtime"))
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.json.schema.validator)
}
