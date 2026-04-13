#!/usr/bin/env pwsh
# ============================================================
# generate-keystore.ps1
#
# Bir kez çalıştırın: release keystore + keystore.properties oluşturur.
# Üretilen dosyalar Git'e ALINMAZ (.gitignore gereği).
#
# Gereksinim: Java 17+ (keytool) PATH'te olmalı.
# ============================================================

$ErrorActionPreference = "Stop"

$repoRoot    = Split-Path -Parent $PSScriptRoot
$keysDir     = Join-Path $env:USERPROFILE "keys"
$jksPath     = Join-Path $keysDir "benimgunlerim-release.jks"
$propsPath   = Join-Path $repoRoot "keystore.properties"
$keyAlias    = "benimgunlerim"
$validity    = 10000   # gün (≈27 yıl)

Write-Host "`n=== BenimGünlerim Release Keystore Oluşturucu ===" -ForegroundColor Cyan

# ── Daha önce oluşturulmuş mu? ─────────────────────────────────────────────
if (Test-Path $jksPath) {
    Write-Host "UYARI: Keystore zaten mevcut: $jksPath" -ForegroundColor Yellow
    Write-Host "Üzerine yazmak istiyor musunuz? (e/H) " -NoNewline
    $answer = Read-Host
    if ($answer -notmatch "^[eEyY]") {
        Write-Host "İptal edildi." -ForegroundColor Yellow
        exit 0
    }
}

# ── Şifreler ─────────────────────────────────────────────────────────────
Write-Host "`nKeystore şifresi (en az 6 karakter): " -NoNewline
$storePass = Read-Host -AsSecureString
$storePassPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePass))

Write-Host "Anahtar şifresi (boş bırakırsanız keystore şifresiyle aynı olur): " -NoNewline
$keyPass = Read-Host -AsSecureString
$keyPassPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPass))
if ([string]::IsNullOrWhiteSpace($keyPassPlain)) { $keyPassPlain = $storePassPlain }

if ($storePassPlain.Length -lt 6) {
    Write-Host "HATA: Şifre en az 6 karakter olmalıdır." -ForegroundColor Red
    exit 1
}

# ── Distinguised Name ────────────────────────────────────────────────────
Write-Host "`nAd Soyad (CN, örn: Ali Yilmaz): " -NoNewline
$cn  = Read-Host
Write-Host "Kuruluş (O, örn: BenimGunlerim Dev): " -NoNewline
$org = Read-Host
Write-Host "Ülke kodu (C, örn: TR): " -NoNewline
$cc  = Read-Host
if ([string]::IsNullOrWhiteSpace($cc)) { $cc = "TR" }

$dname = "CN=$cn, O=$org, C=$cc"

# ── Dizin oluştur ─────────────────────────────────────────────────────────
New-Item -ItemType Directory -Force -Path $keysDir | Out-Null

# ── keytool çalıştır ──────────────────────────────────────────────────────
Write-Host "`nKeystore oluşturuluyor: $jksPath" -ForegroundColor Yellow
& keytool `
    -genkeypair `
    -v `
    -keystore $jksPath `
    -storetype JKS `
    -alias $keyAlias `
    -keyalg RSA `
    -keysize 2048 `
    -validity $validity `
    -storepass $storePassPlain `
    -keypass $keyPassPlain `
    -dname $dname

if ($LASTEXITCODE -ne 0) {
    Write-Host "HATA: keytool başarısız oldu." -ForegroundColor Red
    exit 1
}

# ── keystore.properties yaz ───────────────────────────────────────────────
$content = @"
# Bu dosya otomatik oluşturuldu — asla repoya commit etmeyin!
storeFile=$($jksPath.Replace('\','/'))
storePassword=$storePassPlain
keyAlias=$keyAlias
keyPassword=$keyPassPlain
"@

Set-Content -Path $propsPath -Value $content -Encoding UTF8
Write-Host "keystore.properties yazıldı: $propsPath" -ForegroundColor Green

# ── Doğrula ───────────────────────────────────────────────────────────────
Write-Host "`n[Doğrulama] verifyReleaseSigning..."
Set-Location $repoRoot
& .\gradlew.bat verifyReleaseSigning
if ($LASTEXITCODE -eq 0) {
    Write-Host "PASS: Release signing hazır." -ForegroundColor Green
} else {
    Write-Host "FAIL: verifyReleaseSigning geçmedi — keystore.properties kontrol edin." -ForegroundColor Red
    exit 1
}

Write-Host "`n=== TAMAMLANDI ===" -ForegroundColor Green
Write-Host "Keystore : $jksPath" -ForegroundColor Cyan
Write-Host "Props    : $propsPath" -ForegroundColor Cyan
Write-Host "`nGitHub Actions için KEYSTORE_BASE64 değeri:"
$b64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($jksPath))
Write-Host $b64
Write-Host "`nBu değeri GitHub > Settings > Secrets > KEYSTORE_BASE64 altına ekleyin." -ForegroundColor Yellow
