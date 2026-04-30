#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Scans app source files for UTF-8 mojibake sequences.
.DESCRIPTION
    Fails with exit code 1 if any mojibake pattern is found.
    Patterns are intentionally ASCII-only regex literals so this script cannot
    become a source of mojibake parse errors.
.EXAMPLE
    pwsh -File scripts/check-mojibake.ps1
#>

$ErrorActionPreference = 'Stop'

$mojibakePatterns = @(
    '\u00C3\u0192\u00C2\u00BC',        # u-umlaut double-encoded
    '\u00C3\u0192\u00C2\u00B6',        # o-umlaut double-encoded
    '\u00C3\u0192\u00C2\u00B1',        # n-tilde double-encoded
    '\u00C3\u201E\u00C2\u00B1',        # dotless i double-encoded
    '\u00C3\u2026\u00C5\u00B8',        # s-cedilla double-encoded
    '\u00C3\u201E\u00C5\u00BE',        # G-breve double-encoded
    '\u00C3\u00A2\u00E2\u201A\u00AC',  # broken smart quote/dash prefix
    '\x00'                              # null bytes embedded in source
)

$appSrcDir = Join-Path (Join-Path (Join-Path $PSScriptRoot '..') 'app') 'src'
$sourceDir = Join-Path $appSrcDir 'main'
$testDir = Join-Path $appSrcDir 'test'
$androidTestDir = Join-Path $appSrcDir 'androidTest'
$extensions = @('*.kt', '*.xml', '*.json', '*.toml', '*.properties', '*.md')

$allFiles = @()
foreach ($ext in $extensions) {
    $allFiles += Get-ChildItem -Path $sourceDir -Recurse -Filter $ext -ErrorAction SilentlyContinue
    $allFiles += Get-ChildItem -Path $testDir -Recurse -Filter $ext -ErrorAction SilentlyContinue
    $allFiles += Get-ChildItem -Path $androidTestDir -Recurse -Filter $ext -ErrorAction SilentlyContinue
}

$violations = [System.Collections.Generic.List[string]]::new()

foreach ($file in $allFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    foreach ($pattern in $mojibakePatterns) {
        if ($content -match $pattern) {
            $violations.Add("$($file.FullName): contains mojibake pattern '$pattern'")
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Error "Mojibake scan FAILED. Found $($violations.Count) violation(s):"
    $violations | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    exit 1
}

Write-Host "Mojibake scan PASSED. No encoding violations found." -ForegroundColor Green
exit 0
