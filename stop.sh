#!/usr/bin/env bash
# ============================================================================
# 开放实验室预约系统 · 停止服务 (Linux / macOS / Git Bash)
# 用法：
#   ./stop.sh           停止并移除容器（保留数据卷，下次启动数据仍在）
#   ./stop.sh --wipe    停止 + 删除数据卷（MySQL/Redis/上传文件一并清空，不可恢复）
# ============================================================================
set -euo pipefail
cd "$(dirname "$0")/system-under-test"

echo "[OLRS] 检查 Docker ..."
if ! docker info >/dev/null 2>&1; then
  echo "[OLRS] 未检测到正在运行的 Docker，请先启动 Docker。" >&2
  exit 1
fi

WIPE=0
for arg in "$@"; do
  case "$arg" in
    --wipe|-w) WIPE=1 ;;
    *) echo "[OLRS] 未知参数: $arg（可用: --wipe）" >&2 ;;
  esac
done

if [ "$WIPE" = "1" ]; then
  echo "[OLRS] 停止服务并清空数据卷（MySQL/Redis/上传文件将被删除，不可恢复）..."
  docker compose down -v
else
  echo "[OLRS] 停止并移除容器（数据卷保留，下次启动数据仍在）..."
  docker compose down
fi

echo ""
echo "=================================================================="
if [ "$WIPE" = "1" ]; then
  echo " ✅ 已停止并清空数据卷。下次 ./start.sh 会重新初始化数据库。"
else
  echo " ✅ 已停止。数据已保留，重新启动： ./start.sh"
  echo "    如需彻底清空数据卷： ./stop.sh --wipe"
fi
echo "=================================================================="
