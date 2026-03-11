# ADR 0001: PromptSpec v1alpha1

## Status

Accepted

## Context

PromptSpec-J needs a contract format that is easy to review, works in build tools, and keeps room for future compatibility policies without turning v0.1 into a full registry platform.

## Decision

Use a YAML-first `promptspec/v1alpha1` document root with `prompts[]`, plus JSON parity.

## Consequences

- Editors can validate against a published JSON Schema.
- Contracts remain small and review-friendly.
- The validator can enforce enterprise governance rules consistently across Maven, Gradle, and JUnit integrations.
