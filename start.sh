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

echo "[OLRS] 等待后端就绪 (最多 5 分钟) ..."
ready=0
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:${BACK_PORT}/" >/dev/null 2>&1; then ready=1; break; fi
  sleep 5
  echo "  ... 后端启动中"
done

echo ""
if [ "$ready" = "1" ]; then
  echo "=================================================================="
  echo " ✅ 启动成功！"
  echo "   前端访问： http://localhost:${FRONT_PORT}"
  echo "   后端接口： http://localhost:${BACK_PORT}"
  echo "   登录账号： admin / admin123"
  echo "=================================================================="
else
  echo "[OLRS] 后端未在 5 分钟内就绪，查看日志： docker compose logs -f backend"
fi
echo "停止服务： ./stop.sh        （加 --wipe 清空数据卷）"
