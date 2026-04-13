#!/usr/bin/env pwsh
# clean-build-artifacts.ps1 — Build çıktılarını temizle
# Kullanım: .\scripts\clean-build-artifacts.ps1

Set-Location (Split-Path $PSScriptRoot)

Write-Host "Build artifacts temizleniyor..." -ForegroundColor Yellow

.\gradlew.bat clean

# Kotlin cache
if (Test-Path ".kotlin") { Remove-Item -Recurse -Force ".kotlin"; Write-Host "  .kotlin temizlendi" }

Write-Host "Temizlik tamamlandi." -ForegroundColor Green
