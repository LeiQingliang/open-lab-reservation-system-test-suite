#!/usr/bin/env bash
# ============================================================
# T29 Apache Bench 性能测试脚本
# ------------------------------------------------------------
# 用途: 使用 Apache Bench (ab) 对开放实验室预约系统的核心只读接口
#       及预约提交接口做单接口吞吐量 / 响应时间基准测试。
# 前置:
#   1. 已通过仓库根目录 start.sh/start.ps1/start.bat 启动 SUT
#      (后端 http://localhost:8080, 账号 admin/admin123)。
#   2. 已安装 Apache Bench (ab) 与 curl。
# 用法:
#   bash ab_benchmark.sh
#   BASE_URL=http://localhost:8080 bash ab_benchmark.sh
# 输出:
#   ./ab_reports/ 目录下的 *.txt (ab 原始报告) 与 *.tsv (gnuplot 数据)。
# 注意:
#   预约提交接口会写入真实数据，跑完请用 ../sql/db_performance.sql
#   附录或 README 的清理语句清理测试数据。
# ============================================================

set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
REPORT_DIR="./ab_reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p "$REPORT_DIR"

# ===== 1. 获取认证Token =====
echo "=== 获取JWT Token ==="
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","code":"","uuid":""}')
TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# ===== 诚信守卫: 未拿到 token 则不要用 401 路径冒充吞吐量 =====
# RuoYi 登录默认带图形验证码, 纯接口登录 (code/uuid 留空) 通常失败 → TOKEN 为空。
# 此时需鉴权的 AB-002~005 实际在压测 401 响应, ab 仍会输出 RPS, 属于误导性数据。
if [ -z "$TOKEN" ]; then
  echo "[WARN] 未获取到 JWT Token (登录响应: ${LOGIN_RESP:0:120})。"
  echo "[WARN] 可能原因: 图形验证码已开启 / 账号口令变更。"
  echo "[WARN] 仅运行匿名接口基准 (AB-001 实验室列表 @Anonymous), 跳过需鉴权的接口以免输出 401 路径的伪吞吐量。"
  echo ""
  echo "=== AB-001(匿名): 实验室列表 GET - 10并发 100请求 ==="
  ab -n 100 -c 10 -H "Accept: application/json" \
    -g "$REPORT_DIR/labs_list_${TIMESTAMP}.tsv" \
    "$BASE_URL/labs_browse/labs_browse/list" \
    > "$REPORT_DIR/labs_list_${TIMESTAMP}.txt" 2>&1
  echo "结果: $(grep 'Requests per second' "$REPORT_DIR/labs_list_${TIMESTAMP}.txt")"
  echo "失败: $(grep 'Failed requests' "$REPORT_DIR/labs_list_${TIMESTAMP}.txt")"
  echo ""
  echo "[提示] 如需完整压测, 请在测试环境关闭验证码(captchaEnabled=false)后重跑。报告目录: $REPORT_DIR"
  exit 0
fi
echo "Token: ${TOKEN:0:30}..."

# ===== 2. 实验室列表基准测试 =====
echo ""
echo "=== AB-001: 实验室列表 GET - 10并发 100请求 ==="
ab -n 100 -c 10 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json" \
  -g "$REPORT_DIR/labs_list_${TIMESTAMP}.tsv" \
  "$BASE_URL/labs_browse/labs_browse/list" \
  > "$REPORT_DIR/labs_list_${TIMESTAMP}.txt" 2>&1
echo "结果: $(grep 'Requests per second' "$REPORT_DIR/labs_list_${TIMESTAMP}.txt")"

# ===== 3. 实验室详情测试 =====
echo ""
echo "=== AB-002: 实验室详情 GET - 10并发 100请求 ==="
ab -n 100 -c 10 \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/labs_browse/labs_browse/1" \
  > "$REPORT_DIR/labs_detail_${TIMESTAMP}.txt" 2>&1
echo "结果: $(grep 'Requests per second' "$REPORT_DIR/labs_detail_${TIMESTAMP}.txt")"

# ===== 4. 排期列表测试 =====
echo ""
echo "=== AB-003: 排期列表 GET - 10并发 100请求 ==="
ab -n 100 -c 10 \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/apply/apply/list" \
  > "$REPORT_DIR/schedule_list_${TIMESTAMP}.txt" 2>&1
echo "结果: $(grep 'Requests per second' "$REPORT_DIR/schedule_list_${TIMESTAMP}.txt")"

# ===== 5. 预约列表测试 =====
echo ""
echo "=== AB-004: 预约列表 GET - 10并发 100请求 ==="
ab -n 100 -c 10 \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/reservation/reservation/list" \
  > "$REPORT_DIR/reservation_list_${TIMESTAMP}.txt" 2>&1
echo "结果: $(grep 'Requests per second' "$REPORT_DIR/reservation_list_${TIMESTAMP}.txt")"

# ===== 6. 预约提交POST测试 (不同并发梯度) =====
echo ""
echo "=== AB-005: 预约提交 POST - 梯度并发测试 ==="

RESERVATION_JSON='{"userId":1,"labId":1,"reservationDate":"2026-07-01","timeSlot":1,"purpose":"AB性能测试"}'

# 保存JSON到临时文件
POST_DATA="$REPORT_DIR/ab_post_data.json"
echo "$RESERVATION_JSON" > "$POST_DATA"

for CONC in 1 5 10 20 50; do
    echo ""
    echo "  --- 并发数: $CONC, 请求数: $((CONC * 10)) ---"
    ab -n $((CONC * 10)) -c "$CONC" \
      -T "application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -p "$POST_DATA" \
      "$BASE_URL/reservation/reservation/insert" \
      > "$REPORT_DIR/reservation_insert_c${CONC}_${TIMESTAMP}.txt" 2>&1
    echo "  RPS: $(grep 'Requests per second' "$REPORT_DIR/reservation_insert_c${CONC}_${TIMESTAMP}.txt")"
    echo "  平均时间: $(grep 'Time per request.*mean' "$REPORT_DIR/reservation_insert_c${CONC}_${TIMESTAMP}.txt" | head -1)"
done

rm -f "$POST_DATA"

# ===== 7. 汇总所有结果 =====
echo ""
echo "============================================"
echo "  性能测试汇总"
echo "============================================"
for f in "$REPORT_DIR"/*_${TIMESTAMP}.txt; do
    [ -f "$f" ] || continue
    name=$(basename "$f" .txt | sed "s/_${TIMESTAMP}//")
    rps=$(grep 'Requests per second' "$f" | head -1 | awk '{print $4}')
    tpr=$(grep 'Time per request.*mean' "$f" | head -1 | awk '{print $4}')
    failed=$(grep 'Failed requests' "$f" | awk '{print $3}')
    echo "| $name | ${rps:-N/A} req/s | ${tpr:-N/A} ms | ${failed:-0} failures |"
done

echo ""
echo "报告目录: $REPORT_DIR"
