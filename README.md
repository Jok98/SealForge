# SealForge

SealForge is a local-first JavaFX desktop application for authoring Kubernetes Sealed Secrets with less CLI friction and a safer default UX.

It is not a secret manager, not a secure vault, and not a replacement for enterprise secret lifecycle platforms. Its job is narrower: help developers and platform engineers create, inspect, validate, copy, and export SealedSecret manifests locally.

## Status

This repository currently contains:

- the initial production-oriented project skeleton
- a layered architecture aligned to desktop, local-first requirements
- representative domain, use-case, infrastructure, and UI classes
- an executable JavaFX application shell
- tests for validation, YAML generation, certificate parsing, and kubeseal command/process behavior
- release and open-source readiness scaffolding

## Product Summary

SealForge supports the following MVP direction:

- load or paste a Sealed Secrets public certificate
- enter Kubernetes Secret metadata
- add and remove dynamic key/value entries
- generate deterministic Secret YAML using `stringData`
- seal the Secret through the official `kubeseal` binary
- validate the resulting SealedSecret through `kubeseal --validate`
- explicitly copy or export generated YAML
- clear sensitive form state on reset

## Security Posture

- plaintext secret values are kept in memory only for the active session
- secret values are never logged
- clipboard actions require explicit user intent
- the app avoids temp files for sealing by streaming YAML through process stdin
- the app does not autosave secret drafts
- certificate material is treated as non-secret but still tracked with source and fingerprint metadata

## Build

Requirements:

- Java 21
- Maven 3.9+
- `kubeseal` available on `PATH` or configured via `-Dsealforge.kubeseal.path=/path/to/kubeseal`

Run tests:

```bash
mvn test
```

Run the desktop application:

```bash
mvn javafx:run
```

Build a runtime image:

```bash
mvn -DskipTests clean package javafx:jlink
```

Build a native installer locally:

- Linux or macOS:

```bash
SEALFORGE_PACKAGE_VERSION=0.1.0 ./scripts/release/build-installer.sh
```

If Linux installer tooling is unavailable locally, you can still validate the `jpackage` path with:

```bash
SEALFORGE_LINUX_PACKAGE_TYPE=app-image SEALFORGE_PACKAGE_VERSION=0.1.0 ./scripts/release/build-installer.sh
```

- Windows:

```powershell
pwsh -File .\scripts\release\build-installer.ps1 -PackageVersion 0.1.0
```

## Packaging Strategy

- `jlink` for a trimmed runtime image
- `jpackage` for native installers in CI release workflows
- GitHub Actions matrix builds for Linux, Windows, and macOS

The project now includes:

- a modular runtime image build via `mvn javafx:jlink`
- cross-platform `jpackage` scripts under `scripts/release/`
- a GitHub Actions release workflow at `.github/workflows/release.yml`

See [docs/technical-specification.md](docs/technical-specification.md) for the full architecture and [docs/release-build.md](docs/release-build.md) for release packaging details.

## Open Source Notes

- Recommended license: Apache-2.0
- Please read [SECURITY.md](SECURITY.md) and [CONTRIBUTING.md](CONTRIBUTING.md) before contributing

## Screenshots

UI screenshots will be added under `docs/images/` as the application matures.
