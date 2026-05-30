$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
if (-not (Test-Path out)) { New-Item -ItemType Directory -Path out | Out-Null }

$javacCmd = "javac"
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    $justjPath = "C:\Users\venky\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.11.v20260515-1531\jre\bin\javac.exe"
    if (Test-Path $justjPath) {
        $javacCmd = $justjPath
    } else {
        Write-Error "javac not found. Please install JDK 17+ and add it to your PATH."
        exit 1
    }
}

$files = (Get-ChildItem -Recurse -Filter *.java src).FullName
& $javacCmd -encoding UTF-8 -cp "lib\*" -d out $files
Write-Host "Build successful."
