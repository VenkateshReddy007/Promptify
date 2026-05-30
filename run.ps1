$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
if (-not (Test-Path "out\Main.class")) {
    & "$PSScriptRoot\build.ps1"
}

$javaCmd = "java"
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    $justjPath = "C:\Users\venky\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.11.v20260515-1531\jre\bin\java.exe"
    if (Test-Path $justjPath) {
        $javaCmd = $justjPath
    } else {
        Write-Error "java not found. Please install JDK 17+ and add it to your PATH."
        exit 1
    }
}

& $javaCmd -cp "out;lib\*" Main
