# ============================================================
# Script para lanzar la actualización 1.0.0 de Reloquence
# Ejecutar en PowerShell dentro de esta carpeta:
# C:\Users\Angel\Documents\Mis proyectos\Reloquence_6_recreado\reloquence 1.0 renew
# ============================================================

$ErrorActionPreference = "Stop"

$repo = "angelonmx/reloquenceTTS"
$tag  = "v1.0.0"

Write-Host "=== Lanzador de actualización Reloquence 1.0.0 ===" -ForegroundColor Cyan

# 1. Verificar que estamos en la carpeta del proyecto
if (-not (Test-Path "version.properties")) {
    Write-Error "No se encontró version.properties. Ejecuta este script dentro de la carpeta raíz del proyecto."
    exit 1
}

# 2. Verificar que Git y GitHub CLI (gh) existen
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "Git no está instalado o no está en el PATH."
    exit 1
}

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    $gh = "C:\Program Files\GitHub CLI\gh.exe"
    if (Test-Path $gh) {
        $env:PATH += ";" + (Split-Path -Parent $gh)
    } else {
        Write-Error "GitHub CLI (gh) no está instalado. Instálalo desde https://cli.github.com/"
        exit 1
    }
}

# 3. Inicializar git si no hay repositorio
if (-not (Test-Path ".git")) {
    Write-Host "Inicializando repositorio Git..." -ForegroundColor Yellow
    git init
    git remote add origin "https://github.com/$repo.git"
}

# 4. Configurar usuario de git
git config user.name "angelonmx"
git config user.email "angelonmx@users.noreply.github.com"

# 5. Rama principal
git branch -M main

# 6. Agregar todos los cambios
Write-Host "Agregando archivos..." -ForegroundColor Yellow
git add .

# 7. Commit (solo si hay cambios nuevos)
$status = git status --porcelain
if ($status) {
    git commit -m "Conecta servicio al motor: añade todo kotlin/, ediciones full/lite 1.0.0, workflows en la nube"
    Write-Host "Commit realizado." -ForegroundColor Green
} else {
    Write-Host "No hay cambios nuevos para commitear." -ForegroundColor Gray
}

# 8. Subir cambios a GitHub
Write-Host "Subiendo cambios a GitHub..." -ForegroundColor Yellow
git push -u origin main

# 9. Verificar sesión de GitHub
Write-Host "Verificando sesión de GitHub..." -ForegroundColor Yellow
gh auth status

# 10. Lanzar el workflow de release en la nube (compila en GitHub Actions, no en tu PC)
Write-Host "Lanzando workflow de release (tag $tag) en la nube..." -ForegroundColor Cyan
gh workflow run release.yml --repo $repo -f tag=$tag

# 11. Ver progreso
Write-Host "Revisa el progreso en: https://github.com/$repo/actions" -ForegroundColor Cyan
gh run watch --repo $repo

Write-Host "=== Listo. Cuando termine, los APK firmados estarán en la release $tag ===" -ForegroundColor Green