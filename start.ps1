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

Write-Host "[OLRS] 连接体检：依次等待各服务就绪 (最多 5 分钟) ..." -ForegroundColor Cyan
# 顺序与依赖链一致：MySQL/Redis -> 后端(连上库+缓存才 healthy) -> 前端
$services = @(
  @{ Name = 'MySQL'; Container = 'olrs-mysql' },
  @{ Name = 'Redis'; Container = 'olrs-redis' },
  @{ Name = '后端 '; Container = 'olrs-backend' },
  @{ Name = '前端 '; Container = 'olrs-frontend' }
)
$deadline = (Get-Date).AddMinutes(5)
$allOk = $true
foreach ($s in $services) {
  $ok = $false
  while ((Get-Date) -lt $deadline) {
    # 有 healthcheck 取健康状态(healthy/starting/unhealthy)，否则退化为容器运行状态
    $status = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $s.Container 2>$null
    if ($status -eq 'healthy' -or $status -eq 'running') { $ok = $true; break }
    Start-Sleep -Seconds 3
  }
  if ($ok) {
    Write-Host ("   [OK]   {0} 已连通" -f $s.Name) -ForegroundColor Green
  } else {
    Write-Host ("   [FAIL] {0} 未就绪 ({1})" -f $s.Name, $s.Container) -ForegroundColor Red
    $allOk = $false
  }
}

Write-Host ""
if ($allOk) {
  Write-Host "==================================================================" -ForegroundColor Green
  Write-Host " ✅ 启动成功！MySQL / Redis / 后端 / 前端 全部连接正常" -ForegroundColor Green
  Write-Host "   前端访问： http://localhost:$frontPort" -ForegroundColor Green
  Write-Host "   后端接口： http://localhost:$backPort" -ForegroundColor Green
  Write-Host "   登录账号： admin / admin123" -ForegroundColor Green
  Write-Host "==================================================================" -ForegroundColor Green
} else {
  Write-Host "[OLRS] 有服务未在 5 分钟内就绪，请按下方命令排查对应日志：" -ForegroundColor Yellow
  Write-Host "   docker compose -f system-under-test/docker-compose.yml ps" -ForegroundColor Yellow
  Write-Host "   docker compose -f system-under-test/docker-compose.yml logs -f mysql redis backend frontend" -ForegroundColor Yellow
}
Write-Host "查看状态： docker compose -f system-under-test/docker-compose.yml ps"
Write-Host "停止服务： ./stop.ps1   （加 -Wipe 可清空数据卷）"

