#!/usr/bin/env pwsh
# Lokal geliştirme kalite kapısı.

Set-Location (Split-Path $PSScriptRoot)

$ErrorActionPreference = "Stop"

Write-Host "`n=== BenimGünlerim Lokal Kalite Kontrolü ===" -ForegroundColor Cyan

Write-Host "`n[1/3] Unit testler..." -ForegroundColor Yellow
.\gradlew.bat testDebugUnitTest
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Unit testler" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Unit testler" -ForegroundColor Green

Write-Host "`n[2/4] Coverage..." -ForegroundColor Yellow
.\gradlew.bat jacocoDebugUnitTestCoverageVerification
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Coverage" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Coverage" -ForegroundColor Green

Write-Host "`n[3/4] Lint debug..." -ForegroundColor Yellow
.\gradlew.bat lintDebug
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Lint debug" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Lint debug" -ForegroundColor Green

Write-Host "`n[4/4] Debug build..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Debug build" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Debug build" -ForegroundColor Green

Write-Host "`n=== TÜM KAPILAR GEÇTİ ===" -ForegroundColor Green
