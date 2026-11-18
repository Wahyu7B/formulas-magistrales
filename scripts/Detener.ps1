# Detiene únicamente los procesos identificados como pertenecientes a esta copia y su servidor PostgreSQL académico. Conserva los archivos de datos.
$ErrorActionPreference = 'Stop'
$Materiales = Split-Path -Parent $PSScriptRoot
$Proyecto = Split-Path -Parent $PSScriptRoot
$Runtime = Join-Path $Materiales '.runtime'
$ArchivoProcesos = Join-Path $Runtime 'procesos.json'
$PuertoAPI = Get-NetTCPConnection -State Listen -LocalPort 8082 -ErrorAction SilentlyContinue
foreach ($Conexion in $PuertoAPI) {
    $ProcesoAPI = Get-CimInstance Win32_Process -Filter "ProcessId=$($Conexion.OwningProcess)" -ErrorAction SilentlyContinue
    if ($ProcesoAPI -and $ProcesoAPI.CommandLine -like '*target/formulas-avance2-1.0.0.jar*') {
        Stop-Process -Id $Conexion.OwningProcess -ErrorAction SilentlyContinue
    }
}
if (Test-Path -LiteralPath $ArchivoProcesos) {
    $Procesos = Get-Content -LiteralPath $ArchivoProcesos -Raw | ConvertFrom-Json
    foreach ($Identificador in @($Procesos.backend, $Procesos.frontend)) {
        $Proceso = Get-CimInstance Win32_Process -Filter "ProcessId=$Identificador" -ErrorAction SilentlyContinue
        if ($Proceso -and ($Proceso.CommandLine -like '*formulas-avance2-1.0.0.jar*' -or $Proceso.CommandLine -like '*node_modules/@angular/cli/bin/ng.js*4202*')) {
            Stop-Process -Id $Identificador -ErrorAction SilentlyContinue
        }
    }
    Remove-Item -LiteralPath $ArchivoProcesos
}
$PgCtl = 'C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe'
$Data = Join-Path $Runtime 'postgres-data'
$DatosAvance2 = Join-Path (Split-Path -Parent $Proyecto) 'Formulas_Magistrales_Avance2_Materiales/.runtime/postgres-data'
if (Test-Path -LiteralPath (Join-Path $DatosAvance2 'PG_VERSION')) {
    $Data = $DatosAvance2
}
if ((Test-Path -LiteralPath $PgCtl) -and (Test-Path -LiteralPath (Join-Path $Data 'postmaster.pid'))) {
    & $PgCtl -D $Data -m fast -w stop
}
Write-Host 'Procesos de esta copia detenidos; los datos quedan guardados.'


