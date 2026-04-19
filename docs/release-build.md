# Release Build Guide

SealForge uses a two-step desktop packaging flow:

1. `mvn package javafx:jlink` builds the modular application artifact, stages runtime dependencies, and creates a trimmed runtime image at `target/sealforge`
2. `jpackage` combines that runtime image with the staged application modules to produce a platform-native installer

## Prerequisites

- Java 21
- Maven 3.9+
- `jpackage` available from the active JDK distribution

Windows packaging also requires WiX Toolset 3.x when producing `exe` installers. The GitHub Actions workflow installs WiX automatically on `windows-latest`.

## Local Runtime Image

```bash
mvn -DskipTests clean package javafx:jlink
```

The build produces:

- `target/sealforge-<version>.jar` as the modular application artifact
- `target/jpackage-input/` with runtime dependency jars staged for packaging
- `target/sealforge/` as the runnable app image
- `target/sealforge-<version>.zip` as a zipped runtime image

## Local Native Packaging

Linux and macOS:

```bash
SEALFORGE_PACKAGE_VERSION=0.1.0 ./scripts/release/build-installer.sh
```

If your Linux workstation does not have `dpkg-deb` available, you can still validate the `jpackage` flow with:

```bash
SEALFORGE_LINUX_PACKAGE_TYPE=app-image SEALFORGE_PACKAGE_VERSION=0.1.0 ./scripts/release/build-installer.sh
```

On macOS, `jpackage` rejects application versions whose first component is `0`. The packaging script automatically normalizes pre-`1.0.0` versions, for example `0.1.0` becomes `1.1.0` for the DMG metadata.

Windows:

```powershell
pwsh -File .\scripts\release\build-installer.ps1 -PackageVersion 0.1.0
```

Generated installers are written to `dist/jpackage/`.

## GitHub Actions Release Builds

`.github/workflows/release.yml` provides:

- an Ubuntu verification job that runs the automated test suite
- a packaging matrix for Linux, Windows, and macOS
- installer artifact uploads for every platform build
- automatic GitHub Release publication when a tag like `v0.1.0` is pushed

Manual packaging runs are also supported with `workflow_dispatch`.

## Platform Notes

- Linux currently targets `deb` packages on Ubuntu-based runners and expects both `dpkg-deb` and `fakeroot`.
- Windows currently targets unsigned `exe` installers and requires WiX.
- macOS currently produces unsigned `dmg` artifacts. Notarization and code signing are intentionally deferred until release credentials are available.
- Custom icons can be added later at `packaging/icons/sealforge.icns`, `packaging/icons/sealforge.ico`, and `packaging/icons/sealforge.png`.
