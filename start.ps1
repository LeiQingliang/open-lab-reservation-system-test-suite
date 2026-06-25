# ============================================================================
# 开放实验室预约系统 · 一键启动 (Windows / PowerShell)
# 用法：右键“使用 PowerShell 运行”，或在终端执行  ./start.ps1
# 作用：构建并启动 MySQL + Redis + 后端 + 前端，等待就绪后给出访问地址。
# ============================================================================
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location (Join-Path $root 'system-under-test')

Write-Host "[OLRS] 检查 Docker ..." -ForegroundColor Cyan
try { docker info *> $null } catch {
  Write-Host "[OLRS] 未检测到正在运行的 Docker，请先启动 Docker Desktop。" -ForegroundColor Red
  exit 1
}

# 读取 .env 中的宿主机端口（缺省 81 / 8080）
$frontPort = 81; $backPort = 8080
if (Test-Path .env) {
  Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*FRONTEND_HOST_PORT\s*=\s*(\d+)') { $frontPort = $Matches[1] }
    if ($_ -match '^\s*BACKEND_HOST_PORT\s*=\s*(\d+)')  { $backPort  = $Matches[1] }
  }
}

Write-Host "[OLRS] 构建并启动容器（首次会下载镜像/依赖，请耐心等待几分钟）..." -ForegroundColor Cyan
docker compose up -d --build
if ($LASTEXITCODE -ne 0) { Write-Host "[OLRS] 启动失败，请查看上方日志。" -ForegroundColor Red; exit 1 }

Write-Host "[OLRS] 等待后端就绪 (最多 5 分钟) ..." -ForegroundColor Cyan
$deadline = (Get-Date).AddMinutes(5)
$ready = $false
while ((Get-Date) -lt $deadline) {
  try {
    $r = Invoke-WebRequest -UseBasicParsing -TimeoutSec 4 "http://localhost:$backPort/" 2>$null
    if ($r.StatusCode -eq 200) { $ready = $true; break }
  } catch { }
  Start-Sleep -Seconds 5
  Write-Host "  ... 后端启动中" -ForegroundColor DarkGray
}

Write-Host ""
if ($ready) {
  Write-Host "==================================================================" -ForegroundColor Green
  Write-Host " ✅ 启动成功！" -ForegroundColor Green
  Write-Host "   前端访问： http://localhost:$frontPort" -ForegroundColor Green
  Write-Host "   后端接口： http://localhost:$backPort" -ForegroundColor Green
  Write-Host "   登录账号： admin / admin123" -ForegroundColor Green
  Write-Host "==================================================================" -ForegroundColor Green
} else {
  Write-Host "[OLRS] 后端未在 5 分钟内就绪，可执行以下命令查看日志：" -ForegroundColor Yellow
  Write-Host "   docker compose -f system-under-test/docker-compose.yml logs -f backend" -ForegroundColor Yellow
}
Write-Host "查看状态： docker compose -f system-under-test/docker-compose.yml ps"
Write-Host "停止服务： ./stop.ps1   （加 -Wipe 可清空数据卷）"
