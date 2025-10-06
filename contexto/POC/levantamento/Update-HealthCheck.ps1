# =============================================================================
# Script: Update-HealthCheck.ps1
# Description: Collects system hardware information and sends it to the
#              Aegis Patrimonio API for health check updates.
#              This script is designed to run silently in the background.
# =============================================================================

# --- Parameters ---
param(
    [Parameter(Mandatory=$true)]
    [string]$ApiUrl,  # e.g., "http://localhost:8080/api"

    [Parameter(Mandatory=$true)]
    [int]$AtivoId,

    [Parameter(Mandatory=$true)]
    [string]$ApiUser, # User email for authentication

    [Parameter(Mandatory=$true)]
    [System.Security.SecureString]$ApiPassword # User password
)

# --- Core Functions ---

# Function to get JWT token from the API
function Get-JwtToken {
    param(
        [string]$Url,
        [string]$User,
        [System.Security.SecureString]$Password
    )
    $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
    $body = @{
        email = $User
        password = $plainPassword
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$Url/auth/login" -Method Post -Body $body -ContentType "application/json"
        return $response.token
    }
    catch {
        Write-Error "Failed to get JWT token. Status: $($_.Exception.Response.StatusCode). Response: $($_.Exception.Response.Content)"
        return $null
    }
}

# Function to collect system data, adapted to match HealthCheckDTO
function Get-HardwareData {
    $os = Get-CimInstance Win32_OperatingSystem
    $computer = Get-CimInstance Win32_ComputerSystem
    $processor = Get-CimInstance Win32_Processor | Select-Object -First 1
    $baseboard = Get-CimInstance Win32_BaseBoard

    $memoryModules = @(Get-CimInstance Win32_PhysicalMemory | ForEach-Object {
        @{
            "manufacturer" = if ([string]::IsNullOrWhiteSpace($_.Manufacturer)) { "Unknown" } else { $_.Manufacturer.Trim() }
            "serialNumber" = $_.SerialNumber.Trim()
            "partNumber" = if ([string]::IsNullOrWhiteSpace($_.PartNumber)) { "Undefined" } else { $_.PartNumber.Trim() }
            "sizeGb" = [math]::Round($_.Capacity / 1GB)
        }
    })

    $disks = @(Get-CimInstance Win32_DiskDrive | Where-Object { $_.MediaType -match 'Fixed' } | ForEach-Object {
        $disk = $_
        $partitions = Get-CimInstance -Query "ASSOCIATORS OF {Win32_DiskDrive.DeviceID='$($disk.DeviceID)'} WHERE AssocClass = Win32_DiskDriveToDiskPartition"
        $freeGB = 0
        foreach ($partition in $partitions) {
            $logical = Get-CimInstance -Query "ASSOCIATORS OF {Win32_DiskPartition.DeviceID='$($partition.DeviceID)'} WHERE AssocClass = Win32_LogicalDiskToPartition"
            if ($logical) { $freeGB += [math]::Round($logical.FreeSpace / 1GB) }
        }
        $type = if ($disk.MediaType -match 'SSD') { 'SSD' } else { 'HDD' }
        @{
            "model" = $disk.Model.Trim()
            "serial" = $disk.SerialNumber.Trim()
            "type" = $type
            "totalGb" = [math]::Round($disk.Size / 1GB)
            "freeGb" = $freeGB
            "freePercent" = if (($disk.Size / 1GB) -gt 0) { [math]::Round(($freeGB / ($disk.Size / 1GB)) * 100) } else { 0 }
        }
    })

    $networkAdapters = @(Get-CimInstance Win32_NetworkAdapterConfiguration | Where-Object { $_.IPEnabled -eq $true } | ForEach-Object {
        @{
            "description" = $_.Description
            "macAddress" = $_.MACAddress
            "ipAddresses" = $_.IPAddress -join ', '
        }
    })

    $payload = @{
        "computerName" = $env:COMPUTERNAME
        "domain" = $computer.Domain
        "osName" = $os.Caption
        "osVersion" = $os.Version
        "osArchitecture" = $os.OSArchitecture
        "motherboardManufacturer" = $baseboard.Manufacturer
        "motherboardModel" = $baseboard.Product
        "motherboardSerialNumber" = $baseboard.SerialNumber
        "cpuModel" = $processor.Name
        "cpuCores" = $processor.NumberOfCores
        "cpuThreads" = $processor.NumberOfLogicalProcessors
        "discos" = $disks
        "memorias" = $memoryModules
        "adaptadoresRede" = $networkAdapters
    }

    return $payload
}

# Function to send data to the API
function Send-HealthCheckData {
    param(
        [string]$Url,
        [int]$Id,
        [string]$Token,
        [object]$Payload
    )

    $headers = @{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }
    $body = $Payload | ConvertTo-Json -Depth 10

    try {
        Invoke-RestMethod -Uri "$Url/ativos/$Id/health-check" -Method Patch -Headers $headers -Body $body
        Write-Host "Health check data sent successfully for Ativo ID: $Id"
    }
    catch {
        Write-Error "Failed to send health check data. Status: $($_.Exception.Response.StatusCode). Response: $($_.Exception.Response.Content)"
    }
}

# --- Main Execution ---

# 1. Get JWT Token
Write-Host "Authenticating with API..."
$jwtToken = Get-JwtToken -Url $ApiUrl -User $ApiUser -Password $ApiPassword

if (-not $jwtToken) {
    Write-Error "Could not obtain JWT Token. Aborting script."
    exit 1
}
Write-Host "Authentication successful."

# 2. Collect Hardware Data
Write-Host "Collecting hardware data..."
$hardwarePayload = Get-HardwareData
Write-Host "Data collection complete."

# 3. Send Data to API
Write-Host "Sending health check data to API..."
Send-HealthCheckData -Url $ApiUrl -Id $AtivoId -Token $jwtToken -Payload $hardwarePayload

Write-Host "Script finished."
