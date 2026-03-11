plugins {
    base
}

description = "PromptSpec-J documentation project"

tasks.register("docsCheck") {
    group = "documentation"
    description = "Verifies that the documentation tree is present."
    val requiredFiles = listOf(
        layout.projectDirectory.file("getting-started.md").asFile,
        layout.projectDirectory.file("roadmap.md").asFile
    )
    doLast {
        val missing = requiredFiles.filterNot { it.exists() }
        check(missing.isEmpty()) {
            "Missing documentation files: ${missing.joinToString { it.name }}"
        }
    }
}
