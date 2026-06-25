#!/usr/bin/env bash
# ============================================================================
# 开放实验室预约系统 · 一键启动 (Linux / macOS / Git Bash)
# 用法：  ./start.sh
# ============================================================================
set -euo pipefail
cd "$(dirname "$0")/system-under-test"

echo "[OLRS] 检查 Docker ..."
if ! docker info >/dev/null 2>&1; then
  echo "[OLRS] 未检测到正在运行的 Docker，请先启动 Docker。" >&2
  exit 1
fi

FRONT_PORT=81; BACK_PORT=8080
[ -f .env ] && {
  FRONT_PORT="$(grep -E '^\s*FRONTEND_HOST_PORT' .env | sed -E 's/.*=\s*//' || echo 81)"
  BACK_PORT="$(grep -E '^\s*BACKEND_HOST_PORT' .env | sed -E 's/.*=\s*//' || echo 8080)"
}

echo "[OLRS] 构建并启动容器（首次会下载镜像/依赖，请耐心等待几分钟）..."
docker compose up -d --build

echo "[OLRS] 连接体检：依次等待各服务就绪 (最多 5 分钟) ..."
# 顺序与依赖链一致：MySQL/Redis -> 后端(连上库+缓存才 healthy) -> 前端
names=("MySQL" "Redis" "后端" "前端")
conts=("olrs-mysql" "olrs-redis" "olrs-backend" "olrs-frontend")
all_ok=1
deadline=$(( $(date +%s) + 300 ))
for i in "${!conts[@]}"; do
  ok=0
  while [ "$(date +%s)" -lt "$deadline" ]; do
    # 有 healthcheck 取健康状态，否则退化为容器运行状态
    status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${conts[$i]}" 2>/dev/null || echo '')"
    if [ "$status" = "healthy" ] || [ "$status" = "running" ]; then ok=1; break; fi
    sleep 3
  done
  if [ "$ok" = "1" ]; then
    echo "   [OK]   ${names[$i]} 已连通"
  else
    echo "   [FAIL] ${names[$i]} 未就绪 (${conts[$i]})"
    all_ok=0
  fi
done

echo ""
if [ "$all_ok" = "1" ]; then
  echo "=================================================================="
  echo " ✅ 启动成功！MySQL / Redis / 后端 / 前端 全部连接正常"
  echo "   前端访问： http://localhost:${FRONT_PORT}"
  echo "   后端接口： http://localhost:${BACK_PORT}"
  echo "   登录账号： admin / admin123"
  echo "=================================================================="
else
  echo "[OLRS] 有服务未在 5 分钟内就绪，请排查日志："
  echo "   docker compose ps"
  echo "   docker compose logs -f mysql redis backend frontend"
fi
echo "停止服务： ./stop.sh        （加 --wipe 清空数据卷）"
