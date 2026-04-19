#!/usr/bin/env bash

set -euo pipefail

APP_IMAGE_PATH="${1:-target/sealforge}"
MODULE_INPUT_DIR="${2:-target/jpackage-input}"
OUTPUT_DIR="${3:-dist/jpackage}"
PACKAGE_VERSION="${SEALFORGE_PACKAGE_VERSION:-0.1.0}"
APP_NAME="${SEALFORGE_APP_NAME:-SealForge}"
APP_MODULE="${SEALFORGE_APP_MODULE:-com.sealforge/com.sealforge.app.AppLauncher}"
VENDOR_NAME="${SEALFORGE_VENDOR:-SealForge contributors}"
ICON_DIR="packaging/icons"
LINUX_PACKAGE_TYPE="${SEALFORGE_LINUX_PACKAGE_TYPE:-deb}"

normalize_macos_package_version() {
  local raw_version="$1"
  local normalized="$raw_version"
  local -a parts=()

  if [[ ! "${raw_version}" =~ ^[0-9]+(\.[0-9]+){0,2}$ ]]; then
    echo "macOS packaging requires a numeric app version with one to three dot-separated integer components." >&2
    echo "Received '${raw_version}'." >&2
    exit 1
  fi

  IFS='.' read -r -a parts <<< "${raw_version}"
  if (( parts[0] <= 0 )); then
    parts[0]=1
    normalized="$(IFS=.; echo "${parts[*]}")"
    echo "macOS packaging normalizes app version '${raw_version}' to '${normalized}' because the first component must be greater than zero." >&2
  fi

  printf '%s\n' "${normalized}"
}

if ! command -v jpackage >/dev/null 2>&1; then
  echo "jpackage is not available on PATH. Use a JDK distribution that includes jpackage." >&2
  exit 1
fi

if [[ ! -d "${APP_IMAGE_PATH}" ]]; then
  echo "Runtime image not found at ${APP_IMAGE_PATH}. Run 'mvn -DskipTests clean package javafx:jlink' first." >&2
  exit 1
fi

if [[ ! -d "${MODULE_INPUT_DIR}" ]]; then
  echo "Module input directory not found at ${MODULE_INPUT_DIR}. Run 'mvn -DskipTests clean package javafx:jlink' first." >&2
  exit 1
fi

mkdir -p "${OUTPUT_DIR}"

APP_JAR="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -n 1)"
if [[ -z "${APP_JAR}" ]]; then
  echo "Application jar not found in target/. Run 'mvn -DskipTests clean package javafx:jlink' first." >&2
  exit 1
fi

cp -f "${APP_JAR}" "${MODULE_INPUT_DIR}/"

OS_NAME="$(uname -s)"
PACKAGE_TYPE=""
ICON_PATH=""
EXTRA_ARGS=()

case "${OS_NAME}" in
  Linux)
    case "${LINUX_PACKAGE_TYPE}" in
      deb)
        if ! command -v dpkg-deb >/dev/null 2>&1 || ! command -v fakeroot >/dev/null 2>&1; then
          echo "Linux deb packaging requires both 'dpkg-deb' and 'fakeroot' on PATH." >&2
          echo "Install those tools or rerun with SEALFORGE_LINUX_PACKAGE_TYPE=app-image." >&2
          exit 1
        fi
        PACKAGE_TYPE="deb"
        EXTRA_ARGS+=("--linux-package-name" "sealforge" "--linux-shortcut")
        ;;
      app-image)
        PACKAGE_TYPE="app-image"
        ;;
      rpm)
        if ! command -v rpmbuild >/dev/null 2>&1; then
          echo "Linux rpm packaging requires 'rpmbuild' on PATH." >&2
          exit 1
        fi
        PACKAGE_TYPE="rpm"
        ;;
      *)
        echo "Unsupported SEALFORGE_LINUX_PACKAGE_TYPE='${LINUX_PACKAGE_TYPE}'. Use deb, rpm, or app-image." >&2
        exit 1
        ;;
    esac
    ICON_PATH="${ICON_DIR}/sealforge.png"
    ;;
  Darwin)
    PACKAGE_TYPE="dmg"
    ICON_PATH="${ICON_DIR}/sealforge.icns"
    PACKAGE_VERSION="$(normalize_macos_package_version "${PACKAGE_VERSION}")"
    ;;
  *)
    echo "Unsupported platform '${OS_NAME}'. Use the Windows PowerShell packaging script on Windows." >&2
    exit 1
    ;;
esac

JPACKAGE_ARGS=(
  "--type" "${PACKAGE_TYPE}"
  "--name" "${APP_NAME}"
  "--runtime-image" "${APP_IMAGE_PATH}"
  "--module-path" "${MODULE_INPUT_DIR}"
  "--module" "${APP_MODULE}"
  "--dest" "${OUTPUT_DIR}"
  "--app-version" "${PACKAGE_VERSION}"
  "--vendor" "${VENDOR_NAME}"
  "--verbose"
)

if [[ -f "${ICON_PATH}" ]]; then
  JPACKAGE_ARGS+=("--icon" "${ICON_PATH}")
fi

if [[ ${#EXTRA_ARGS[@]} -gt 0 ]]; then
  JPACKAGE_ARGS+=("${EXTRA_ARGS[@]}")
fi

echo "Building ${PACKAGE_TYPE} installer for ${APP_NAME} ${PACKAGE_VERSION}..."
jpackage "${JPACKAGE_ARGS[@]}"
echo "Installer artifacts written to ${OUTPUT_DIR}"
