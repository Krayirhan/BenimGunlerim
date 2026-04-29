#!/usr/bin/env pwsh
# Local development quality gate.

param(
    [switch]$StrictRelease
)

Set-Location (Split-Path $PSScriptRoot)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "=== BenimGunlerim Local Quality Check ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[0/5] Mojibake scan..." -ForegroundColor Yellow
$mojibake = Get-ChildItem -Path "app/src" -Recurse -Include "*.kt", "*.xml" |
    Select-String -Pattern "\u00C3|\u00C2|\u00C4|\u00C5|\uFFFD"
if ($mojibake) {
    Write-Host "FAIL: Mojibake (broken encoding) detected:" -ForegroundColor Red
    $mojibake | ForEach-Object { Write-Host "  $($_.Filename):$($_.LineNumber) - $($_.Line.Trim())" }
    exit 1
}
Write-Host "PASS: No mojibake found" -ForegroundColor Green

Write-Host ""
Write-Host "[1/5] Unit tests..." -ForegroundColor Yellow
.\gradlew.bat testDebugUnitTest
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Unit tests" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Unit tests" -ForegroundColor Green

Write-Host ""
Write-Host "[2/5] Coverage..." -ForegroundColor Yellow
.\gradlew.bat jacocoDebugUnitTestCoverageVerification
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Coverage" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Coverage" -ForegroundColor Green

Write-Host ""
Write-Host "[3/5] Lint debug..." -ForegroundColor Yellow
.\gradlew.bat lintDebug
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Lint debug" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Lint debug" -ForegroundColor Green

Write-Host ""
Write-Host "[4/5] Static analysis (detekt)..." -ForegroundColor Yellow
.\gradlew.bat detekt
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Detekt" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Detekt" -ForegroundColor Green

Write-Host ""
Write-Host "[5/5] Debug build..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Debug build" -ForegroundColor Red; exit 1 }
Write-Host "PASS: Debug build" -ForegroundColor Green

Write-Host ""
Write-Host "=== ALL GATES PASSED ===" -ForegroundColor Green

Write-Host ""
Write-Host "[release] Release readiness..." -ForegroundColor Yellow
if ($StrictRelease) {
    .\gradlew.bat verifyReleaseSigning assembleRelease
    if ($LASTEXITCODE -ne 0) { Write-Host "FAIL: Release readiness" -ForegroundColor Red; exit 1 }
    Write-Host "PASS: Release signing + assembleRelease" -ForegroundColor Green
} else {
    Write-Host "SKIP: Strict release checks disabled. Run with -StrictRelease to enforce verifyReleaseSigning + assembleRelease." -ForegroundColor DarkYellow
}
