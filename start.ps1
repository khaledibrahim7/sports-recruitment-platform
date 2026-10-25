# ============================================================
#  start.ps1 — تشغيل المنصة الكاملة (Backend + Frontend)
#  الاستخدام: .\start.ps1
# ============================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   منصة الكورة لعبتنا — تشغيل المنصة  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# تحميل إعدادات R2 المحلية من .env (الملف مستبعد من Git)
$scriptDir = $PSScriptRoot
$localEnvFile = Join-Path $scriptDir ".env"
if (Test-Path $localEnvFile) {
    $allowedStorageKeys = @('VIDEO_S3_BUCKET', 'VIDEO_S3_ENDPOINT', 'VIDEO_S3_REGION', 'AWS_ACCESS_KEY_ID', 'AWS_SECRET_ACCESS_KEY', 'VIDEO_S3_URL_TTL')
    foreach ($line in Get-Content $localEnvFile) {
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$' -and $allowedStorageKeys -contains $matches[1]) {
            $value = $matches[2].Trim('"', "'")
            Set-Item -Path "Env:$($matches[1])" -Value $value
        }
    }
}

# ── 1. تحميل Environment Variables ──────────────────────────
$env:SERVER_PORT        = "8080"
$env:DB_HOST            = "localhost"
$env:DB_PORT            = "1433"
$env:DB_NAME            = "SportsPlatform"
$env:DB_USERNAME        = "hr-user"
$env:DB_PASSWORD        = "123"
$env:JWT_SECRET         = "MinimumThirtyTwoCharactersLongSecretKeyHere2026!!"
$env:JWT_EXPIRATION_MS  = "86400000"
$env:GEMINI_API_KEY     = ""        # ← ضع مفتاحك هنا إن أردت AI
$env:ALLOWED_ORIGINS    = "http://localhost:4200"
$env:TRUSTED_PROXY_IPS  = "127.0.0.1,::1"
$env:SWAGGER_ENABLED    = "false"
$env:RATE_LIMIT_GENERAL = "100"
$env:RATE_LIMIT_AUTH    = "10"
$env:RATE_LIMIT_AI      = "20"
$env:SPRING_PROFILES_ACTIVE = "local"
$env:APP_PUBLIC_URL     = "http://localhost:4200"

Write-Host "[1/3] Environment variables loaded" -ForegroundColor Green

# ── 2. إيقاف أي process قديم على port 8080 ──────────────────
$existing = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    $existing | ForEach-Object {
        Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 1
    Write-Host "[2/3] Stopped old backend process on port 8080" -ForegroundColor Yellow
} else {
    Write-Host "[2/3] Port 8080 is free" -ForegroundColor Green
}

# ── 3. مسار الـ JAR ─────────────────────────────────────────
$jarPath   = Join-Path $scriptDir "backend\sports-web\target\sports-web-1.0.0.jar"
$frontendPath = Join-Path $scriptDir "frontend"

if (-not (Test-Path $jarPath)) {
    Write-Host ""
    Write-Host "[ERROR] JAR not found: $jarPath" -ForegroundColor Red
    Write-Host "Run 'mvn clean package -DskipTests' in the backend folder first." -ForegroundColor Red
    exit 1
}

# ── 4. تشغيل Backend في نافذة جديدة ─────────────────────────
$backendArgs = @(
    "-NoExit",
    "-Command",
    "`$env:DB_PASSWORD='123'; `$env:JWT_SECRET='MinimumThirtyTwoCharactersLongSecretKeyHere2026!!'; `$env:ALLOWED_ORIGINS='http://localhost:4200'; `$env:SPRING_PROFILES_ACTIVE='local'; `$env:APP_PUBLIC_URL='http://localhost:4200'; `$env:GEMINI_API_KEY=''; java -jar '$jarPath'"
)
Start-Process powershell -ArgumentList $backendArgs -WindowStyle Normal
Write-Host "[3/3] Backend starting in new window (port 8080)..." -ForegroundColor Green

# ── 5. انتظر حتى يشتغل الـ Backend ─────────────────────────
Write-Host ""
Write-Host "Waiting for backend to be ready..." -ForegroundColor DarkGray
$maxWait = 60
$waited  = 0
$ready   = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 2
    $waited += 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/players?page=0&size=1" -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $ready = $true
            break
        }
    } catch {}
    Write-Host "  ...waiting ($waited s)" -ForegroundColor DarkGray
}

if ($ready) {
    Write-Host ""
    Write-Host "Backend API and database are UP!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[WARN] Backend did not respond in $maxWait seconds. Check the backend window." -ForegroundColor Yellow
}

# ── 6. تشغيل Frontend في نافذة جديدة ────────────────────────
$frontendArgs = @(
    "-NoExit",
    "-Command",
    "Set-Location '$frontendPath'; npx ng serve --port=4200 --open"
)
Start-Process powershell -ArgumentList $frontendArgs -WindowStyle Normal
Write-Host ""
Write-Host "Frontend starting in new window (port 4200)..." -ForegroundColor Green

# ── ملخص ─────────────────────────────────────────────────────
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Backend  →  http://localhost:8080     " -ForegroundColor White
Write-Host "  Frontend →  http://localhost:4200     " -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
