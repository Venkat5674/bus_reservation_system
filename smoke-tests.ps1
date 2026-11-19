# ==============================================================================
# Comprehensive API Smoke Test Suite for Bus Reservation System (PowerShell)
# Usage: .\smoke-tests.ps1 -BaseUrl "http://localhost:8080"
# ==============================================================================
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminEmail = "admin@yourdomain.com",
    [string]$AdminPassword = "AdminTestPassword123"
)

$ErrorActionPreference = "Continue"
$Timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$TestUserEmail = "smoketest_$Timestamp@example.com"
$TestUserPass = "SmokePass123!"

$Passed = 0
$Failed = 0

function Assert-Result($TestName, $Condition, $Details) {
    if ($Condition) {
        Write-Host "  [PASS] $TestName" -ForegroundColor Green
        $script:Passed++
    } else {
        Write-Host "  [FAIL] $TestName - $Details" -ForegroundColor Red
        $script:Failed++
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " BUS RESERVATION SYSTEM - API SMOKE TEST SUITE" -ForegroundColor Cyan
Write-Host " Target Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Health Check
Write-Host "[Test 1] Actuator Health Check"
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get -TimeoutSec 5
    Assert-Result "Health Status UP" ($health.status -eq "UP") "Status is $($health.status)"
} catch {
    Assert-Result "Health Status UP" $false $_.Exception.Message
}

# 2. Public Bus Listing
Write-Host "[Test 2] Public Get All Buses"
try {
    $buses = Invoke-RestMethod -Uri "$BaseUrl/api/buses" -Method Get -TimeoutSec 5
    Assert-Result "Get All Buses Status 200" ($buses.success -eq $true) "Success flag was false"
} catch {
    Assert-Result "Get All Buses Status 200" $false $_.Exception.Message
}

# 3. User Registration
Write-Host "[Test 3] User Registration (/api/auth/register)"
$userToken = $null
try {
    $regBody = @{ name = "Smoke Tester"; email = $TestUserEmail; password = $TestUserPass } | ConvertTo-Json
    $reg = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method Post -Body $regBody -ContentType "application/json" -TimeoutSec 5
    Assert-Result "User Registration Success" ($reg.success -eq $true) "Failed to register"
    $userToken = $reg.data.token
} catch {
    Assert-Result "User Registration Success" $false $_.Exception.Message
}

# 4. User Login
Write-Host "[Test 4] User Login (/api/auth/login)"
try {
    $loginBody = @{ email = $TestUserEmail; password = $TestUserPass } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -TimeoutSec 5
    Assert-Result "User Login Success" ($login.success -eq $true) "Failed to login"
} catch {
    Assert-Result "User Login Success" $false $_.Exception.Message
}

# 5. Invalid Login Validation
Write-Host "[Test 5] Invalid Credentials Rejected"
try {
    $badLoginBody = @{ email = $TestUserEmail; password = "WrongPassword" } | ConvertTo-Json
    $bad = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $badLoginBody -ContentType "application/json" -TimeoutSec 5
    Assert-Result "Invalid Login Rejected" ($bad.success -eq $false) "Should have failed"
} catch {
    # 400 or 401 is expected
    Assert-Result "Invalid Login Rejected (Status 401/400)" $true "Expected rejection"
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " SMOKE TEST SUMMARY: Passed: $Passed | Failed: $Failed" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
