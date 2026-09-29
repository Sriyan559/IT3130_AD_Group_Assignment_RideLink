param([switch]$Build)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
Set-Location $repoRoot
if ($Build) {
    & mvn -B -pl account-service,driver-vehicle-service,ride-management-service -am package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
}
$services = @(
    @{ Name='account'; Module='account-service'; Port=18081 },
    @{ Name='driver'; Module='driver-vehicle-service'; Port=18082 },
    @{ Name='ride'; Module='ride-management-service'; Port=18083 }
)
foreach ($service in $services) {
    if (Get-NetTCPConnection -LocalPort $service.Port -State Listen -ErrorAction SilentlyContinue) {
        throw "Port $($service.Port) is already in use. Stop the earlier demo/test first."
    }
    if (-not (Test-Path "$($service.Module)/target/$($service.Module)-1.0.0-SNAPSHOT.jar")) {
        throw 'Missing JAR. Run this script with -Build.'
    }
}
$logDirectory = Join-Path $repoRoot 'tmp/local-integration'
New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$env:JWT_SECRET = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
$env:RIDELINK_SERVICE_TOKEN = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
$env:RIDELINK_SECURITY_ENABLED = 'true'
$env:ACCOUNT_SERVICE_URL = 'http://localhost:18081'
$env:DRIVER_SERVICE_URL = 'http://localhost:18082'
$env:SPRING_PROFILES_ACTIVE = 'integration-demo'
$env:DRIVER_LOCATION_MAX_AGE_SECONDS = '300'
$started = @()
try {
    foreach ($service in $services) {
        $dbName = "ridelink_demo_$($service.Name)"
        $env:SERVER_PORT = [string]$service.Port
        $env:SPRING_DATA_MONGODB_URI = "mongodb://localhost:27017/$dbName"
        $env:SPRING_DATA_MONGODB_DATABASE = $dbName
        $env:SPRING_DATA_MONGODB_HOST = 'localhost'
        $env:SPRING_DATA_MONGODB_PORT = '27017'
        $jarPath = Join-Path $repoRoot "$($service.Module)/target/$($service.Module)-1.0.0-SNAPSHOT.jar"
        $process = Start-Process -FilePath (Get-Command java).Source -ArgumentList @('-jar', "`"$jarPath`"") `
            -WorkingDirectory $repoRoot -WindowStyle Hidden -PassThru `
            -RedirectStandardOutput "$logDirectory/$($service.Name)-out.log" `
            -RedirectStandardError "$logDirectory/$($service.Name)-err.log"
        $started += @{ Name=$service.Name; Id=$process.Id; Jar=$jarPath }
    }
    $started | ConvertTo-Json | Set-Content "$logDirectory/processes.json"
    foreach ($service in $services) {
        $ready = $false
        for ($attempt = 0; $attempt -lt 90; $attempt++) {
            try {
                Invoke-RestMethod "http://localhost:$($service.Port)/v3/api-docs" -TimeoutSec 2 | Out-Null
                $ready = $true
                break
            } catch { Start-Sleep -Seconds 1 }
        }
        if (-not $ready) { throw "$($service.Name) did not start. Check tmp/local-integration logs." }
        Write-Host "$($service.Name) ready on http://localhost:$($service.Port)"
    }
    Write-Host 'Import postman/collections/RideLink-Integration.postman_collection.json and run in order.'
    Write-Host 'Stop these services with integration-tests/stop-local.ps1. Demo data is retained.'
} catch {
    foreach ($service in $started) { Stop-Process -Id $service.Id -ErrorAction SilentlyContinue }
    throw
} finally {
    Remove-Item Env:JWT_SECRET,Env:RIDELINK_SERVICE_TOKEN -ErrorAction SilentlyContinue
}
