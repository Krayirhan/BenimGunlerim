#!/usr/bin/env pwsh
# Startup perf smoke measurement on a connected Android device.

param(
    [int]$Iterations = 10,
    [string]$PackageName = "com.benimgunlerim.debug",
    [string]$ActivityName = "com.benimgunlerim.MainActivity",
    [ValidateSet("cold", "warm")]
    [string]$StartupMode = "cold",
    [int]$ColdMedianThresholdMs = 2000,
    [int]$ColdP90ThresholdMs = 3000,
    [int]$WarmMedianThresholdMs = 900,
    [int]$MaxSpikeThresholdMs = 4000,
    [switch]$SkipInstall,
    [switch]$FailOnThreshold
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
$csvPath = Join-Path $reportDir "startup_times_$StartupMode.csv"

"iteration,total_ms,this_ms,wait_ms,effective_total_ms" | Set-Content $csvPath -Encoding ASCII
$results = @()

Write-Host "[run] $Iterations startup ölçümü alınıyor (cihaz: $device)" -ForegroundColor Cyan

if ($StartupMode -eq "warm") {
    & $adb -s $device shell am force-stop $PackageName | Out-Null
    & $adb -s $device shell am start -W -n "$PackageName/$ActivityName" | Out-Null
    & $adb -s $device shell input keyevent 3 | Out-Null
}

for ($i = 1; $i -le $Iterations; $i++) {
    if ($StartupMode -eq "cold") {
        & $adb -s $device shell am force-stop $PackageName | Out-Null
    } else {
        & $adb -s $device shell input keyevent 3 | Out-Null
    }

    $out = & $adb -s $device shell am start -W -n "$PackageName/$ActivityName" 2>&1

    $total = ($out | Select-String "TotalTime:" | ForEach-Object { ($_ -split ":")[1].Trim() } | Select-Object -First 1)
    $wait = ($out | Select-String "WaitTime:" | ForEach-Object { ($_ -split ":")[1].Trim() } | Select-Object -First 1)

    if (-not $total) { $total = "0" }
    if (-not $wait) { $wait = "0" }
    $launchTimeMs = "0"

    $effectiveTotal = [int]$total
    if ($effectiveTotal -le 0) {
        $effectiveTotal = [int]$wait
    }

    "$i,$total,$launchTimeMs,$wait,$effectiveTotal" | Add-Content $csvPath -Encoding ASCII
    $results += [pscustomobject]@{
        Iteration = $i
        TotalMs = $effectiveTotal
        ThisMs = [int]$launchTimeMs
        WaitMs = [int]$wait
    }
    Write-Host ("  #{0}: total={1}ms this={2}ms wait={3}ms effective={4}ms" -f $i, $total, $launchTimeMs, $wait, $effectiveTotal)
}

$avg = [math]::Round((($results | Measure-Object -Property TotalMs -Average).Average), 1)
$min = ($results | Measure-Object -Property TotalMs -Minimum).Minimum
    $max = ($results | Measure-Object -Property TotalMs -Maximum).Maximum
$sortedTotals = $results.TotalMs | Sort-Object
$median = if ($sortedTotals.Count -eq 0) {
    0
} elseif ($sortedTotals.Count % 2 -eq 1) {
    $sortedTotals[[int]($sortedTotals.Count / 2)]
} else {
    [math]::Round((($sortedTotals[($sortedTotals.Count / 2) - 1] + $sortedTotals[$sortedTotals.Count / 2]) / 2.0), 1)
}
$p90Index = [math]::Ceiling($sortedTotals.Count * 0.9) - 1
$p90 = if ($sortedTotals.Count -eq 0) { 0 } else { $sortedTotals[[math]::Max(0, [int]$p90Index)] }

$summary = @(
    "device=$device",
    "package=$PackageName",
    "startup_mode=$StartupMode",
    "iterations=$Iterations",
    "avg_total_ms=$avg",
    "median_total_ms=$median",
    "p90_total_ms=$p90",
    "min_total_ms=$min",
    "max_total_ms=$max"
)
$summaryPath = Join-Path $reportDir "startup_summary_$StartupMode.txt"
$summary | Set-Content $summaryPath -Encoding ASCII

Write-Host "[done] Ortalama startup: $avg ms (median=$median, p90=$p90, min=$min, max=$max)" -ForegroundColor Green
Write-Host "CSV: $csvPath" -ForegroundColor Green
Write-Host "Summary: $summaryPath" -ForegroundColor Green

if ($FailOnThreshold) {
    $errors = @()
    if ($StartupMode -eq "cold") {
        if ($median -ge $ColdMedianThresholdMs) {
            $errors += "Cold median threshold aşıldı: $median ms >= $ColdMedianThresholdMs ms"
        }
        if ($p90 -ge $ColdP90ThresholdMs) {
            $errors += "Cold p90 threshold aşıldı: $p90 ms >= $ColdP90ThresholdMs ms"
        }
    } else {
        if ($median -ge $WarmMedianThresholdMs) {
            $errors += "Warm median threshold aşıldı: $median ms >= $WarmMedianThresholdMs ms"
        }
    }
    if ($max -ge $MaxSpikeThresholdMs) {
        $errors += "Max spike threshold aşıldı: $max ms >= $MaxSpikeThresholdMs ms"
    }

    if ($errors.Count -gt 0) {
        $errors | ForEach-Object { Write-Host "FAIL: $_" -ForegroundColor Red }
        exit 1
    }

    Write-Host "PASS: Startup threshold kontrolleri başarılı" -ForegroundColor Green
}
