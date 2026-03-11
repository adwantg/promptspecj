# PromptSpec-J

PromptSpec-J is a contract-first library for prompts in Java applications. It treats prompts the way API teams treat OpenAPI contracts: as versioned, reviewable, validated, generated, and testable artifacts instead of anonymous strings embedded in service code.

## What this library means in the AI ecosystem

Most of the AI application stack today is organized around runtime execution:

- model providers expose chat and completion APIs
- framework layers such as Spring AI provide prompt templating, chat clients, structured output helpers, and tool-calling abstractions
- application code wires prompts, domain types, tools, and business workflows together

PromptSpec-J is not trying to replace any of those layers. It sits above the runtime framework and below the application business logic.

That means:

- it is not a model SDK
- it is not an agent runtime
- it is not a hosted prompt registry
- it is not an evaluation platform
- it is a contract and build-governance layer for prompt-driven Java systems

In practical terms, PromptSpec-J gives Java teams a way to say:

- this prompt has a stable identifier and version
- this prompt accepts these inputs and no others
- this prompt expects this output shape
- this prompt is allowed to use only these tools
- this prompt should fail the build if its contract becomes inconsistent
- this prompt should produce generated Java types and clients instead of handwritten glue
- this prompt should have drift tests that are reviewable in CI

For Spring AI teams, PromptSpec-J acts as the missing governance layer around `PromptTemplate`, structured output conversion, and `ChatClient`. Spring AI already solves prompt rendering and model interaction well. PromptSpec-J makes those prompt definitions auditable, typed, and build-aware.

## The problem PromptSpec-J solves

Prompt engineering in production Java services is usually still managed as string engineering.

A typical service today looks like this:

- a prompt template lives inline in Java code or in a loose resource file
- placeholders are tracked manually
- expected output structure is documented in comments or implied by code
- JSON output parsing is handwritten or delegated to runtime best-effort conversion
- tool access is configured ad hoc in the execution path
- regression testing happens informally, if at all
- prompt changes are reviewed as prose, not as contract changes

This produces a recurring set of engineering problems:

### 1. Placeholder drift

The prompt text changes, but the input map does not. Or the input object changes, but the prompt text does not. That failure is usually discovered only at runtime.

### 2. Output drift

A prompt that used to return one structured shape starts returning another. The runtime parser fails late, or worse, succeeds partially and produces degraded data.

### 3. Tool surface sprawl

As tool-calling expands, prompts can accidentally gain access to tools they were never meant to invoke because tool registration is handled close to runtime wiring rather than close to the prompt contract.

### 4. Review blindness

A pull request shows a changed prompt string, but not the implied impact on inputs, output schema, generated API shape, or compatibility expectations.

### 5. CI blindness

Teams can test controllers, repositories, and serializers in CI, but prompt behavior often remains outside the normal build discipline.

### 6. Release-train mismatch

Enterprise Java teams are used to typed interfaces, code generation, schema validation, and compatibility policies. Prompt logic usually bypasses all of that and lives as untyped runtime data.

PromptSpec-J solves this by moving prompt definitions into explicit contracts and making those contracts visible to the build, to generated code, and to automated tests.

## How teams do this without PromptSpec-J today

Without a library like this, most teams assemble a fragmented workflow:

### Prompt authoring

- keep prompts in Java string literals, text blocks, or ad hoc files
- manually remember which placeholders are expected
- manually keep template variables and DTO fields in sync

### Output handling

- write parsing instructions directly into prompt prose
- parse JSON manually with Jackson
- rely on runtime conversion and hope the model keeps responding in the same shape

### Tool governance

- register tools in code near the chat client call
- depend on developer discipline to keep tool access narrow
- discover accidental tool exposure through code review or incidents

### Testing

- run prompts interactively during development
- copy outputs into docs or comments
- sometimes create brittle integration tests that are hard to replay deterministically

### Change management

- review prompt text changes without explicit compatibility markers
- have no lockfile or hash-based visibility into template or schema drift
- discover breaking changes only after deployment or during staging

This approach works for early prototypes. It does not scale well for backend teams that need repeatability, ownership, auditability, and CI-driven release control.

## What PromptSpec-J changes technically

PromptSpec-J introduces a contract lifecycle:

1. Define a prompt in YAML or JSON under `src/main/promptspec/`
2. Validate the contract before code generation or compilation
3. Generate stable Java APIs from the contract
4. Execute through Spring AI using generated wiring
5. Snapshot and diff prompt behavior in tests
6. Track compatibility through a lockfile

The contract describes:

- prompt identity and version
- template source
- declared variables and types
- expected output mode
- allowed tool names
- test cases and snapshot references

The validator enforces:

- placeholder and variable consistency
- supported variable types
- structured-output `{format}` placeholder presence
- resolvable Java output types
- well-formed and registered tool names
- lockfile compatibility checks

The code generator produces:

- typed input classes
- prompt interfaces
- Spring AI-backed clients
- metadata constants and execution metadata

The test layer provides:

- snapshot storage
- normalization
- replay-mode verification
- a path toward CI-visible prompt drift

## Architecture at a glance

- `promptspec-model`: contract AST and tool-registry types
- `promptspec-parser`: YAML and JSON parsing plus schema validation
- `promptspec-validator`: diagnostics, lockfile generation, compatibility checking
- `promptspec-runtime`: runtime abstractions such as `PromptContract`, metadata, snapshot interfaces, and lockfile models
- `promptspec-codegen-java`: Java generation and a small CLI used by root Gradle tasks
- `promptspec-spring-ai-adapter`: Spring AI execution helpers
- `promptspec-junit5`: snapshot-oriented test support
- `promptspec-gradle-plugin`: reusable Gradle plugin implementation
- `promptspec-maven-plugin`: reusable Maven plugin implementation

## Example contract

```yaml
apiVersion: promptspec/v1alpha1
prompts:
  - id: article.summary
    version: 1.0.0
    template:
      inline: "Summarize {article} using {format}"
    variables:
      - name: article
        type: string
    output:
      mode: java-type
      javaType: java.lang.String
    tools:
      - name: weather.lookup
```

## Example generated shape

At generation time, PromptSpec-J produces a stable Java surface similar to:

```java
public final class ArticleSummaryPromptInputs {
    private final String article;

    public ArticleSummaryPromptInputs(String article) {
        this.article = article;
    }

    public String article() {
        return article;
    }
}

public interface ArticleSummaryPrompt extends PromptContract<ArticleSummaryPromptInputs, String> {
    String execute(ArticleSummaryPromptInputs input);
}
```

The generated client delegates to Spring AI runtime primitives instead of inventing a new chat abstraction.

## Quickstart

1. Add a contract under `src/main/promptspec/`.
2. Add `tools.yaml` if the prompt uses a tool allowlist.
3. Run `./gradlew promptSpecValidate promptSpecGenerate`.
4. Inspect the generated sources under `build/generated/sources/promptspec/java`.
5. Use the generated client and metadata types in your application.
6. Add snapshot tests for replayable CI drift detection.

The root repo tasks also validate and generate against the sample project in [examples/spring-boot-demo](/Users/goutamadwant/Documents/OpenSource/JavaProjects/PromptSpecJ/examples/spring-boot-demo).

## Documentation

- [Getting Started](/Users/goutamadwant/Documents/OpenSource/JavaProjects/PromptSpecJ/docs/getting-started.md)
- [PromptSpec Schema](/Users/goutamadwant/Documents/OpenSource/JavaProjects/PromptSpecJ/docs/schema/promptspec-v1alpha1.schema.json)
- [Roadmap](/Users/goutamadwant/Documents/OpenSource/JavaProjects/PromptSpecJ/docs/roadmap.md)

## Current scope

The current implementation targets `0.1.0` and is intentionally narrow:

- Java-first contract format
- Spring AI adapter as the primary runtime target
- build-time validation and generation
- basic compatibility lockfile support
- snapshot-oriented test support

These are intentionally out of scope for the first release:

- hosted prompt management
- multi-language contract generation
- annotation processing
- Kotlin KSP support
- non-Spring runtime adapters
- full evaluation and benchmarking framework
