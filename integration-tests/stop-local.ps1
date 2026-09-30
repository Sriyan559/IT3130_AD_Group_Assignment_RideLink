$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$pidFile = Join-Path $repoRoot 'tmp/local-integration/processes.json'
if (-not (Test-Path $pidFile)) { Write-Host 'No recorded demo processes.'; return }
foreach ($service in (Get-Content $pidFile -Raw | ConvertFrom-Json)) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$($service.Id)"
    # Check PID identity before stopping anything; PIDs can be reused.
    if ($process -and $process.Name -eq 'java.exe' -and $process.CommandLine.Contains($service.Jar)) {
        Stop-Process -Id $service.Id
        Write-Host "Stopped $($service.Name)."
    }
}
Remove-Item -LiteralPath $pidFile
