# One-time MySQL setup for AI Credit Cost Analyzer
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"
$iniPath = Join-Path $PSScriptRoot "mysql\my.ini"
$dataDir = Join-Path $PSScriptRoot "mysql\data"

if (-not (Test-Path $mysqlBin)) {
    Write-Host "MySQL not found. Install with: winget install -e --id Oracle.MySQL"
    exit 1
}

New-Item -ItemType Directory -Force -Path $dataDir | Out-Null

if (-not (Test-Path "$dataDir\mysql")) {
    Write-Host "Initializing MySQL data directory at $dataDir ..."
    & "$mysqlBin\mysqld.exe" --defaults-file="$iniPath" --initialize-insecure
}

if (-not (Get-Process mysqld -ErrorAction SilentlyContinue)) {
    Write-Host "Starting MySQL on port 3306..."
    Start-Process -FilePath "$mysqlBin\mysqld.exe" -ArgumentList "--defaults-file=`"$iniPath`"" -WindowStyle Hidden
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 1
        $test = & "$mysqlBin\mysql.exe" -u root -e "SELECT 1;" 2>$null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
    }
    if (-not $ready) {
        Write-Host "MySQL failed to start. Check mysql\data\*.err"
        exit 1
    }
}

Write-Host "Creating database and tables..."
Get-Content (Join-Path $PSScriptRoot "schema.sql") -Raw | & "$mysqlBin\mysql.exe" -u root --default-character-set=utf8mb4
Write-Host "MySQL setup complete."
