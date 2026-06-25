# 人工(手动)测试套件 — Manual Test Suite

> 课题编号：T29 · 开放实验室网上预约管理系统(基于 RuoYi v3.8.9 二次开发)
> 作者：雷清亮 (QINGLIANG LEI) · 指导教师：刘嘉 · 软件测试综合训练课程设计

本目录提供一套**可下载即用**的人工测试资产。你在本地把被测系统(SUT)跑起来后，
可照着这里的清单、charter 和用例**重新手动复测**整个系统，并把结果记录进执行日志模板。

---

## 一、本套件的定位

人工测试轨道与自动化轨道(`system-under-test/back/labs/.../src/test/java`,JUnit5+Mockito)互补。
自动化轨道在不依赖数据库/Redis 的前提下验证 Service/Controller 的纯逻辑;
人工轨道则覆盖**端到端真实交互**——登录验证码、前端表单、跨模块流程、以及自动化难以触达的探索式发现。

本套件包含两种人工测试形态:

1. **基于会话的探索式测试 (Session-Based Exploratory Testing)**
   以 charter(测试章程)为单位,带着明确 mission 在时间盒内自由探索,记录观察与疑点。
   见 [`charters/`](charters/)。
2. **脚本化手工用例 (Scripted Manual Test Cases)**
   预先写好步骤与预期的确定性用例,逐条执行、逐条判定。
   见 [`manual-test-cases.md`](manual-test-cases.md) 与 [`smoke-checklist.md`](smoke-checklist.md)。

> **诚信声明**:本目录中的清单/charter/用例是"**供本地复测的脚本**",不代表"已经跑出某个结果"。
> 历史真实执行结论(通过率 79.3%、12 个缺陷等)只记录在 [`docs/06-reports/`](../../docs/06-reports/)。
> 你跑出的新结果请填入 [`execution-log-template.md`](execution-log-template.md),不要改动历史报告。

---

## 二、前置条件 / 环境准备

### 2.1 启动被测系统

在仓库根目录执行一键启动脚本(基于 Docker Compose 拉起 MySQL + Redis + 后端 + 前端):

```bash
# Linux / macOS / Git Bash
./start.sh

# Windows PowerShell
./start.ps1

# Windows CMD
start.bat
```

停止:对应的 `stop.sh` / `stop.ps1` / `stop.bat`。

### 2.2 访问入口与账号

| 项 | 默认值 | 说明 |
|----|--------|------|
| 前端 | http://localhost:81 | 手测主入口 |
| 后端 API | http://localhost:8080 | 接口直测(curl/Postman) |
| 管理员账号 | `admin` / `admin123` | 登录带**图形验证码** |
| MySQL(宿主机) | localhost:3307 | 可经 `system-under-test/.env` 覆盖 |
| Redis(宿主机) | localhost:6380 | 可经 `system-under-test/.env` 覆盖 |

> 端口可在 `system-under-test/.env` 中调整。RuoYi 登录需要输入图形验证码,自动化脚本无法绕过,因此登录这一步天然属于人工轨道。

### 2.3 复测前自检

- [ ] `docker ps` 能看到 MySQL/Redis/后端/前端四个容器均为 `Up`
- [ ] 浏览器打开 http://localhost:81 能加载登录页
- [ ] 用 `admin/admin123` + 验证码 能成功登录进入首页

---

## 三、怎么用这套件(推荐顺序)

1. **冒烟先行**:起服务后先跑 [`smoke-checklist.md`](smoke-checklist.md)(约 12-15 步,几分钟)。
   冒烟不过就先排查环境,不要继续往下测。
2. **脚本化回归**:执行 [`manual-test-cases.md`](manual-test-cases.md) 中约 20 条高价值用例,逐条判定。
   尤其关注标注了关联缺陷(BUG-001 等)的用例,验证缺陷是否仍存在。
3. **探索式补充**:挑选 [`charters/`](charters/) 下相关模块的 charter,按 mission 在时间盒内自由探索,
   记录新发现的疑点。
4. **记录结果**:把本轮所有结果(通过/失败/新缺陷)填入 [`execution-log-template.md`](execution-log-template.md) 的副本。

---

## 四、目录结构

```
tests/manual/
├── README.md                    # 本文件:人工测试轨道总览
├── smoke-checklist.md           # 起服务后立即执行的冒烟清单
├── manual-test-cases.md         # 约20条高价值脚本化手工用例
├── execution-log-template.md    # 空白执行记录模板(复测时填写)
└── charters/                    # 基于会话的探索式测试章程
    ├── M1-lab-management.md      # 实验室管理
    ├── M2-schedule-management.md # 排期管理
    ├── M3-reservation.md         # 预约管理
    ├── M4-review.md              # 预约审核
    └── auth-security.md          # 鉴权与安全
```

---

## 五、关联文档

| 文档 | 路径 | 用途 |
|------|------|------|
| 测试结果汇总(历史真实结论) | [`docs/06-reports/09_测试结果汇总.md`](../../docs/06-reports/09_测试结果汇总.md) | 计划63/执行58/通过率79.3%/12缺陷 |
| 测试用例总表 | [`docs/03-test-design/测试用例总表.md`](../../docs/03-test-design/测试用例总表.md) | 全量用例(FT/VAL/BIZ/SEC...) |
| 测试用例设计 | [`docs/03-test-design/04_测试用例设计.md`](../../docs/03-test-design/04_测试用例设计.md) | 用例设计方法与思路 |
| 白盒路径分析 | [`docs/03-test-design/06_白盒路径分析.md`](../../docs/03-test-design/06_白盒路径分析.md) | 关键方法控制流 |
| T29 缺陷清单 | [`docs/05-defects/T29_缺陷清单.md`](../../docs/05-defects/T29_缺陷清单.md) | BUG-001~012 详情 |
| 缺陷分析与修复 | [`docs/05-defects/07_缺陷分析与修复.md`](../../docs/05-defects/07_缺陷分析与修复.md) | 根因与修复方案 |

---

## 六、已知缺陷速查(手测时重点观察)

下表汇总测试战役(2026-06)发现的 12 个缺陷。**「当前源码状态」列依据被测源码注释**
([`ReservationController.java`](../../system-under-test/back/labs/labs-management/src/main/java/com/labs/reservation/controller/ReservationController.java) 顶部明确标注 BUG-001/002/003/004/005/006/010 已修复);
而测试战役结束当时的历史关闭率为 6/12,逐条历史状态见 [缺陷清单](../../docs/05-defects/T29_缺陷清单.md)。
**复测以本地实际复现结果为准。**

| 缺陷 | 现象 | 当前源码状态 | 模块 |
|------|------|------|------|
| BUG-001 | 同实验室同日期同时段可重复预约(无冲突检测) | ✅ 已修复 | M3 |
| BUG-002 | timeSlot 可提交 99 等非法值(无 1-4 范围校验) | ✅ 已修复 | M3 |
| BUG-003 | 可预约过去日期(如 2020-01-01) | ✅ 已修复 | M3 |
| BUG-004 | 审核通过后未自动拒绝其他冲突待审核预约 | ✅ 已修复 | M4 |
| BUG-005 | labId 校验(空 labId 可预约) | ✅ 已修复(非空校验)¹ | M3 |
| BUG-006 | 空 purpose 可提交 | ✅ 已修复 | M3 |
| BUG-007 | 排期发布接受过去日期 | ⚠️ 未修复(预期可复现) | M2 |
| BUG-008 | 删除实验室无关联检查,关联数据未级联 | ⚠️ 未修复(预期可复现) | M1 |
| BUG-009 | 前端时间段显示不一致(14:00 vs 14:30) | 🔎 待复测确认 | 前端 |
| BUG-010 | 已审核预约可被重复审核覆盖 | ✅ 已修复 | M4 |
| BUG-011 | 排期重复发布无去重 | ⚠️ 未修复(预期可复现) | M2 |
| BUG-012 | 操作日志可能泄露请求参数中的敏感信息 | ⚠️ 未修复(预期可复现) | 全局 |

> ¹ BUG-005 源码修复为 **labId 非空校验**;`labId=99999`(非空但不存在)这一子场景是否被拦截需以本地复测为准。
> ⚠️ BUG-001 源码虽加冲突检测,但 insert 路径冲突查询未设 id,叠加 mapper 的 `AND id != #{id}`(MySQL `id!=NULL` 恒为 UNKNOWN),
> **可能导致检测在 insert 路径不命中**,第二次预约仍成功——**结果存疑,务必实测**(详见 [tests/e2e/README.md](../e2e/README.md))。
>
> **重点回归**:BUG-002/003/006/010 等已在源码修复,复测应「无法复现」,可作为**回归守护点**;
> BUG-007/008/011/012 预期仍可复现。另一反复出现的现象:未授权访问受保护接口返回 **500 而非 401**(API-08 / SEC-01,见报告)。

---

*本套件为人工复测脚本,产物均需人工执行确认。AI 仅作为编写助手,未自主运行任何测试。*
