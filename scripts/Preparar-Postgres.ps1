# Verifica PostgreSQL local y crea bases separadas para la aplicación y sus pruebas.
$ErrorActionPreference = 'Stop'
$PgBin = 'C:\Program Files\PostgreSQL\18\bin'
$env:PGPASSWORD = Read-Host 'Clave local del usuario postgres'
$ready = & (Join-Path $PgBin 'pg_isready.exe') -h localhost -p 5432
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL no acepta conexiones en localhost:5432.' }
foreach ($db in @('formulas_avance3', 'formulas_avance3_test')) {
    $exists = & (Join-Path $PgBin 'psql.exe') -h localhost -p 5432 -U postgres -d postgres -At -c "SELECT 1 FROM pg_database WHERE datname='$db'"
    if ($LASTEXITCODE -ne 0) { throw "No se pudo consultar PostgreSQL. $ready" }
    if ($exists -ne '1') {
        & (Join-Path $PgBin 'createdb.exe') -h localhost -p 5432 -U postgres $db
        if ($LASTEXITCODE -ne 0) { throw "No se pudo crear la base $db." }
    }
}
$env:DB_URL = 'jdbc:postgresql://localhost:5432/formulas_avance3'
$env:DB_USER = 'postgres'
$env:DB_PASSWORD = $env:PGPASSWORD
Write-Host 'PostgreSQL verificado. Bases formulas_avance3 y formulas_avance3_test listas.'
