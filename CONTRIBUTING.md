# Contributing

## Development baseline

- Java 17 is the minimum supported version.
- Gradle is the primary build tool for the repo.
- CI runs the test suite on Java 17 and Java 21.

## Workflow

1. Open an issue or comment on an existing issue before large changes.
2. Keep PromptSpec format changes behind ADRs in `docs/adr/`.
3. Add tests for parser, validator, code generation, or plugin behavior with every change.
4. Keep public APIs stable unless the change is explicitly versioned.

## Local checks

- `./gradlew test`
- `./gradlew promptSpecValidate`
- `./gradlew publishToMavenLocal`

## Release discipline

- Contracts, lockfile behavior, and generated APIs are treated as compatibility-sensitive.
- Breaking changes require an ADR and migration notes.
