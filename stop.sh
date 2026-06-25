#!/usr/bin/env bash
# 停止服务。 加 --wipe 同时删除数据卷（清空数据库）。
set -euo pipefail
cd "$(dirname "$0")/system-under-test"
if [ "${1:-}" = "--wipe" ]; then
  echo "[OLRS] 停止并删除容器与数据卷 ..."
  docker compose down -v
else
  echo "[OLRS] 停止服务（保留数据）..."
  docker compose stop
fi
