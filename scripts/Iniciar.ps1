# Prepara PostgreSQL, compila el backend, instala las dependencias necesarias e inicia API y Angular en segundo plano. Guarda logs e identificadores para poder detenerlos.
param([switch]$SoloBackend)
$ErrorActionPreference = 'Stop'
$Materiales = Split-Path -Parent $PSScriptRoot
$Proyecto = Split-Path -Parent $PSScriptRoot
$Runtime = Join-Path $Materiales '.runtime'
& (Join-Path $PSScriptRoot 'Preparar-Postgres.ps1')
$Backend = Join-Path $Proyecto 'backend'
$Frontend = Join-Path $Proyecto 'frontend'
Push-Location $Backend
try {
    & '.\mvnw.cmd' -q -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar el backend.' }
} finally { Pop-Location }
Push-Location $Frontend
try {
    if (-not $SoloBackend -and -not (Test-Path -LiteralPath 'node_modules')) {
        & npm.cmd ci --no-audit --no-fund
        if ($LASTEXITCODE -ne 0) { throw 'No se pudieron instalar las dependencias de Angular.' }
    }
} finally { Pop-Location }
$PuertoBackend = Get-NetTCPConnection -State Listen -LocalPort 8082 -ErrorAction SilentlyContinue
$PuertoFrontend = Get-NetTCPConnection -State Listen -LocalPort 4202 -ErrorAction SilentlyContinue
if ($PuertoBackend -or ($PuertoFrontend -and -not $SoloBackend)) {
    throw 'Un puerto de la aplicación está ocupado (8082 o 4202). Si ya la iniciaste, abre http://localhost:4202; para reiniciar usa Detener.ps1.'
}
$PropiedadJava = (& java.exe -XshowSettings:properties -version 2>&1 | Select-String '^\s+java.home =').ToString()
$CarpetaJava = $PropiedadJava.Split('=', 2)[1].Trim()
$Java = Join-Path $CarpetaJava 'bin/java.exe'
if (-not $SoloBackend) { $Node = (Get-Command node.exe).Source }
if (-not $env:JWT_SECRET) {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
}
$ProcesoBackend = Start-Process -FilePath $Java -ArgumentList '-jar', 'target/formulas-avance2-1.0.0.jar' -WorkingDirectory $Backend -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $Runtime 'backend.log') -RedirectStandardError (Join-Path $Runtime 'backend-error.log')
if (-not $SoloBackend) { $ProcesoFrontend = Start-Process -FilePath $Node -ArgumentList 'node_modules/@angular/cli/bin/ng.js', 'serve', '--host', '127.0.0.1', '--port', '4202' -WorkingDirectory $Frontend -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $Runtime 'frontend.log') -RedirectStandardError (Join-Path $Runtime 'frontend-error.log') }
@{ backend = $ProcesoBackend.Id; frontend = $ProcesoFrontend.Id } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $Runtime 'procesos.json')
$Lista = $false
for ($Intento = 0; $Intento -lt 45; $Intento++) {
    $ApiLista = [bool](Get-NetTCPConnection -State Listen -LocalPort 8082 -ErrorAction SilentlyContinue)
    $FrontLista = [bool](Get-NetTCPConnection -State Listen -LocalPort 4202 -ErrorAction SilentlyContinue)
    if ($ApiLista -and ($SoloBackend -or $FrontLista)) { $Lista = $true; break }
    if ($ProcesoBackend.HasExited -or ($ProcesoFrontend -and $ProcesoFrontend.HasExited)) {
        throw 'Un proceso terminó durante el arranque. Consulta sus logs en .runtime.'
    }
    Start-Sleep -Seconds 2
}
if (-not $Lista) { throw 'La aplicación no terminó de iniciar. Revisa los logs en .runtime.' }
Write-Host 'Aplicación lista: http://localhost:4202'

