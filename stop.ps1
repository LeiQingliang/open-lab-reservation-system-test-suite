# 停止开放实验室预约系统。 加参数 -Wipe 同时删除数据卷（清空数据库）。
param([switch]$Wipe)
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location (Join-Path $root 'system-under-test')
if ($Wipe) {
  Write-Host "[OLRS] 停止并删除容器与数据卷 ..." -ForegroundColor Yellow
  docker compose down -v
} else {
  Write-Host "[OLRS] 停止服务（保留数据）..." -ForegroundColor Cyan
  docker compose stop
}
