# Prepara PostgreSQL, ejecuta las pruebas Maven y comprueba la compilación de Angular. Se detiene si alguna verificación falla.
$ErrorActionPreference = 'Stop'
$Materiales = Split-Path -Parent $PSScriptRoot
$Proyecto = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'Preparar-Postgres.ps1')
Push-Location (Join-Path $Proyecto 'backend')
try {
    & '.\mvnw.cmd' '-Dspring.datasource.url=jdbc:postgresql://127.0.0.1:15439/formulas_avance2_test' test
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas del backend.' }
} finally { Pop-Location }
Push-Location (Join-Path $Proyecto 'frontend')
try {
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación de Angular.' }
} finally { Pop-Location }

