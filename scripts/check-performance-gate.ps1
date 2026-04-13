#!/usr/bin/env pwsh
# Production startup performance gate (cold + warm).

param(
    [int]$Iterations = 12,
    [string]$PackageName = "com.benimgunlerim.debug",
    [string]$ActivityName = "com.benimgunlerim.MainActivity"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "`n=== Startup Performance Gate ===" -ForegroundColor Cyan

Write-Host "`n[1/2] Cold startup threshold kontrolü" -ForegroundColor Yellow
& "$PSScriptRoot/measure-startup-perf.ps1" `
    -Iterations $Iterations `
    -PackageName $PackageName `
    -ActivityName $ActivityName `
    -StartupMode cold `
    -FailOnThreshold
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAIL: Cold startup gate" -ForegroundColor Red
    exit 1
}

Write-Host "`n[2/2] Warm startup threshold kontrolü" -ForegroundColor Yellow
& "$PSScriptRoot/measure-startup-perf.ps1" `
    -Iterations $Iterations `
    -PackageName $PackageName `
    -ActivityName $ActivityName `
    -StartupMode warm `
    -SkipInstall `
    -FailOnThreshold
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAIL: Warm startup gate" -ForegroundColor Red
    exit 1
}

Write-Host "`nPASS: Startup performance gate başarılı" -ForegroundColor Green