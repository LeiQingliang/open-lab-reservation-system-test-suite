#!/usr/bin/env bash
# ============================================================
# T29 预约并发冲突测试 (curl + 后台进程)
# ------------------------------------------------------------
# 用途: 验证同一实验室、同一日期、同一时段被多个用户并发预约时
#       系统是否具备冲突检测 (对应真实缺陷 BUG-001)。
# 前置:
#   1. 已通过 start.sh/start.ps1/start.bat 启动 SUT。
#   2. 已安装 curl;校验数据库需可访问 MySQL (mysql 客户端)。
#   3. MySQL 默认: 容器内端口 3306, 宿主机映射 3307;
#      库名/账号请按实际环境调整下方 mysql 命令。
# 用法:
#   bash concurrent_conflict.sh
#   BASE_URL=http://localhost:8080 CONCURRENT=20 bash concurrent_conflict.sh
#   MYSQL_HOST=127.0.0.1 MYSQL_PORT=3307 bash concurrent_conflict.sh   # 指向宿主机映射端口
# 输出:
#   ./concurrent_test_results/ 目录;并在终端打印各请求 HTTP/业务码。
# ============================================================

set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
CONCURRENT="${CONCURRENT:-20}"  # 并发数
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3307}"  # SUT MySQL 宿主机默认映射端口
RESULTS_DIR="./concurrent_test_results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p "$RESULTS_DIR"

# ===== 获取Token =====
TOKEN=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","code":"","uuid":""}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# ===== 诚信守卫: 没有 token 则无法做有效的并发预约, 直接退出 =====
if [ -z "$TOKEN" ]; then
  echo "[SKIP] 未获取到 JWT Token (验证码可能已开启)。本测试依赖鉴权, 无法继续, 退出。"
  echo "[提示] 请在测试环境关闭验证码(captchaEnabled=false)后重跑。"
  exit 2
fi
echo "Token: ${TOKEN:0:20}..."

# ===== 并发预约同一实验室同时段 =====
echo ""
echo "=== 并发预约测试: $CONCURRENT 个用户同时预约 实验室1 2026-07-10 时段1 ==="
echo "开始时间: $(date '+%H:%M:%S.%3N')"

# 准备请求数据
REQUEST_DATA='{"userId":1,"labId":1,"reservationDate":"2026-07-10","timeSlot":1,"purpose":"并发测试"}'

# 并发发送请求
for i in $(seq 1 "$CONCURRENT"); do
  (
    RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/reservation/reservation/insert" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$REQUEST_DATA")

    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    SUCCESS=$(echo "$BODY" | grep -o '"code":[0-9]*' | cut -d: -f2)

    echo "[$i] HTTP:$HTTP_CODE code:$SUCCESS $(date '+%H:%M:%S.%3N')"
  ) &
done | tee "$RESULTS_DIR/conflict_${TIMESTAMP}.log"

# 等待所有后台进程
wait

echo "结束时间: $(date '+%H:%M:%S.%3N')"
echo ""
echo "验证数据库中的预约记录数:"

# ===== 验证数据库：应该只有1条成功 =====
# 默认连宿主机映射端口 ${MYSQL_HOST}:${MYSQL_PORT} (可经 MYSQL_HOST/MYSQL_PORT 覆盖, 或在容器内执行)
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u root -proot open_lab_reservation -e \
  "SELECT COUNT(*) AS concurrent_reservations
   FROM reservation
   WHERE lab_id=1 AND reservation_date='2026-07-10' AND time_slot=1;" \
  || echo "[WARN] 数据库校验失败: 请确认 MySQL 地址(${MYSQL_HOST}:${MYSQL_PORT})/账号/库名, 或改为在容器内执行。"

echo ""
echo "期望: 1条 (只有第1个成功，其余应被冲突检测拒绝 → BUG-001 修复有效)"
echo "若出现多条, 说明冲突检测在 insert 路径未生效(BUG-001 修复不完整, 详见 tests/e2e/README.md 的 SQL 说明)"
