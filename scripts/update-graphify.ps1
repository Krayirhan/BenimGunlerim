param(
    [switch]$Force,
    [switch]$NoCluster
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command graphify -ErrorAction SilentlyContinue)) {
    throw 'graphify CLI bulunamadı. Kurulum: python -m pip install graphifyy'
}

$arguments = @('update', '.', '--no-cluster')
if ($Force) { $env:GRAPHIFY_FORCE = '1' }

& graphify @arguments
if ($LASTEXITCODE -ne 0) {
    throw "graphify update başarısız oldu (exit code: $LASTEXITCODE)"
}

Write-Host 'Graphify grafiği güncellendi: graphify-out/graph.json'
