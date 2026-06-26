# ============================================================================
# 开放实验室预约系统 · 停止服务 (Windows / PowerShell)
# 用法：
#   ./stop.ps1          停止并移除容器（保留数据卷，下次启动数据仍在）
#   ./stop.ps1 -Wipe    停止 + 删除数据卷（MySQL/Redis/上传文件一并清空，不可恢复）
# ============================================================================
param([switch]$Wipe)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location (Join-Path $root 'system-under-test')

Write-Host "[OLRS] 检查 Docker ..." -ForegroundColor Cyan
try { docker info *> $null } catch {
  Write-Host "[OLRS] 未检测到正在运行的 Docker，请先启动 Docker Desktop。" -ForegroundColor Red
  exit 1
}

if ($Wipe) {
  Write-Host "[OLRS] 停止服务并清空数据卷（MySQL/Redis/上传文件将被删除，不可恢复）..." -ForegroundColor Yellow
  docker compose down -v
} else {
  Write-Host "[OLRS] 停止并移除容器（数据卷保留，下次启动数据仍在）..." -ForegroundColor Cyan
  docker compose down
}
if ($LASTEXITCODE -ne 0) { Write-Host "[OLRS] 停止失败，请查看上方日志。" -ForegroundColor Red; exit 1 }

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Green
if ($Wipe) {
  Write-Host " ✅ 已停止并清空数据卷。下次 ./start.ps1 会重新初始化数据库。" -ForegroundColor Green
} else {
  Write-Host " ✅ 已停止。数据已保留，重新启动： ./start.ps1" -ForegroundColor Green
  Write-Host "    如需彻底清空数据卷： ./stop.ps1 -Wipe" -ForegroundColor DarkGray
}
Write-Host "==================================================================" -ForegroundColor Green

