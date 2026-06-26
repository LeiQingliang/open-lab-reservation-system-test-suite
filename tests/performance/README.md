# 性能测试 (Performance Tests)

本目录是「开放式实验室网上预约管理系统」(SUT，基于 RuoYi v3.8.9 二次开发) 的性能测试资产集合，从设计文档 [docs/04-test-implementation/08_性能测试脚本.md](../../docs/04-test-implementation/08_性能测试脚本.md) 抽取整理为**可直接在本地运行**的脚本与配置。

> **诚实声明**
> 课程实测阶段**仅完成了「单请求基线」测量**（5 个接口的单次响应时间，见 [docs/06-reports/09_测试结果汇总.md 第十一节](../../docs/06-reports/09_测试结果汇总.md)）。
> 本目录下的并发 / 压测 / 稳定性脚本是**可供本地复测的资产**，尚未在课程环境中执行，README 与脚本中**不包含任何编造的性能数字**；运行后得到的结果须经人工核对后方可记录。

---

## 1. 测试目标

| 目标编号 | 测试目标 | 指标 | 阈值 |
|----------|----------|------|------|
| PERF-G1 | 验证登录接口响应时间 | 平均响应时间 | < 500ms |
| PERF-G2 | 验证实验室列表查询性能 | 平均响应时间 | < 500ms |
| PERF-G3 | 验证预约提交吞吐量 | TPS | > 50/s |
| PERF-G4 | 验证并发预约无数据错误 | 错误率 | < 1% |
| PERF-G5 | 验证数据库连接池不耗尽 | 活跃连接数 | < 20 |
| PERF-G6 | 验证 Redis 缓存命中率 | 缓存命中率 | > 80% |

> 阈值为**设计期望基线**，并非实测结论。

---

## 2. 场景矩阵

| 场景编号 | 场景名称 | 并发用户 | 持续时间 | 梯度模式 |
|----------|----------|----------|----------|----------|
| SCN-01 | 基准测试 | 1 | 60s | 无 |
| SCN-02 | 常规负载 | 10 | 120s | 5s 内全部启动 |
| SCN-03 | 中等负载 | 50 | 300s | 每 10s 增加 10 用户 |
| SCN-04 | 峰值负载 | 100 | 300s | 每 10s 增加 20 用户 |
| SCN-05 | 压力测试 | 200 | 600s | 每 15s 增加 25 用户 |
| SCN-06 | 稳定性测试 | 50 | 1800s | 一次性启动 |
| SCN-07 | 突发测试 | 10→100 | 60s | 瞬间从 10 跳到 100 |

---

## 3. 前置条件

1. **启动被测系统**：在仓库根目录运行一键脚本（基于 Docker Compose 拉起 MySQL + Redis + 后端 + 前端）：
   - Linux / macOS / WSL：`./start.sh`
   - Windows PowerShell：`./start.ps1`
   - Windows CMD：`start.bat`
   - 停止：对应的 `stop.sh` / `stop.ps1` / `stop.bat`
2. **验证服务可用**：前端 http://localhost:81 ，后端 API http://localhost:8080 ，登录账号 `admin / admin123`（RuoYi 登录带图形验证码，脚本中 `code`/`uuid` 留空仅在验证码关闭或测试放行时可用，必要时请按实际环境调整）。
3. **安装工具**（按所用脚本各取所需）：
   - JDK（推荐 Amazon Corretto 21）
   - Apache JMeter 5.6+（`ab_benchmark` 之外的场景化压测）
   - Apache Bench `ab`
   - `curl`
   - `mysql` 客户端（数据库验证与 SQL 分析）
   - 可选：`redis-cli`、`vmstat`、`free`、`jps`/`jstat`/`jstack`（资源监控，建议 Linux/WSL）

---

## 4. 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `BASE_URL` | `http://localhost:8080` | 后端 API 基址（`ab_benchmark.sh` / `concurrent_conflict.sh`） |
| `BASE` | `http://localhost:8080` | 同上（`functional_concurrency.sh`） |
| `CONCURRENT` | `20` | 并发数（`concurrent_conflict.sh`） |
| 登录账号 | `admin / admin123` | 脚本内硬编码请求体 |

JMeter 通过 `-J` 参数化：`threads`、`rampup`、`loops`、`duration`、`scheduler`（见下）。

---

## 5. 目录结构

```
tests/performance/
├── README.md                          # 本文件
├── jmeter/
│   └── T29_Performance_Test_Plan.jmx  # JMeter 5 测试计划 (登录→预约提交)
├── scripts/
│   ├── ab_benchmark.sh                # Apache Bench 单接口基准 + 梯度并发
│   ├── concurrent_conflict.sh         # curl 并发冲突测试 (复现 BUG-001)
│   ├── functional_concurrency.sh      # 批量并发功能测试
│   └── monitor.sh                     # 系统资源监控 (CPU/内存/JVM/Redis/MySQL)
└── sql/
    └── db_performance.sql             # 数据库性能分析与索引建议
```

---

## 6. 如何运行

### 6.1 JMeter 场景化压测

```bash
cd tests/performance/jmeter

# 场景1: 基准测试 (1用户) —— 按 loops 跑固定次数, 不计时
jmeter -n -t T29_Performance_Test_Plan.jmx \
  -Jthreads=1 -Jrampup=1 -Jloops=1 \
  -l results/baseline.jtl -e -o reports/baseline/

# 场景2: 常规负载 (10用户, 按时长 120s 运行) —— 需 -Jscheduler=true 让 duration 生效
jmeter -n -t T29_Performance_Test_Plan.jmx \
  -Jthreads=10 -Jrampup=5 -Jscheduler=true -Jduration=120 \
  -l results/normal_load.jtl -e -o reports/normal_load/

# 场景3: 中等负载 (50用户, 300s)
jmeter -n -t T29_Performance_Test_Plan.jmx \
  -Jthreads=50 -Jrampup=30 -Jscheduler=true -Jduration=300 \
  -l results/medium_load.jtl -e -o reports/medium_load/

# 场景4: 峰值负载 (100用户, 300s)
jmeter -n -t T29_Performance_Test_Plan.jmx \
  -Jthreads=100 -Jrampup=60 -Jscheduler=true -Jduration=300 \
  -l results/peak_load.jtl -e -o reports/peak_load/

# 场景5: 压力测试 (200用户, 600s)
jmeter -n -t T29_Performance_Test_Plan.jmx \
  -Jthreads=200 -Jrampup=120 -Jscheduler=true -Jduration=600 \
  -l results/stress_test.jtl -e -o reports/stress_test/
```

> ⚙️ **关于运行时长**：线程组 `scheduler` 默认 `false`，此时按 `loops`（循环次数）运行、`duration` 被忽略。
> 需按固定**时长**压测时务必加 `-Jscheduler=true`（如上）。基准场景用 `loops` 即可。
>
> `.jmx` 含两个线程组：**登录认证**（提取 JWT Token 并写入全局 property）与**预约提交-并发测试**
> （经 property 跨线程组读取 Token、随机生成预约数据、断言响应码、加入思考时间）。计划已设
> `serialize_threadgroups=true`，确保**先跑完登录组、再跑提交组**。可在 JMeter GUI 中打开查看 / 微调。

### 6.2 Apache Bench 与 curl 脚本

```bash
cd tests/performance/scripts

bash ab_benchmark.sh            # 单接口基准 + 预约提交梯度并发
bash concurrent_conflict.sh     # 同实验室同时段并发预约 + 数据库校验
bash functional_concurrency.sh  # 多接口并发功能验证 + 冲突检测判定
bash monitor.sh                 # 压测期间后台采集资源指标 (Ctrl+C 停止)
```

可用环境变量覆盖目标地址，例如：
```bash
BASE_URL=http://localhost:8080 bash ab_benchmark.sh
CONCURRENT=50 bash concurrent_conflict.sh
```

### 6.3 数据库性能分析

```bash
mysql -u root -proot open_lab_reservation < tests/performance/sql/db_performance.sql
```

> SQL 中第 5 节的「生成 10 万条测试数据」仅创建存储过程，`CALL` 已注释；执行前请备份数据库。

---

## 7. 输出位置

| 脚本 / 工具 | 输出 |
|-------------|------|
| JMeter | `jmeter/results/*.jtl`（原始数据）、`jmeter/reports/<场景>/`（HTML 报告） |
| `ab_benchmark.sh` | `scripts/ab_reports/*.txt`（ab 报告）、`*.tsv`（gnuplot 数据） |
| `concurrent_conflict.sh` | `scripts/concurrent_test_results/`（含 `conflict_*.log`）+ 终端 |
| `functional_concurrency.sh` | 终端 PASS/FAIL 汇总；中间结果在系统临时目录 |
| `monitor.sh` | `scripts/perf_monitor_<时间戳>/`（各类 `*.log`） |
| `db_performance.sql` | MySQL 客户端输出 / 慢查询日志 |

> `results/`、`reports/`、`ab_reports/`、`concurrent_test_results/`、`perf_monitor_*/` 为运行期产物，建议不纳入版本控制。

---

## 8. 已知缺陷关联（并发回归探针）

**BUG-001：预约无冲突检测**（同一实验室、同一日期、同一时段可重复预约）已在被测源码中加入冲突检测
（`ReservationController.insertReservation`）。下列并发脚本可作为该修复的**回归探针**：

- `concurrent_conflict.sh` 在并发提交后查询数据库，**期望仅 1 条成功**；若出现多条，说明冲突检测未生效。
- `functional_concurrency.sh` 对 20 并发提交结果做判定：恰好 1 条成功判 PASS、0 条判 INCONCLUSIVE、多条判 FAIL。

> ⚠️ **诚实标注**：经核对源码，`insertReservation` 的冲突查询未设 `id`（为 `null`），而 mapper 的
> `selectConflictingReservations` SQL 末尾含 `AND id != #{id}`，MySQL 中 `id != NULL` 恒为 `UNKNOWN`，
> 可能导致冲突检测在 insert 路径**不命中**、第二次预约仍成功。因此这些脚本是**回归探针**而非"必绿守护"——
> 若复现到重复预约，说明 BUG-001 修复在 insert 路径不完整，应反馈 SUT。请以本地实际运行结果为准。
>
> 字段缺校验（`timeSlot=99`、过去日期可提交）与未授权访问返回 500 等历史问题详见
> [docs/06-reports/09_测试结果汇总.md](../../docs/06-reports/09_测试结果汇总.md)；历史缺陷状态见 [缺陷清单](../../docs/05-defects/T29_缺陷清单.md)。

---

## 9. 测试后数据清理

性能 / 并发脚本会写入真实预约数据，运行后请清理（SQL 见 `sql/db_performance.sql` 附录，默认已注释，按需放开）：

```sql
DELETE FROM reservation WHERE purpose LIKE '%性能测试%' OR purpose LIKE '%AB测试%' OR purpose LIKE '%并发%';
DELETE FROM lab_schedule WHERE note LIKE '%性能测试%';
DELETE FROM labs WHERE lab_name LIKE '%TestLab_TC%';
```

清理临时文件：
```bash
rm -rf scripts/perf_monitor_*/ scripts/ab_reports/ scripts/concurrent_test_results/
```

---

## 10. 相关文档

- 设计文档：[docs/04-test-implementation/08_性能测试脚本.md](../../docs/04-test-implementation/08_性能测试脚本.md)
- 测试结果汇总（含单请求基线）：[docs/06-reports/09_测试结果汇总.md](../../docs/06-reports/09_测试结果汇总.md)

---

*课题编号：T29ㅤ|ㅤ作者：雷清亮 (QINGLIANG LEI)ㅤ|ㅤ指导教师：刘嘉ㅤ|ㅤ课程：软件测试综合训练课程设计*
