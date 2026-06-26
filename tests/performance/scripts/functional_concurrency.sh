#!/usr/bin/env bash
# ============================================================
# T29 批量并发功能测试脚本
# ------------------------------------------------------------
# 用途: 对核心接口做 10 并发只读访问验证, 并对预约提交做 20 并发
#       冲突测试 (BUG-001 回归探针: SUT 已加冲突检测, 验证高并发下是否生效)。
# 前置:
#   1. 已通过 start.sh/start.ps1/start.bat 启动 SUT。
#   2. 已安装 curl。
# 用法:
#   bash functional_concurrency.sh
#   BASE=http://localhost:8080 bash functional_concurrency.sh
# 输出:
#   终端打印 PASS/FAIL 汇总;中间结果写入系统临时目录。
# ============================================================

set -u

BASE="${BASE:-http://localhost:8080}"
AUTH=""
PASS=0
FAIL=0
TMP="${TMPDIR:-/tmp}"

# 获取Token
RESP=$(curl -s -X POST "$BASE/login" -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","code":"","uuid":""}')
RAW_TOKEN=$(echo "$RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
AUTH="Authorization: Bearer $RAW_TOKEN"

# ===== 诚信守卫: 没有 token 不要把"什么都没成功"误判为通过 =====
# RuoYi 登录默认带图形验证码, 纯接口登录通常失败 → token 为空。
# 若不拦截, 后续所有请求都是未鉴权/失败, 而"成功数=0 ≤ 1"会被误报为"冲突检测正常 PASS"。
if [ -z "$RAW_TOKEN" ]; then
  echo "[SKIP] 未获取到 JWT Token (登录响应: ${RESP:0:120})。"
  echo "[SKIP] 可能原因: 图形验证码已开启 / 账号口令变更。本脚本依赖鉴权, 无法判定, 标记 SKIP 退出。"
  echo "[提示] 请在测试环境关闭验证码(captchaEnabled=false)后重跑。"
  exit 2
fi

log_result() {
  local endpoint="$1"
  local method="$2"
  local concurrency="$3"
  local result="$4"
  local expected="$5"

  if echo "$result" | grep -q "$expected"; then
    echo "  PASS | $method $endpoint | 并发:$concurrency | $(echo "$result" | head -c 80)"
    PASS=$((PASS+1))
  else
    echo "  FAIL | $method $endpoint | 并发:$concurrency | $(echo "$result" | head -c 80)"
    FAIL=$((FAIL+1))
  fi
}

echo "============================================"
echo "  T29 并发功能测试套件"
echo "  开始时间: $(date)"
echo "============================================"

# ----- GET请求并发测试 -----
echo ""
echo "--- GET请求 10并发测试 ---"

for i in $(seq 1 10); do
  (curl -s "$BASE/labs_browse/labs_browse/list" -H "$AUTH" -o /dev/null -w "%{http_code}\n") &
done | sort | uniq -c > "$TMP/concurrent_get.txt"
wait
log_result "/labs_browse/labs_browse/list" "GET" "10" "$(cat "$TMP/concurrent_get.txt")" "200"

for i in $(seq 1 10); do
  (curl -s "$BASE/apply/apply/list" -H "$AUTH" -o /dev/null -w "%{http_code}\n") &
done | sort | uniq -c > "$TMP/concurrent_schedule.txt"
wait
log_result "/apply/apply/list" "GET" "10" "$(cat "$TMP/concurrent_schedule.txt")" "200"

for i in $(seq 1 10); do
  (curl -s "$BASE/reservation/reservation/list" -H "$AUTH" -o /dev/null -w "%{http_code}\n") &
done | sort | uniq -c > "$TMP/concurrent_resv.txt"
wait
log_result "/reservation/reservation/list" "GET" "10" "$(cat "$TMP/concurrent_resv.txt")" "200"

# ----- POST请求并发测试 -----
echo ""
echo "--- POST提交预约 20并发冲突测试 ---"

TOMORROW=$(date -d "+1 day" +%Y-%m-%d 2>/dev/null || date -v+1d +%Y-%m-%d 2>/dev/null || echo "2026-07-01")

SUCCESS_COUNT=0
FAIL_COUNT=0

# 20个并发预约同一实验室同时段
for i in $(seq 1 20); do
  (
    CODE=$(curl -s -X POST "$BASE/reservation/reservation/insert" \
      -H "$AUTH" -H "Content-Type: application/json" \
      -d "{\"userId\":1,\"labId\":2,\"reservationDate\":\"$TOMORROW\",\"timeSlot\":3,\"purpose\":\"并发$i\"}" \
      | grep -o '"code":[0-9]*' | cut -d: -f2)
    echo "$CODE"
  ) &
done > "$TMP/concurrent_insert.txt"
wait

SUCCESS_COUNT=$(grep -c "200" "$TMP/concurrent_insert.txt" 2>/dev/null || echo 0)
FAIL_COUNT=$(grep -c "500" "$TMP/concurrent_insert.txt" 2>/dev/null || echo 0)
TOTAL_COUNT=$(grep -cE '[0-9]' "$TMP/concurrent_insert.txt" 2>/dev/null || echo 0)

echo "  并发预约结果: 成功=$SUCCESS_COUNT, 失败(500)=$FAIL_COUNT, 总响应=$TOTAL_COUNT"
echo "  预期(冲突检测生效): 恰好 1 条成功, 其余因冲突被拒绝"
# 诚信判定: 严格"恰好 1 条成功"才算冲突检测正常。
# 0 条成功 = 鉴权/种子数据问题, 不能判 PASS(否则"什么都没成功"会被误报通过)。
if [ "$SUCCESS_COUNT" -eq 0 ]; then
  echo "  INCONCLUSIVE | 0 条成功: 可能是 labId=2 不存在/无排期/权限问题, 无法判定冲突检测, 请检查种子数据后重试。"
  FAIL=$((FAIL+1))
elif [ "$SUCCESS_COUNT" -eq 1 ]; then
  echo "  PASS | 冲突检测正常 (恰好 1 条成功)"
  PASS=$((PASS+1))
else
  echo "  FAIL | 冲突检测未生效: 产生了 ${SUCCESS_COUNT} 条重复预约。"
  echo "         注: SUT 已加冲突检测, 但 insert 路径冲突查询 id 为 null 叠加 mapper 的 'AND id != #{id}',"
  echo "         在 MySQL 中 id!=NULL 恒为 UNKNOWN 可能导致检测不命中(参见 tests/e2e/README.md)。"
  FAIL=$((FAIL+1))
fi

# ===== 结果汇总 =====
echo ""
echo "============================================"
echo "  结果: PASS=$PASS  FAIL=$FAIL"
echo "  结束时间: $(date)"
echo "============================================"
