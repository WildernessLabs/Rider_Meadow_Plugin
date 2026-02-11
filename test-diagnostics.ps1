# Diagnostic Testing Script for MeadowBackendHost
# Run this to rebuild, check logs, and verify component activation

Write-Host "=== Meadow Plugin Diagnostic Test ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Clean previous logs
Write-Host "[1/5] Clearing previous logs..." -ForegroundColor Yellow
$logPath = "$env:USERPROFILE\.cache\JetBrains\Rider*\log\idea.log"
Get-ChildItem $logPath -ErrorAction SilentlyContinue | ForEach-Object {
    Clear-Content $_.FullName -ErrorAction SilentlyContinue
}

# Step 2: Build the plugin
Write-Host "[2/5] Building plugin..." -ForegroundColor Yellow
.\gradlew buildPlugin
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "[3/5] Plugin built successfully" -ForegroundColor Green
Write-Host ""
Write-Host "=== NEXT STEPS ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Run the plugin in Rider:" -ForegroundColor White
Write-Host "   .\gradlew runIde" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Open a Meadow solution in the sandbox IDE" -ForegroundColor White
Write-Host ""
Write-Host "3. Try to open device selector or run a deployment" -ForegroundColor White
Write-Host ""
Write-Host "4. Check logs by running:" -ForegroundColor White
Write-Host "   Get-Content `$env:USERPROFILE\.cache\JetBrains\Rider*\log\idea.log | Select-String 'MeadowBackendHost'" -ForegroundColor Gray
Write-Host ""
Write-Host "=== EXPECTED LOG ENTRIES ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "IF [SolutionComponent] DI IS WORKING:" -ForegroundColor Green
Write-Host "  ✓ 'MeadowBackendHost created - [SolutionComponent] DI activated'" -ForegroundColor Gray
Write-Host "  ✓ 'GetSerialPorts RD handler registered successfully'" -ForegroundColor Gray
Write-Host "  ✓ 'GetSerialPortsAsync RD handler invoked' (when opening device list)" -ForegroundColor Gray
Write-Host "  ✓ 'Found X serial ports'" -ForegroundColor Gray
Write-Host ""
Write-Host "IF [SolutionComponent] DI IS BROKEN:" -ForegroundColor Red
Write-Host "  ✗ No MeadowBackendHost log entries" -ForegroundColor Gray
Write-Host "  ✗ Device selector spinner hangs forever" -ForegroundColor Gray
Write-Host "  ✗ getSerialPorts RD call never returns" -ForegroundColor Gray
Write-Host ""
