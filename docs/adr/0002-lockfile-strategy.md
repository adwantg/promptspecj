# ADR 0002: Lockfile strategy

## Status

Accepted

## Decision

Generate `promptspec.lock.json` from validated contracts and compare prompt id, version, template hash, and output schema hash during validation.

## Rationale

This keeps compatibility checks deterministic and auditable in CI without requiring a hosted registry.
