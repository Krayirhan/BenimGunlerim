#!/usr/bin/env pwsh
# check-release.ps1 — Release öncesi tam kalite kapısı
# Kullanım: .\scripts\check-release.ps1

Set-Location (Split-Path $PSScriptRoot)

$ErrorActionPreference = "Stop"

Write-Host "`n=== BenimGünlerim Release Kalite Kontrolü ===" -ForegroundColor Cyan

# 1. Unit testler
Write-Host "`n[1/4] Unit testler..." -ForegroundColor Yellow
.\gradlew.bat testDebugUnitTest
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Unit testler" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Unit testler" -ForegroundColor Green

# 2. Lint release
Write-Host "`n[2/4] Lint release..." -ForegroundColor Yellow
.\gradlew.bat lintRelease
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Lint release" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Lint release" -ForegroundColor Green

# 3. Release assemble
Write-Host "`n[3/4] Release build..." -ForegroundColor Yellow
.\gradlew.bat assembleRelease
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Release build" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Release build" -ForegroundColor Green

# 4. Bundle (AAB)
Write-Host "`n[4/4] Bundle release (AAB)..." -ForegroundColor Yellow
.\gradlew.bat bundleRelease
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Bundle release" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Bundle release" -ForegroundColor Green

Write-Host "`n=== TUM RELEASE KAPILARI GECTI ===" -ForegroundColor Green
Write-Host "AAB konumu: app\build\outputs\bundle\release\" -ForegroundColor Cyan
