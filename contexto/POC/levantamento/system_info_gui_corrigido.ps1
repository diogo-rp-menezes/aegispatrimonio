Add-Type -Name Window -Namespace Console -MemberDefinition '
[DllImport("Kernel32.dll")] 
public static extern IntPtr GetConsoleWindow();
[DllImport("user32.dll")]
public static extern bool ShowWindow(IntPtr hWnd, Int32 nCmdShow);'
$consolePtr = [Console.Window]::GetConsoleWindow()
[Console.Window]::ShowWindow($consolePtr, 0) | Out-Null
Add-Type -AssemblyName PresentationFramework

# Inicializa a lista de equipamentos adicionais
$Equipamentos = New-Object System.Collections.ObjectModel.ObservableCollection[object]

# Função para abrir janela de adicionar equipamento
function Show-AddEquipmentDialog {
    [xml]$xaml = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        Title="Adicionar Equipamento" Height="250" Width="400" WindowStartupLocation="CenterScreen">
    <Grid Margin="10">
        <Grid.RowDefinitions>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
        </Grid.RowDefinitions>
        <Grid.ColumnDefinitions>
            <ColumnDefinition Width="100"/>
            <ColumnDefinition Width="*"/>
        </Grid.ColumnDefinitions>

        <TextBlock Text="Nome:" Grid.Row="0" Grid.Column="0" Margin="5"/>
        <TextBox Name="NomeInput" Grid.Row="0" Grid.Column="1" Margin="5"/>

        <TextBlock Text="Tipo:" Grid.Row="1" Grid.Column="0" Margin="5"/>
        <TextBox Name="TipoInput" Grid.Row="1" Grid.Column="1" Margin="5"/>

        <TextBlock Text="Patrimônio:" Grid.Row="2" Grid.Column="0" Margin="5"/>
        <TextBox Name="PatrimonioInput" Grid.Row="2" Grid.Column="1" Margin="5"/>

        <TextBlock Text="Observações:" Grid.Row="3" Grid.Column="0" Margin="5"/>
        <TextBox Name="NotasInput" Grid.Row="3" Grid.Column="1" Margin="5"/>

        <Button Name="SalvarBtn" Content="Salvar" Grid.Row="4" Grid.Column="1" Margin="5" Width="100" HorizontalAlignment="Right"/>
    </Grid>
</Window>
"@

    $reader = New-Object System.Xml.XmlNodeReader $xaml
    $form = [Windows.Markup.XamlReader]::Load($reader)

    $NomeInput        = $form.FindName("NomeInput")
    $TipoInput        = $form.FindName("TipoInput")
    $PatrimonioInput  = $form.FindName("PatrimonioInput")
    $NotasInput       = $form.FindName("NotasInput")
    $SalvarBtn        = $form.FindName("SalvarBtn")

    $SalvarBtn.Add_Click({
        if ($NomeInput.Text -ne "") {
            $Equipamentos.Add([pscustomobject]@{
                name      = $NomeInput.Text
                type      = $TipoInput.Text
                patrimony = $PatrimonioInput.Text
                notes     = $NotasInput.Text
            })
            $form.Close()
        } else {
            [System.Windows.MessageBox]::Show("Nome é obrigatório.", "Atenção", "OK", "Warning")
        }
    })

    $form.ShowDialog() | Out-Null
}


# Mapeamento de fabricantes de monitores
$monitorManufacturerMap = @{
    'AAC'='AcerView'; 'ACR'='Acer'; 'ACT'='Targa'; 'ADI'='ADI Corporation'
    'AIC'='AG Neovo'; 'APP'='Apple'; 'ART'='ArtMedia'; 'AST'='AST Research'
    'AUO'='AU Optronics'; 'BNQ'='BenQ'; 'CMO'='Acer'; 'CPL'='Compal'
    'CPQ'='Compaq'; 'CTX'='CTX'; 'DEC'='DEC'; 'DEL'='Dell'
    'DON'='Denon'; 'DNY'='Disney'; 'ENV'='EIZO'; 'EIZ'='EIZO'
    'ELS'='ELSA'; 'EPI'='EPI'; 'FUS'='Fujitsu-Siemens'; 'GSM'='LG Electronics'
    'GWY'='Gateway'; 'HEI'='Hyundai'; 'HIT'='Hitachi'; 'HSD'='HannStar'
    'HTC'='Hitachi'; 'HWP'='HP'; 'IBM'='IBM'; 'ICL'='Fujitsu ICL'
    'IVM'='Iiyama'; 'LEN'='Lenovo'; 'LGD'='LG Display'; 'LPL'='LG Philips'
    'MAX'='Belinea'; 'MEI'='Panasonic'; 'MEL'='Mitsubishi'; 'NEC'='NEC'
    'NOK'='Nokia'; 'NVD'='Fujitsu'; 'OPT'='Optoma'; 'PHL'='Philips'
    'PIO'='Pioneer'; 'PRI'='Prolink'; 'PRO'='Proview'; 'QDS'='Quanta'
    'SAM'='Samsung'; 'SAN'='Sanyo'; 'SEC'='Seiko Epson'; 'SGI'='SGI'
    'SNY'='Sony'; 'SRC'='Shamrock'; 'SUN'='Sun Microsystems'; 'TAT'='Tatung'
    'TOS'='Toshiba'; 'TSB'='Toshiba'; 'VSC'='ViewSonic'; 'WTC'='Wen'
    'ZCM'='Zenith'; 'PZG'='HQ'; 'UNK'='Unknown'
}

function Get-SystemData {
    $os = Get-CimInstance Win32_OperatingSystem
    $computer = Get-CimInstance Win32_ComputerSystem
    $processor = Get-CimInstance Win32_Processor | Select-Object -First 1
    $baseboard = Get-CimInstance Win32_BaseBoard

    $memoryModules = @(Get-CimInstance Win32_PhysicalMemory | ForEach-Object {
        @{
            "SerialNumber" = $_.SerialNumber.Trim()
            "Manufacturer" = if ([string]::IsNullOrWhiteSpace($_.Manufacturer)) { "Unknown" } else { $_.Manufacturer.Trim() }
            "size_gb" = [math]::Round($_.Capacity / 1GB)
            "PartNumber" = if ([string]::IsNullOrWhiteSpace($_.PartNumber)) { "Undefined" } else { $_.PartNumber.Trim() }
        }
    })

    $disks = @(Get-CimInstance Win32_DiskDrive | Where-Object { $_.MediaType -match 'Fixed' } | ForEach-Object {
        $disk = $_
        $partitions = Get-CimInstance -Query "ASSOCIATORS OF {Win32_DiskDrive.DeviceID='$($disk.DeviceID)'} WHERE AssocClass = Win32_DiskDriveToDiskPartition"
        $freeGB = 0

        foreach ($partition in $partitions) {
            $logical = Get-CimInstance -Query "ASSOCIATORS OF {Win32_DiskPartition.DeviceID='$($partition.DeviceID)'} WHERE AssocClass = Win32_LogicalDiskToPartition"
            $freeGB += [math]::Round($logical.FreeSpace / 1GB)
        }

        $type = if ($disk.MediaType -match 'SSD') {
            'SSD'
        } else {
            try {
                $driveType = (Get-PhysicalDisk -DeviceNumber $disk.Index).MediaType
                if ($driveType -eq 'SSD') { 'SSD' } else { 'HDD' }
            } catch {
                'HDD'
            }
        }

        @{
            "model" = $disk.Model.Trim()
            "serial" = $disk.SerialNumber.Trim()
            "type" = $type
            "total_gb" = [math]::Round($disk.Size / 1GB)
            "free_gb" = $freeGB
            "free_percent" = [math]::Round(($freeGB / ($disk.Size / 1GB)) * 100)
        }
    })

    $monitors = @(Get-CimInstance -Namespace root/wmi -Class WmiMonitorID | ForEach-Object {
        $m = $_
        $manufacturerCode = (-join ($m.ManufacturerName | Where-Object { $_ -ne 0 } | ForEach-Object { [char]$_ })).Trim()
        $manufacturer = if ($monitorManufacturerMap.ContainsKey($manufacturerCode)) {
            $monitorManufacturerMap[$manufacturerCode]
        } else {
            $manufacturerCode
        }

        @{
            "manufacturer" = $manufacturer
            "name" = (-join ($m.UserFriendlyName | Where-Object { $_ -ne 0 } | ForEach-Object { [char]$_ })).Trim()
            "product" = (-join ($m.ProductCodeID | Where-Object { $_ -ne 0 } | ForEach-Object { [char]$_ })).Trim()
            "serial" = (-join ($m.SerialNumberID | Where-Object { $_ -ne 0 } | ForEach-Object { [char]$_ })).Trim()
            "id" = 0
        }
    })

    $networkAdapters = @(Get-CimInstance Win32_NetworkAdapterConfiguration | Where-Object { $_.IPEnabled -eq $true } | ForEach-Object {
        @{
            "Description" = $_.Description
            "MACAddress" = $_.MACAddress
            "IPAddress" = @($_.IPAddress)
            "IPSubnet" = @($_.IPSubnet)
            "DefaultIPGateway" = @($_.DefaultIPGateway)
            "DHCPServer" = $_.DHCPServer
            "DNSDomain" = $_.DNSDomain
            "DNSDomainSuffixSearchOrder" = @($_.DNSDomainSuffixSearchOrder)
            "DNSServerSearchOrder" = @($_.DNSServerSearchOrder)
        }
    })

    $cpuInfo = @{
        "model" = $processor.Name
        "architecture" = $os.OSArchitecture
        "cores" = $processor.NumberOfCores
        "threads" = $processor.NumberOfLogicalProcessors
    }

    return @{
        "workstation" = @{
            "computer_name" = $env:COMPUTERNAME
            "domain" = $computer.Domain
            "os_name" = $os.Caption
            "os_version" = $os.Version
        }
        "motherboard" = @{
            "manufacturer" = $baseboard.Manufacturer
            "model" = $baseboard.Product
            "serial_number" = $baseboard.SerialNumber
        }
        "cpu" = $cpuInfo
        "memory" = $memoryModules
        "disks" = $disks
        "monitors" = $monitors
        "network" = $networkAdapters
    }
}

[xml]$xaml = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        Title="TIGestor" Height="600" Width="800" ResizeMode="NoResize"
        WindowStartupLocation="CenterScreen">
    <Grid Margin="10">
        <StackPanel Orientation="Vertical">
            <TextBlock Text="Informacoes da Organizacao" FontWeight="Bold" FontSize="16"/>
            <StackPanel Orientation="Horizontal">
                <Label Content="Coligada:" Width="100"/>
					<ComboBox Name="ColigadaBox" Width="200">
						<ComboBoxItem Content="Galois LePetit" />
						<ComboBoxItem Content="Galois 601" />
						<ComboBoxItem Content="Galois Aguas Claras" />
					</ComboBox>
					<Label Content="Departamento:" Width="100" Margin="20,0,0,0"/>
					<ComboBox Name="DepartamentoBox" Width="200">
						<ComboBoxItem Content="Infantil" />
						<ComboBoxItem Content="Fundamental" />
						<ComboBoxItem Content="Secretaria" />
						<ComboBoxItem Content="Estendido" />
						<ComboBoxItem Content="Administrativo" />
						<ComboBoxItem Content="TI" />
						<ComboBoxItem Content="Portaria" />
					</ComboBox>
            </StackPanel>
            <StackPanel Orientation="Horizontal" Margin="0,10,0,0">
                <Label Content="Local:" Width="100"/>
                <TextBox Name="LocalBox" Width="200"/>
                <Label Content="ID AnyDesk:" Width="100" Margin="20,0,0,0"/>
                <TextBox Name="AnydeskBox" Width="200"/>
            </StackPanel>
            <StackPanel Orientation="Horizontal" Margin="0,10,0,0">
                <Label Content="Senha Admin:" Width="100"/>
                <PasswordBox Name="SenhaBox" Width="200"/>
                <Label Content="Patrimonio PC:" Width="100" Margin="20,0,0,0"/>
                <TextBox Name="PatrimonioBox" Width="200"/>
            </StackPanel>
            <TextBlock Text="Equipamentos adicionais" FontWeight="Bold" FontSize="16" Margin="0,20,0,5"/>
            <DataGrid Name="EquipamentosGrid" AutoGenerateColumns="False" Height="150" CanUserAddRows="False">
                <DataGrid.Columns>
                    <DataGridTextColumn Header="Nome" Binding="{Binding name}" Width="*"/>
                    <DataGridTextColumn Header="Tipo" Binding="{Binding type}" Width="*"/>
                    <DataGridTextColumn Header="Patrimonio" Binding="{Binding patrimony}" Width="*"/>
                    <DataGridTextColumn Header="Observacoes" Binding="{Binding notes}" Width="*"/>
                </DataGrid.Columns>
            </DataGrid>
            <StackPanel Orientation="Horizontal" Margin="0,10,0,0">
                <Button Name="AdicionarEquipBtn" Content="Adicionar Equipamento" Width="180"/>
                <Button Name="RemoverEquipBtn" Content="Remover Selecionado" Width="180" Margin="10,0,0,0"/>
            </StackPanel>

            <Separator Margin="0,15,0,15"/>
            <StackPanel Orientation="Horizontal" HorizontalAlignment="Center">
                <Button Name="GerarJsonBtn" Content="Gerar JSON" Width="150" Margin="0,0,10,0"/>
                <Button Name="NovoScanBtn" Content="Limpa tela" Width="150" Margin="0,0,10,0"/>
                <Button Name="SairBtn" Content="Sair" Width="150"/>
            </StackPanel>
        </StackPanel>
    </Grid>
</Window>
"@
# Carrega o XAML
$reader = (New-Object System.Xml.XmlNodeReader $xaml)
$window = [Windows.Markup.XamlReader]::Load($reader)

# Mapeia os controles
$ColigadaBox = $window.FindName("ColigadaBox")
$DepartamentoBox = $window.FindName("DepartamentoBox")
$LocalBox = $window.FindName("LocalBox")
$AnydeskBox = $window.FindName("AnydeskBox")
$SenhaBox = $window.FindName("SenhaBox")
$PatrimonioBox = $window.FindName("PatrimonioBox")

$EquipamentosGrid = $window.FindName("EquipamentosGrid")
$AdicionarEquipBtn = $window.FindName("AdicionarEquipBtn")
$RemoverEquipBtn = $window.FindName("RemoverEquipBtn")
$GerarJsonBtn = $window.FindName("GerarJsonBtn")
$NovoScanBtn = $window.FindName("NovoScanBtn")
$SairBtn = $window.FindName("SairBtn")

# Lista de equipamentos (como array de objetos personalizados)
$Equipamentos = New-Object System.Collections.ObjectModel.ObservableCollection[object]
$EquipamentosGrid.ItemsSource = $Equipamentos

# Ações dos botões
$AdicionarEquipBtn.Add_Click({
    Show-AddEquipmentDialog
})

$RemoverEquipBtn.Add_Click({
    if ($EquipamentosGrid.SelectedItem) {
        $Equipamentos.Remove($EquipamentosGrid.SelectedItem)
    }
})
# Botão: Gerar JSON
$GerarJsonBtn.Add_Click({
    # Dados automáticos do sistema
    $autoData = Get-SystemData

    # Dados manuais da interface
    $manualData = @{
        patrimonio = $PatrimonioBox.Text
        additional_equipment = @($Equipamentos)
        admin_password = $SenhaBox.Password
        anydesk_id = $AnydeskBox.Text
    }

    $orgData = @{
        coligada = $ColigadaBox.Text
        departamento = $DepartamentoBox.Text
        local = $LocalBox.Text
    }

    # Monta o JSON final
    $json = @{
        system_info = @{
            manual_info = $manualData
            organization = $orgData
            motherboard = $autoData.motherboard
            memory = $autoData.memory
            workstation = $autoData.workstation
            disks = $autoData.disks
            network = $autoData.network
            monitors = @()

            cpu = $autoData.cpu
        }
    }

    # Corrige IDs dos monitores
    $monitorId = 1
    foreach ($m in $autoData.monitors) {
        $m.id = $monitorId++
        $json.system_info.monitors += $m
    }

    # Caminho e nome do arquivo
	$baseDir = Join-Path -Path "$PSScriptRoot" -ChildPath "../levantamento"

	# Função para sanitizar strings (remover acentos e caracteres especiais)
    function Convert-StringSanitized {
        param([string]$inputString)
        
        # Remove acentos e caracteres especiais
        $normalized = $inputString.Normalize("FormD")
        $invalidChars = [System.Text.RegularExpressions.Regex]::Escape([System.IO.Path]::GetInvalidFileNameChars() -join "")
        $cleaned = [System.Text.RegularExpressions.Regex]::Replace($normalized, "[$invalidChars]", "_")
        $cleaned = [System.Text.RegularExpressions.Regex]::Replace($cleaned, "\p{M}", "")
        $cleaned = $cleaned -replace "\s+", "_"  # Substitui espaços por underscores
        $cleaned = $cleaned -replace "_+", "_"   # Remove underscores duplicados
        $cleaned = $cleaned.Trim('_')            # Remove underscores no início/fim
        
        return $cleaned.ToLower()  # Opcional: converter para minúsculas
    }

	# Cria caminho da pasta com nomes sanitizados
    $coligadaSanitized = Convert-StringSanitized $orgData.coligada
    $departamentoSanitized = Convert-StringSanitized $orgData.departamento
    $localSanitized = Convert-StringSanitized $orgData.local

	$folderPath = Join-Path -Path $baseDir -ChildPath (Join-Path -Path $coligadaSanitized -ChildPath (Join-Path -Path $departamentoSanitized -ChildPath $localSanitized))
	New-Item -Path $folderPath -ItemType Directory -Force | Out-Null

	# Gera nome do arquivo sanitizado
	$fileName = "{0}_{1}_{2}_{3}.json" -f `
		$coligadaSanitized,
		$departamentoSanitized,
		$localSanitized,
		(Get-Date -Format "dd-MM-yyyy")

	$filePath = Join-Path -Path $folderPath -ChildPath $fileName

    # Salva o arquivo
    $json | ConvertTo-Json -Depth 6 | Set-Content -Path $filePath -Encoding UTF8
    [System.Windows.MessageBox]::Show("JSON gerado com sucesso em:`n$filePath", "Sucesso")
	# Limpar os campos após gerar JSON
    $ColigadaBox.SelectedIndex = -1
    $DepartamentoBox.SelectedIndex = -1
    $LocalBox.Text = ""
    $AnydeskBox.Text = ""
    $SenhaBox.Password = ""
    $PatrimonioBox.Text = ""
    $Equipamentos.Clear()
})

# Botão: Limpa tela
$NovoScanBtn.Add_Click({
    $ColigadaBox.Text = ""
    $DepartamentoBox.Text = ""
    $LocalBox.Text = ""
    $AnydeskBox.Text = ""
    $SenhaBox.Password = ""
    $PatrimonioBox.Text = ""
    $Equipamentos.Clear()
})

# Botão: Sair
$SairBtn.Add_Click({ $window.Close() })

# Exibe a janela
$window.ShowDialog() | Out-Null