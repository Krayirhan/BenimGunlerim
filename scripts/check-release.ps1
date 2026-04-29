#!/usr/bin/env pwsh
# Release öncesi tam kalite kapısı.

param(
    [switch]$RequirePerformanceGate
)

Set-Location (Split-Path $PSScriptRoot)

$ErrorActionPreference = "Stop"

Write-Host "`n=== BenimGünlerim Release Kalite Kontrolü ===" -ForegroundColor Cyan

Write-Host "`n[1/5] Release signing..." -ForegroundColor Yellow
.\gradlew.bat verifyReleaseSigning
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Release signing" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Release signing" -ForegroundColor Green

Write-Host "`n[2/5] Unit testler..." -ForegroundColor Yellow
.\gradlew.bat testDebugUnitTest
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Unit testler" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Unit testler" -ForegroundColor Green

Write-Host "`n[3/6] Coverage..." -ForegroundColor Yellow
.\gradlew.bat jacocoDebugUnitTestCoverageVerification
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Coverage" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Coverage" -ForegroundColor Green

Write-Host "`n[4/7] Static analysis (detekt)..." -ForegroundColor Yellow
.\gradlew.bat detekt
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Detekt" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Detekt" -ForegroundColor Green

Write-Host "`n[5/7] Lint release..." -ForegroundColor Yellow
.\gradlew.bat lintRelease
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Lint release" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Lint release" -ForegroundColor Green

Write-Host "`n[6/7] Release build..." -ForegroundColor Yellow
.\gradlew.bat assembleRelease
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Release build" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Release build" -ForegroundColor Green

Write-Host "`n[7/7] Bundle release (AAB)..." -ForegroundColor Yellow
.\gradlew.bat bundleRelease
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Bundle release" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Bundle release" -ForegroundColor Green

if ($RequirePerformanceGate) {
	Write-Host "`n[8/8] Startup performance gate..." -ForegroundColor Yellow
	.\check-performance-gate.ps1
	if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Startup performance gate" -ForegroundColor Red; exit 1 }
	Write-Host "PASS: Startup performance gate" -ForegroundColor Green
}

Write-Host "`n=== TÜM RELEASE KAPILARI GEÇTİ ===" -ForegroundColor Green
Write-Host "AAB konumu: app\build\outputs\bundle\release\" -ForegroundColor Cyan
