# 单元测试 / 接口测试（自动化 · 已纳入 CI）

> 测试金字塔的**底座**：纯 JUnit 5 + Mockito + MockMvc(standaloneSetup) 自动化测试，
> 不依赖数据库 / Redis / Spring 上下文，可离线稳定运行，并在 GitHub Actions 的 **JDK 8 / 21** 双矩阵上持续验证。

## 📍 测试代码位置

这些测试**与被测代码同处一个 Maven 模块**，因此按 Maven 标准布局位于被测系统目录内（不能移出模块，否则无法编译）：

```
system-under-test/back/labs/labs-management/src/test/java/com/labs/
├── apply/controller/LabScheduleControllerApiTest.java      接口测试 · 排期 Controller
├── apply/service/LabScheduleServiceImplTest.java           单元测试 · 排期 Service
├── labs_browse/controller/LabsControllerApiTest.java       接口测试 · 实验室 Controller
├── labs_browse/service/LabsServiceImplTest.java            单元测试 · 实验室 Service
├── reservation/controller/ReservationControllerTest.java   接口测试 · 预约 Controller
├── reservation/service/ReservationServiceImplTest.java     单元测试 · 预约 Service
└── system/service/SysPostServiceImplTest.java              单元测试 · 岗位 Service（上游模块）
```

## 🧪 测试基线

| 指标 | 数值 |
|------|------|
| 测试类 | 7 |
| 测试方法（`@Test`） | **62**（全部通过） |
| 行覆盖率（labs-management） | ~74% |
| 分支覆盖率（labs-management） | ~85% |
| 验证环境 | JDK 8、JDK 21（CI 矩阵） |

> 覆盖率为**自研业务模块 labs-management** 的 JaCoCo 统计，不含 vendored RuoYi 框架代码。

## ▶️ 在本地运行

后端工程位于 `system-under-test/back/labs`（其上方无聚合 pom，必须在此目录构建），内置 Maven Wrapper，无需预装 Maven：

```bash
cd system-under-test/back/labs

# Linux / macOS / Git Bash
./mvnw -B -pl labs-management -am clean test

# Windows PowerShell
.\mvnw.cmd -B -pl labs-management -am clean test
```

- `-am`（**务必保留**）：一并编译上游模块 `labs-framework` / `labs-system` / `labs-common`，否则 `SysPostServiceImplTest` 编译失败。
- ⚠️ **JDK 版本**：编译目标为 Java 8，需 **JDK 8 或 21**；**不支持 JDK 25+**（已移除 `-source/-target 8`）。
- 覆盖率报告：测试结束后见 `labs-management/target/site/jacoco/index.html`。
- 测试报告：`labs-management/target/surefire-reports/`。

## 🎯 测试策略

- **单元测试（Service 层）**：用 Mockito 隔离 Mapper（数据访问层），只验证业务层的方法调用、参数传递与返回值处理逻辑。
- **接口测试（Controller 层）**：用 MockMvc 的 `standaloneSetup` 单独装载 Controller，绕过完整 Spring 容器与安全过滤链，对 HTTP 行为做断言。
- 每个 `@Test` 用 `@DisplayName` 标注可读用例编号（如 `UT-RSV-S-01`），便于与 [测试用例总表](../../docs/03-test-design/测试用例总表.md) 追溯对应。

## 🔗 相关

- 设计说明：[docs/04-test-implementation/05_单元测试代码.md](../../docs/04-test-implementation/05_单元测试代码.md)
- 白盒路径分析：[docs/03-test-design/06_白盒路径分析.md](../../docs/03-test-design/06_白盒路径分析.md)
- 这些测试**如何借助 AI 生成并经人工评审**：[tests/ai/](../ai/)
- CI 工作流：[.github/workflows/ci.yml](../../.github/workflows/ci.yml)
