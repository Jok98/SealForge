[CmdletBinding()]
param(
    [string]$AppImagePath = "target/sealforge",
    [string]$ModuleInputDir = "target/jpackage-input",
    [string]$OutputDir = "dist/jpackage",
    [string]$PackageVersion = $env:SEALFORGE_PACKAGE_VERSION,
    [string]$AppName = $env:SEALFORGE_APP_NAME,
    [string]$AppModule = $env:SEALFORGE_APP_MODULE,
    [string]$VendorName = $env:SEALFORGE_VENDOR
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($PackageVersion)) {
    $PackageVersion = "0.1.0"
}

if ([string]::IsNullOrWhiteSpace($AppName)) {
    $AppName = "SealForge"
}

if ([string]::IsNullOrWhiteSpace($VendorName)) {
    $VendorName = "SealForge contributors"
}

if ([string]::IsNullOrWhiteSpace($AppModule)) {
    $AppModule = "com.sealforge/com.sealforge.app.AppLauncher"
}

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage is not available on PATH. Use a JDK distribution that includes jpackage."
}

if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    throw "WiX Toolset 3.x is required for Windows EXE packaging. Install WiX or use the GitHub Actions release workflow."
}

if (-not (Test-Path -Path $AppImagePath -PathType Container)) {
    throw "Runtime image not found at '$AppImagePath'. Run 'mvn -DskipTests clean package javafx:jlink' first."
}

if (-not (Test-Path -Path $ModuleInputDir -PathType Container)) {
    throw "Module input directory not found at '$ModuleInputDir'. Run 'mvn -DskipTests clean package javafx:jlink' first."
}

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

$appJar = Get-ChildItem -Path "target" -Filter "*.jar" -File |
    Where-Object { $_.Name -notmatch "(-sources|-javadoc)\\.jar$" } |
    Select-Object -First 1

if (-not $appJar) {
    throw "Application jar not found in target/. Run 'mvn -DskipTests clean package javafx:jlink' first."
}

Copy-Item -Path $appJar.FullName -Destination $ModuleInputDir -Force

$jpackageArgs = @(
    "--type", "exe",
    "--name", $AppName,
    "--runtime-image", $AppImagePath,
    "--module-path", $ModuleInputDir,
    "--module", $AppModule,
    "--dest", $OutputDir,
    "--app-version", $PackageVersion,
    "--vendor", $VendorName,
    "--win-menu",
    "--win-shortcut",
    "--win-dir-chooser",
    "--verbose"
)

$iconPath = "packaging/icons/sealforge.ico"
if (Test-Path -Path $iconPath -PathType Leaf) {
    $jpackageArgs += @("--icon", $iconPath)
}

Write-Host "Building Windows installer for $AppName $PackageVersion..."
& jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Installer artifacts written to $OutputDir"
