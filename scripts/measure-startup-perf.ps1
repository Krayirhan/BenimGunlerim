#!/usr/bin/env pwsh
# Startup perf smoke measurement on a connected Android device.

param(
    [int]$Iterations = 10,
    [string]$PackageName = "com.benimgunlerim.debug",
    [string]$ActivityName = "com.benimgunlerim.MainActivity",
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    throw "adb bulunamadı: $adb"
}

$device = & $adb devices | Select-String "`tdevice$" | ForEach-Object { ($_ -split "`t")[0] } | Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($device)) {
    throw "Bağlı cihaz bulunamadı."
}

if (-not $SkipInstall) {
    Write-Host "[setup] installDebug" -ForegroundColor Yellow
    & .\gradlew.bat :app:installDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$reportDir = "build_perf"
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
$csvPath = Join-Path $reportDir "startup_times.csv"

"iteration,total_ms,this_ms,wait_ms" | Set-Content $csvPath -Encoding ASCII
$results = @()

Write-Host "[run] $Iterations startup ölçümü alınıyor (cihaz: $device)" -ForegroundColor Cyan
for ($i = 1; $i -le $Iterations; $i++) {
    & $adb -s $device shell am force-stop $PackageName | Out-Null
    $out = & $adb -s $device shell am start -W -n "$PackageName/$ActivityName" 2>&1

    $total = ($out | Select-String "TotalTime:" | ForEach-Object { ($_ -split ":")[1].Trim() } | Select-Object -First 1)
    $this = ($out | Select-String "ThisTime:" | ForEach-Object { ($_ -split ":")[1].Trim() } | Select-Object -First 1)
    $wait = ($out | Select-String "WaitTime:" | ForEach-Object { ($_ -split ":")[1].Trim() } | Select-Object -First 1)

    if (-not $total) { $total = "0" }
    if (-not $this) { $this = "0" }
    if (-not $wait) { $wait = "0" }

    "$i,$total,$this,$wait" | Add-Content $csvPath -Encoding ASCII
    $results += [pscustomobject]@{
        Iteration = $i
        TotalMs = [int]$total
        ThisMs = [int]$this
        WaitMs = [int]$wait
    }
    Write-Host ("  #{0}: total={1}ms this={2}ms wait={3}ms" -f $i, $total, $this, $wait)
}

$avg = [math]::Round((($results | Measure-Object -Property TotalMs -Average).Average), 1)
$min = ($results | Measure-Object -Property TotalMs -Minimum).Minimum
$max = ($results | Measure-Object -Property TotalMs -Maximum).Maximum

$summary = @(
    "device=$device",
    "package=$PackageName",
    "iterations=$Iterations",
    "avg_total_ms=$avg",
    "min_total_ms=$min",
    "max_total_ms=$max"
)
$summaryPath = Join-Path $reportDir "startup_summary.txt"
$summary | Set-Content $summaryPath -Encoding ASCII

Write-Host "[done] Ortalama startup: $avg ms (min=$min, max=$max)" -ForegroundColor Green
Write-Host "CSV: $csvPath" -ForegroundColor Green
Write-Host "Summary: $summaryPath" -ForegroundColor Green
