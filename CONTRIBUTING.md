# Contributing

## Scope

SealForge is intended to be a production-oriented desktop authoring tool for Kubernetes Sealed Secrets. Contributions should preserve the local-first, security-conscious product direction.

## Expectations

- keep plaintext secret handling explicit and minimal
- do not add telemetry or background data collection
- avoid logging sensitive values
- favor small, reviewable pull requests
- add tests for behavior changes
- document architecture-impacting decisions

## Development Workflow

1. Create a topic branch.
2. Make focused changes.
3. Run `mvn test`.
4. Update docs when behavior or architecture changes.
5. Open a pull request with rationale, risks, and test evidence.

## Pull Request Checklist

- code follows existing package and naming conventions
- new behavior is covered by tests where practical
- no secret values are introduced in fixtures or screenshots
- user-facing changes include documentation updates
- security-sensitive changes explain their threat model impact

## Design Guardrails

- desktop only
- JavaFX only
- no backend service
- no persistent secret storage by default
- kubeseal remains the sealing engine for the MVP

