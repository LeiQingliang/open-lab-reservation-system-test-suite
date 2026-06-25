#!/usr/bin/env bash
# ============================================================
# T29 性能测试期间系统资源监控脚本
# ------------------------------------------------------------
# 用途: 在执行 JMeter / ab / curl 压测期间, 后台采集 CPU、内存、
#       JVM GC、线程数、Redis、MySQL 连接数等资源指标。
# 前置:
#   1. SUT 已启动。
#   2. 需要以下工具 (按可用性自动跳过): vmstat、free、jps、jstat、
#      jstack、redis-cli、mysql。
#   建议在 Linux / WSL 下运行;部分工具在 Windows 原生 Git Bash 不可用。
# 用法:
#   bash monitor.sh        # 运行后按 Ctrl+C 停止
# 输出:
#   ./perf_monitor_<时间戳>/ 目录下的各类 *.log。
# ============================================================

set -u

MONITOR_DIR="./perf_monitor_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$MONITOR_DIR"

echo "开始系统资源监控..."
echo "监控目录: $MONITOR_DIR"
echo "按 Ctrl+C 停止"

PIDS=()

# ===== CPU监控 =====
if command -v vmstat >/dev/null 2>&1; then
  vmstat 2 > "$MONITOR_DIR/cpu_vmstat.log" &
  CPU_PID=$!
  PIDS+=("$CPU_PID")
fi

# ===== 内存监控 =====
if command -v free >/dev/null 2>&1; then
  while true; do
    echo "$(date '+%H:%M:%S') $(free -m | grep Mem | awk '{print $3"/"$2" "$4" "$6}')" >> "$MONITOR_DIR/memory.log"
    sleep 2
  done &
  MEM_PID=$!
  PIDS+=("$MEM_PID")
fi

# ===== Java进程监控 =====
JAVA_PID=""
if command -v jps >/dev/null 2>&1; then
  JAVA_PID=$(jps | grep -i "labs" | awk '{print $1}')
fi
if [ -n "$JAVA_PID" ]; then
  echo "监控Java进程 PID=$JAVA_PID"

  # JVM内存
  while true; do
    jstat -gc "$JAVA_PID" 1000 1 >> "$MONITOR_DIR/jvm_gc.log" 2>/dev/null
    sleep 2
  done &
  JVM_PID=$!
  PIDS+=("$JVM_PID")

  # 线程数
  while true; do
    THREAD_COUNT=$(jstack "$JAVA_PID" 2>/dev/null | grep "^Thread" | wc -l)
    echo "$(date '+%H:%M:%S') threads=$THREAD_COUNT" >> "$MONITOR_DIR/threads.log"
    sleep 2
  done &
  THREAD_PID=$!
  PIDS+=("$THREAD_PID")
fi

# ===== Redis监控 =====
if command -v redis-cli >/dev/null 2>&1; then
  while true; do
    redis-cli INFO stats | grep -E "total_connections_received|instantaneous_ops_per_sec|keyspace_hits|keyspace_misses" \
      >> "$MONITOR_DIR/redis.log"
    sleep 2
  done &
  REDIS_PID=$!
  PIDS+=("$REDIS_PID")
fi

# ===== MySQL连接数监控 =====
if command -v mysql >/dev/null 2>&1; then
  while true; do
    CONNS=$(mysql -u root -proot -e "SHOW STATUS LIKE 'Threads_connected';" 2>/dev/null | tail -1 | awk '{print $2}')
    echo "$(date '+%H:%M:%S') mysql_connections=$CONNS" >> "$MONITOR_DIR/mysql_connections.log"
    sleep 2
  done &
  MYSQL_PID=$!
  PIDS+=("$MYSQL_PID")
fi

echo "监控进程已启动: PID=${PIDS[*]}"

# ===== 等待用户中断 =====
trap 'echo "停止监控..."; kill "${PIDS[@]}" 2>/dev/null; echo "监控已停止"; exit 0' INT TERM

echo "监控运行中... (按 Ctrl+C 停止)"
while true; do sleep 5; done
