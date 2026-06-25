# E2E / 接口冒烟测试 (Playwright)

本目录是「开放实验室网上预约管理系统」(SUT) 的端到端与接口冒烟测试**脚手架**，基于
[Playwright Test](https://playwright.dev/)（JavaScript）。测试对准**运行中的 SUT**，
不重复启动服务（应用由仓库根目录的 Docker Compose 脚本管理）。

> 诚实声明：本目录提供的是「可在本地复测」的脚本与模板，**不代表已经跑出某结果**。
> 真实的手工测试执行数据见 `docs/06-reports/`。请勿把本目录的存在解读为"用例已全部通过"。

---

## 目录结构

```
tests/e2e/
├── package.json            # 依赖与脚本
├── playwright.config.js     # baseURL/超时/reporter/trace 配置(不含 webServer)
├── specs/
│   ├── api-smoke.spec.js        # 接口级冒烟 —— 最稳健、真正可跑
│   ├── 01-login.spec.js          # UI 登录(模板, 受验证码影响)
│   ├── 02-browse-labs.spec.js    # 浏览实验室(接口稳健 + UI 模板)
│   └── 03-reservation-flow.spec.js # 预约 happy path(模板) + BUG-001 接口回归
├── .gitignore
└── README.md
```

## 稳健可跑 vs. 需按环境调整(重要)

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `api-smoke.spec.js` | **稳健可跑** | 直接打后端 REST，匿名实验室列表断言最可靠；登录/鉴权用例在验证码开启时**自动跳过**而非伪造通过。 |
| `02-browse-labs.spec.js`(接口部分) | **稳健可跑** | 经匿名接口断言列表加载。 |
| `01-login.spec.js` | **模板** | UI 登录受 RuoYi 图形验证码影响，选择器文案可能需按页面调整。 |
| `02-browse-labs.spec.js`(UI 部分) | **模板** | 前台浏览页路由/选择器需按二开实际补全(含 TODO)。 |
| `03-reservation-flow.spec.js` | **模板** | UI happy path 默认 `test.fixme` 跳过；BUG-001 接口回归需鉴权 + 种子数据，默认条件跳过。 |

---

## 前置条件

1. **启动 SUT**（在仓库根目录）：

   ```bash
   ./start.sh        # Linux/macOS
   # 或  start.ps1 / start.bat   (Windows)
   ```

   该脚本基于 Docker Compose 拉起 MySQL + Redis + 后端 + 前端。
   默认访问地址：前端 <http://localhost:81>，后端 API <http://localhost:8080>，
   登录账号 `admin / admin123`。停止用 `stop.sh` / `stop.ps1` / `stop.bat`。

2. **安装 Node 依赖与浏览器**（在本目录 `tests/e2e/`）：

   ```bash
   npm install
   npm run install:browsers   # 下载 chromium(含系统依赖)
   ```

## 运行

```bash
npm test            # 无头运行全部 specs，输出 list + html 报告
npm run test:ui     # 打开 Playwright UI 模式(交互式)
npm run report      # 查看上次的 HTML 报告
```

只跑最稳健的接口冒烟：

```bash
npx playwright test specs/api-smoke.spec.js
```

UI 用例如需人工处理验证码，建议有头/调试模式运行：

```bash
npx playwright test specs/01-login.spec.js --headed --debug
```

## 环境变量

| 变量 | 默认值 | 用途 |
| --- | --- | --- |
| `E2E_BASE_URL` | `http://localhost:81` | 前端基址(UI 用例) |
| `E2E_API_URL`  | `http://localhost:8080` | 后端基址(接口用例) |
| `E2E_USERNAME` | `admin` | 登录账号 |
| `E2E_PASSWORD` | `admin123` | 登录密码 |
| `E2E_LAB_ID`   | `1` | BUG-001 回归使用的实验室 id(需为环境真实存在) |
| `E2E_BROWSE_PATH` | `/` | 前台浏览页路由(按二开调整) |

示例（Linux/macOS）：

```bash
E2E_BASE_URL=http://localhost:81 E2E_API_URL=http://localhost:8080 npm test
```

Windows PowerShell：

```powershell
$env:E2E_API_URL = 'http://localhost:8080'; npm test
```

## 关于图形验证码

RuoYi 登录默认开启图形验证码（`POST /login` 需要 `{username,password,code,uuid}`，
先 `GET /captchaImage` 拿 `uuid` 与图片）。自动化**无法稳定识别验证码图片**，因此：

- **接口冒烟**：先探测 `captchaImage` 的 `captchaEnabled`。若验证码开启导致纯接口登录失败，
  相关鉴权用例会**显式 `test.skip`** 并打印原因，而**不会伪造通过**。
- **UI 登录**：可在测试环境将验证码关闭（`captchaEnabled=false`）后稳定运行；
  或用 `--headed --debug` 在打开的浏览器里**人工输入验证码**（用例中预留了 `page.pause()` 注释）。
- 最可靠的活性探针是匿名接口 `GET /labs_browse/labs_browse/list`（`@Anonymous`，无需登录）。

## AI 如何辅助本套件（诚实说明）

- 本目录的 spec **脚手架由 AI 辅助生成**：AI 阅读了 SUT 后端控制器源码
  （`ReservationController` 等）以对齐真实路由、`TableDataInfo {total,rows,code,msg}`
  返回结构与 BUG-001/002/003 等校验逻辑，据此产出可运行的模板。
- **AI 是助手，不是裁判**：所有用例必须由人工在本地运行/评审后才能采信结果。
  AI **没有**自主运行过本套件，也**没有**自主"发现"缺陷数量；缺陷与执行数据以
  `docs/06-reports/` 的真实手工测试记录为准。
- AI 探索式测试的思路与产物另见 `tests/ai/`（如存在）。

## 与真实缺陷的对应

`03-reservation-flow.spec.js` 中的 **BUG-001 接口回归**对应真实手工测试结论：
"同实验室同日期同时段可重复预约（缺冲突检测）"。SUT 后端已在
`insertReservation` 中加入冲突检测（源码标注 `BUG-001 [已修复]`）。

> ⚠️ **诚实标注（回归探针而非"必绿守护"）**：经核对 SUT 源码，`insertReservation` 构造的
> `conflictQuery` 未设置 `id`（为 `null`），而 `ReservationMapper.xml#selectConflictingReservations`
> 的 SQL 末尾含 `AND id != #{id}`。MySQL 中 `id != NULL` 恒为 `UNKNOWN`，会过滤掉所有行，
> 导致**冲突检测在 insert 路径可能不命中**，第二次相同预约可能仍返回 `code:200`。
> 因此该用例是**回归探针**：它可能失败，且失败恰好暴露"BUG-001 修复不完整"这一真实情况——
> 应据此向 SUT 反馈（建议 insert 路径使用不含 `id != #{id}` 的冲突查询）。请以本地实际运行结果为准。
