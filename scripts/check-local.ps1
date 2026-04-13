#!/usr/bin/env pwsh
# check-local.ps1 — Lokal geliştirme kalite kapısı
# Kullanım: .\scripts\check-local.ps1

Set-Location (Split-Path $PSScriptRoot)

$ErrorActionPreference = "Stop"

Write-Host "`n=== BenimGünlerim Lokal Kalite Kontrolü ===" -ForegroundColor Cyan

# 1. Unit testler
Write-Host "`n[1/3] Unit testler..." -ForegroundColor Yellow
.\gradlew.bat testDebugUnitTest
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Unit testler" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Unit testler" -ForegroundColor Green

# 2. Lint (debug)
Write-Host "`n[2/3] Lint debug..." -ForegroundColor Yellow
.\gradlew.bat lintDebug
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Lint debug" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Lint debug" -ForegroundColor Green

# 3. Debug build
Write-Host "`n[3/3] Debug build..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Debug build" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Debug build" -ForegroundColor Green

Write-Host "`n=== TUM KAPILARI GECTI ===" -ForegroundColor Green
