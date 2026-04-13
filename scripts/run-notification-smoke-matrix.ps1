#!/usr/bin/env pwsh
# Notification smoke matrix runner (real device)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot
$gradlew = Join-Path $repoRoot "gradlew.bat"

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    Write-Host "adb bulunamadı: $adb" -ForegroundColor Red
    exit 1
}

$device = & $adb devices | Select-String "`tdevice$" | ForEach-Object { ($_ -split "`t")[0] } | Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($device)) {
    Write-Host "Bağlı cihaz bulunamadı." -ForegroundColor Red
    exit 1
}

Write-Host "Notification smoke matrix cihazı: $device" -ForegroundColor Cyan

$reportDir = "build_notification_matrix"
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null

& $adb -s $device shell getprop ro.product.manufacturer | Set-Content "$reportDir/device_manufacturer.txt"
& $adb -s $device shell getprop ro.product.model | Set-Content "$reportDir/device_model.txt"
& $adb -s $device shell getprop ro.build.version.release | Set-Content "$reportDir/android_version.txt"

Write-Host "[1/3] Debug APK yükleniyor..." -ForegroundColor Yellow
& $gradlew :app:installDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "[2/3] NotificationMatrixSmokeTest çalıştırılıyor..." -ForegroundColor Yellow
& $gradlew :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.benimgunlerim.NotificationMatrixSmokeTest"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "[3/3] dumpsys çıktıları alınıyor..." -ForegroundColor Yellow
& $adb -s $device shell dumpsys notification --noredact | Set-Content "$reportDir/dumpsys_notification.txt"
& $adb -s $device shell dumpsys alarm | Set-Content "$reportDir/dumpsys_alarm.txt"

Write-Host "Notification smoke matrix tamamlandı. Rapor dizini: $reportDir" -ForegroundColor Green
