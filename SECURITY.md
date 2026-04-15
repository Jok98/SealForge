# Security Policy

## Supported Security Posture

SealForge is designed as a local authoring tool for Kubernetes Sealed Secrets. It is not a secure secret vault and does not claim to provide persistent secure storage of plaintext secrets.

## Reporting

Please report security issues privately through the repository security advisory flow once the project is published. Do not open public issues for suspected vulnerabilities that could expose users or clusters.

## Security Boundaries

- secret values are handled locally in-memory for active authoring sessions
- SealForge should not persist plaintext secret values by default
- clipboard export is explicit and user-triggered
- process execution should prefer stdin over temp files
- logs, diagnostics, and crash output must exclude secret contents

## Threat Model Priorities

- accidental local disclosure through logs, temp files, or autosave
- incorrect certificate usage leading to unusable manifests
- misleading UX that implies secure long-term storage
- command execution failures that leak content into error messages

## Non-Goals

- server-side secret storage
- cluster secret lifecycle management
- key escrow or secret recovery
- replacement for Vault, SOPS, or External Secrets Operator

