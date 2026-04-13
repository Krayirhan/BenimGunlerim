#!/usr/bin/env pwsh
# CI/Play Console gibi repo dışı production ayarlarını lokal olarak doğrulamak için kontrol listesi.

$ErrorActionPreference = "Stop"

Write-Host "`n=== Dış Release Hazırlığı Kontrolü ===" -ForegroundColor Cyan

$requiredEnv = @(
    "KEYSTORE_PATH",
    "KEYSTORE_PASSWORD",
    "KEY_ALIAS",
    "KEY_PASSWORD"
)

$missing = @()
foreach ($name in $requiredEnv) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
        $missing += $name
    }
}

if ($missing.Count -gt 0) {
    Write-Host "Eksik lokal release değişkenleri:" -ForegroundColor Yellow
    $missing | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }
    Write-Host "`nGitHub Actions için karşılık gelen secret'lar tanımlanmalı:" -ForegroundColor Yellow
    Write-Host " - KEYSTORE_BASE64"
    Write-Host " - KEYSTORE_PASSWORD"
    Write-Host " - KEY_ALIAS"
    Write-Host " - KEY_PASSWORD"
} else {
    Write-Host "PASS: Lokal release signing değişkenleri mevcut." -ForegroundColor Green

    # Keystore dosyasının gerçekten var olup olmadığını doğrula
    $ksPath = [Environment]::GetEnvironmentVariable("KEYSTORE_PATH")
    if (-not [string]::IsNullOrWhiteSpace($ksPath)) {
        # keystore.properties'ten gelen path de kontrol et
        $propsFile = Join-Path (Split-Path $PSScriptRoot) "keystore.properties"
        if (Test-Path $propsFile) {
            $storeFileLine = Get-Content $propsFile | Where-Object { $_ -match "^storeFile=" } | Select-Object -First 1
            if ($storeFileLine) {
                $ksPath = $storeFileLine -replace "^storeFile=", ""
            }
        }
        if (-not (Test-Path $ksPath)) {
            Write-Host "UYARI: Keystore dosyası bulunamadı: $ksPath" -ForegroundColor Yellow
            Write-Host "  -> .\scripts\generate-keystore.ps1 çalıştırarak oluşturun." -ForegroundColor Yellow
        } else {
            Write-Host "PASS: Keystore dosyası mevcut: $ksPath" -ForegroundColor Green
        }
    }
}

Write-Host "`nManuel doğrulanacak dış sistemler:" -ForegroundColor Cyan
Write-Host " - GitHub branch protection: PR, release-quality ve connected-ui-tests required olmalı."
Write-Host " - GitHub tag protection: v* tag release süreci korunmalı."
Write-Host " - Play Console: signed AAB internal testing track'e yüklenmeli."
Write-Host " - Play Console Data Safety: docs/production/privacy-policy-tr.md ile uyumlu doldurulmalı."
Write-Host " - Monitoring: Crash/ANR sağlayıcısı seçilip release health izlenmeli."

if ($missing.Count -gt 0) {
    exit 1
}
