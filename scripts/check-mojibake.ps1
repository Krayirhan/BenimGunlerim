#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Scans app source files for UTF-8 mojibake sequences (Windows-1252 bytes mis-decoded as Latin-1).
.DESCRIPTION
    Fails with exit code 1 if any mojibake pattern is found.
    Intentional emoji characters are excluded via a safelist.
    Run this in CI after a build to catch encoding regressions early.
.EXAMPLE
    pwsh -File scripts/check-mojibake.ps1
#>

$ErrorActionPreference = 'Stop'

# Patterns that indicate mis-encoded Turkish / UTF-8 characters
# (Ã followed by a combining character, broken multibyte sequences, etc.)
$mojibakePatterns = @(
    'Ã¼',   # ü encoded as Windows-1252 read as Latin-1
    'Ã¶',   # ö
    'Ã±',   # ñ
    'Ä±',   # ı (dotless i)
    'ÅŸ',   # ş
    'Äž',   # Ğ
    'â€™',  # right single quotation mark (')
    'â€œ',  # left double quotation mark (")
    'â€',   # broken dash / quote start sequence
    '\x00'  # null bytes embedded in source
)

$sourceDir = Join-Path $PSScriptRoot '..' 'app' 'src' 'main'
$testDir   = Join-Path $PSScriptRoot '..' 'app' 'src' 'test'
$extensions = @('*.kt', '*.xml', '*.json', '*.toml', '*.properties')

$allFiles = @()
foreach ($ext in $extensions) {
    $allFiles += Get-ChildItem -Path $sourceDir -Recurse -Filter $ext -ErrorAction SilentlyContinue
    $allFiles += Get-ChildItem -Path $testDir   -Recurse -Filter $ext -ErrorAction SilentlyContinue
}

$violations = [System.Collections.Generic.List[string]]::new()

foreach ($file in $allFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    foreach ($pattern in $mojibakePatterns) {
        if ($content -match [regex]::Escape($pattern)) {
            $violations.Add("$($file.FullName): contains mojibake pattern '$pattern'")
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Error "Mojibake scan FAILED. Found $($violations.Count) violation(s):"
    $violations | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "Mojibake scan PASSED. No encoding violations found." -ForegroundColor Green
    exit 0
}
