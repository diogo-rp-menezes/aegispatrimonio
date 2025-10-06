# =============================================================================
# Script: Register-NewAsset.ps1
# Description: GUI tool to register a new IT asset in the Aegis Patrimonio system.
#              It dynamically loads data from the API into ComboBoxes.
# =============================================================================

# --- Parameters ---
param(
    [Parameter(Mandatory=$false)]
    [string]$ApiUrl = "http://localhost:8080/api",
    [Parameter(Mandatory=$false)]
    [string]$ApiUser = "admin@aegis.com",
    [Parameter(Mandatory=$false)]
    [string]$ApiPassword = "password"
)

# --- GUI and Core Functions ---

Add-Type -AssemblyName PresentationFramework

# (Funções Get-JwtToken e Get-HardwareData permanecem as mesmas)
function Get-JwtToken {
    param([string]$Url, [string]$User, [string]$Password)
    $body = @{ email = $User; password = $Password } | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri "$Url/auth/login" -Method Post -Body $body -ContentType "application/json"
        return $response.token
    } catch {
        return $null
    }
}

function Get-HardwareData {
    $os = Get-CimInstance Win32_OperatingSystem
    $computer = Get-CimInstance Win32_ComputerSystem
    $processor = Get-CimInstance Win32_Processor | Select-Object -First 1
    $baseboard = Get-CimInstance Win32_BaseBoard
    $memoryModules = @(Get-CimInstance Win32_PhysicalMemory | ForEach-Object { @{ "manufacturer" = if ([string]::IsNullOrWhiteSpace($_.Manufacturer)) { "Unknown" } else { $_.Manufacturer.Trim() }; "serialNumber" = $_.SerialNumber.Trim(); "partNumber" = if ([string]::IsNullOrWhiteSpace($_.PartNumber)) { "Undefined" } else { $_.PartNumber.Trim() }; "sizeGb" = [math]::Round($_.Capacity / 1GB) } })
    $disks = @(Get-CimInstance Win32_DiskDrive | Where-Object { $_.MediaType -match 'Fixed' } | ForEach-Object {
        $disk = $_; $partitions = Get-CimInstance -Query "ASSOCIATORS OF {Win32_DiskDrive.DeviceID='$($disk.DeviceID)'} WHERE AssocClass = Win32_DiskDriveToDiskPartition"; $freeGB = 0
        foreach ($partition in $partitions) { $logical = Get-CimInstance -Query "ASSOCIATORS OF {Win32_DiskPartition.DeviceID='$($partition.DeviceID)'} WHERE AssocClass = Win32_LogicalDiskToPartition"; if ($logical) { $freeGB += [math]::Round($logical.FreeSpace / 1GB) } }
        $type = if ($disk.MediaType -match 'SSD') { 'SSD' } else { 'HDD' }; @{ "model" = $disk.Model.Trim(); "serial" = $disk.SerialNumber.Trim(); "type" = $type; "totalGb" = [math]::Round($disk.Size / 1GB); "freeGb" = $freeGB; "freePercent" = if (($disk.Size / 1GB) -gt 0) { [math]::Round(($freeGB / ($disk.Size / 1GB)) * 100) } else { 0 } }
    })
    $networkAdapters = @(Get-CimInstance Win32_NetworkAdapterConfiguration | Where-Object { $_.IPEnabled -eq $true } | ForEach-Object { @{ "description" = $_.Description; "macAddress" = $_.MACAddress; "ipAddresses" = $_.IPAddress -join ', ' } })
    return @{ "computerName" = $env:COMPUTERNAME; "domain" = $computer.Domain; "osName" = $os.Caption; "osVersion" = $os.Version; "osArchitecture" = $os.OSArchitecture; "motherboardManufacturer" = $baseboard.Manufacturer; "motherboardModel" = $baseboard.Product; "motherboardSerialNumber" = $baseboard.SerialNumber; "cpuModel" = $processor.Name; "cpuCores" = $processor.NumberOfCores; "cpuThreads" = $processor.NumberOfLogicalProcessors; "discos" = $disks; "memorias" = $memoryModules; "adaptadoresRede" = $networkAdapters }
}

# --- NEW: Function to populate a ComboBox from an API endpoint ---
function Populate-ComboBox {
    param(
        [System.Windows.Controls.ComboBox]$ComboBox,
        [string]$Endpoint,
        [string]$Token,
        [string]$DisplayMember = "nome" # Default display member
    )
    $headers = @{ "Authorization" = "Bearer $Token" }
    try {
        $items = Invoke-RestMethod -Uri $Endpoint -Method Get -Headers $headers
        $ComboBox.Items.Clear()
        $items | ForEach-Object {
            $ComboBox.Items.Add($_)
        }
        $ComboBox.DisplayMemberPath = $DisplayMember
        $ComboBox.SelectedValuePath = "id"
    } catch {
        $ComboBox.Items.Clear()
        $ComboBox.Items.Add("Erro ao carregar dados")
    }
}

# --- GUI Definition (XAML) ---
[xml]$xaml = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="Registrar Novo Ativo" Height="450" Width="600" WindowStartupLocation="CenterScreen">
    <Grid Margin="15">
        <Grid.RowDefinitions>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="*"/>
            <RowDefinition Height="Auto"/>
        </Grid.RowDefinitions>
        
        <TextBlock Text="Registro de Novo Ativo de TI" FontSize="18" FontWeight="Bold" Margin="0,0,0,15"/>

        <ScrollViewer Grid.Row="1" VerticalScrollBarVisibility="Auto">
            <StackPanel Orientation="Vertical">
                <Label Content="Nº Patrimônio:" FontWeight="Bold"/>
                <TextBox Name="NumeroPatrimonioBox" ToolTip="Número de patrimônio do ativo" Margin="0,0,0,10"/>

                <Label Content="Filial:" FontWeight="Bold"/>
                <ComboBox Name="FilialBox" ToolTip="Selecione a Filial" Margin="0,0,0,10">
                    <ComboBoxItem IsSelected="True">Carregando...</ComboBoxItem>
                </ComboBox>

                <Label Content="Tipo de Ativo:" FontWeight="Bold"/>
                <ComboBox Name="TipoAtivoBox" ToolTip="Selecione o Tipo de Ativo" Margin="0,0,0,10">
                    <ComboBoxItem IsSelected="True">Carregando...</ComboBoxItem>
                </ComboBox>

                <Label Content="Fornecedor:" FontWeight="Bold"/>
                <ComboBox Name="FornecedorBox" ToolTip="Selecione o Fornecedor" Margin="0,0,0,10">
                    <ComboBoxItem IsSelected="True">Carregando...</ComboBoxItem>
                </ComboBox>

                <Label Content="Responsável:" FontWeight="Bold"/>
                <ComboBox Name="PessoaBox" ToolTip="Selecione a Pessoa responsável" Margin="0,0,0,10">
                    <ComboBoxItem IsSelected="True">Carregando...</ComboBoxItem>
                </ComboBox>

                <Label Content="Localização:" FontWeight="Bold"/>
                <ComboBox Name="LocalizacaoBox" ToolTip="Selecione a Localização (sala, andar)" Margin="0,0,0,10">
                    <ComboBoxItem IsSelected="True">Carregando...</ComboBoxItem>
                </ComboBox>

                <Label Content="Valor de Aquisição:" FontWeight="Bold"/>
                <TextBox Name="ValorAquisicaoBox" Text="0.00" ToolTip="Use ponto como separador decimal" Margin="0,0,0,10"/>
            </StackPanel>
        </ScrollViewer>

        <StackPanel Grid.Row="2" Orientation="Vertical" Margin="0,15,0,0">
            <StackPanel Orientation="Horizontal">
                <Button Name="RegisterBtn" Content="Registrar Ativo" Width="150" FontWeight="Bold" IsEnabled="False"/>
                <Button Name="SairBtn" Content="Sair" Width="100" Margin="10,0,0,0"/>
            </StackPanel>
            <TextBlock Name="StatusBlock" Text="Carregando dados da API..." Margin="0,10,0,0" FontStyle="Italic"/>
        </StackPanel>
    </Grid>
</Window>
"@

# --- Window Loading and Control Mapping ---
$reader = (New-Object System.Xml.XmlNodeReader $xaml)
$window = [Windows.Markup.XamlReader]::Load($reader)

$NumeroPatrimonioBox = $window.FindName("NumeroPatrimonioBox")
$FilialBox = $window.FindName("FilialBox")
$TipoAtivoBox = $window.FindName("TipoAtivoBox")
$FornecedorBox = $window.FindName("FornecedorBox")
$PessoaBox = $window.FindName("PessoaBox")
$LocalizacaoBox = $window.FindName("LocalizacaoBox")
$ValorAquisicaoBox = $window.FindName("ValorAquisicaoBox")
$StatusBlock = $window.FindName("StatusBlock")
$RegisterBtn = $window.FindName("RegisterBtn")
$SairBtn = $window.FindName("SairBtn")

# --- NEW: Function to load all API data on startup ---
function Load-ApiData {
    $StatusBlock.Text = "Autenticando..."
    $securePassword = ConvertTo-SecureString $ApiPassword -AsPlainText -Force
    $jwtToken = Get-JwtToken -Url $ApiUrl -User $ApiUser -Password $securePassword

    if (-not $jwtToken) {
        $StatusBlock.Text = "Falha na autenticação. Verifique os parâmetros e a conexão com a API."
        [System.Windows.MessageBox]::Show($StatusBlock.Text, "Erro Crítico", "OK", "Error")
        return
    }

    $StatusBlock.Text = "Carregando Filiais..."
    Populate-ComboBox -ComboBox $FilialBox -Endpoint "$ApiUrl/filiais" -Token $jwtToken
    
    $StatusBlock.Text = "Carregando Tipos de Ativo..."
    # Assuming a /tipos-ativo endpoint exists
    Populate-ComboBox -ComboBox $TipoAtivoBox -Endpoint "$ApiUrl/tipos-ativo" -Token $jwtToken

    $StatusBlock.Text = "Carregando Fornecedores..."
    Populate-ComboBox -ComboBox $FornecedorBox -Endpoint "$ApiUrl/fornecedores" -Token $jwtToken

    $StatusBlock.Text = "Carregando Pessoas..."
    Populate-ComboBox -ComboBox $PessoaBox -Endpoint "$ApiUrl/pessoas" -Token $jwtToken

    $StatusBlock.Text = "Carregando Localizações..."
    Populate-ComboBox -ComboBox $LocalizacaoBox -Endpoint "$ApiUrl/localizacoes" -Token $jwtToken

    $StatusBlock.Text = "Pronto para registrar."
    $RegisterBtn.IsEnabled = $true
}

# --- Button Click Logic (Updated to use ComboBox values) ---
$RegisterBtn.Add_Click({
    $RegisterBtn.IsEnabled = $false
    $StatusBlock.Text = "Validando dados..."

    # Validate that a selection has been made in the ComboBoxes
    if (($FilialBox.SelectedValue -eq $null) -or ($TipoAtivoBox.SelectedValue -eq $null) -or ($FornecedorBox.SelectedValue -eq $null)) {
        [System.Windows.MessageBox]::Show("Filial, Tipo de Ativo e Fornecedor são obrigatórios.", "Dados Incompletos", "OK", "Warning")
        $RegisterBtn.IsEnabled = $true
        return
    }

    $StatusBlock.Text = "Iniciando registro..."
    $securePassword = ConvertTo-SecureString $ApiPassword -AsPlainText -Force
    $jwtToken = Get-JwtToken -Url $ApiUrl -User $ApiUser -Password $securePassword
    if (-not $jwtToken) { $StatusBlock.Text = "Falha na autenticação."; $RegisterBtn.IsEnabled = $true; return }

    $StatusBlock.Text = "Criando registro do ativo..."
    $headers = @{ "Authorization" = "Bearer $jwtToken"; "Content-Type" = "application/json" }
    
    $ativoPayload = @{
        filialId = $FilialBox.SelectedValue
        nome = "PC-" + $NumeroPatrimonioBox.Text
        tipoAtivoId = $TipoAtivoBox.SelectedValue
        numeroPatrimonio = $NumeroPatrimonioBox.Text
        localizacaoId = $LocalizacaoBox.SelectedValue # Can be null if nothing is selected
        dataAquisicao = (Get-Date).ToString("yyyy-MM-dd")
        fornecedorId = $FornecedorBox.SelectedValue
        valorAquisicao = [decimal]$ValorAquisicaoBox.Text
        pessoaResponsavelId = $PessoaBox.SelectedValue # Can be null
        observacoes = "Ativo registrado via script PowerShell GUI."
        informacoesGarantia = ""
    } | ConvertTo-Json -Depth 5

    try {
        $newAtivo = Invoke-RestMethod -Uri "$ApiUrl/ativos" -Method Post -Headers $headers -Body $ativoPayload
        $newAtivoId = $newAtivo.id
        $StatusBlock.Text = "Ativo criado (ID: $newAtivoId). Enviando dados de hardware..."
        
        $hardwarePayload = Get-HardwareData | ConvertTo-Json -Depth 10
        Invoke-RestMethod -Uri "$ApiUrl/ativos/$newAtivoId/health-check" -Method Patch -Headers $headers -Body $hardwarePayload
        
        [System.Windows.MessageBox]::Show("Ativo ID $newAtivoId registrado e atualizado com sucesso!", "Sucesso", "OK", "Information")
        $StatusBlock.Text = "Pronto."
    } catch {
        [System.Windows.MessageBox]::Show("Falha ao registrar o ativo.`n$($_.Exception.Response.Content)", "Erro na API", "OK", "Error")
        $StatusBlock.Text = "Erro ao registrar ativo."
    }
    $RegisterBtn.IsEnabled = $true
})

$SairBtn.Add_Click({ $window.Close() })

# --- NEW: Load data when the window is initialized ---
$window.Add_SourceInitialized({
    # Run the data loading in a background job so the UI doesn't freeze
    Start-Job -ScriptBlock ${function:Load-ApiData}
})

# --- Show Window ---
$window.ShowDialog() | Out-Null
