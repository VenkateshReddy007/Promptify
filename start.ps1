# Start MySQL (if needed), build, and launch AI Credit Cost Analyzer
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

& "$PSScriptRoot\setup-mysql.ps1"
& "$PSScriptRoot\build.ps1"
& "$PSScriptRoot\run.ps1"
