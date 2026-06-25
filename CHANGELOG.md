# 更新日志 Changelog

本项目的所有重要变更都将记录在本文件中。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

—

## [1.0.0] - 2026-06-25

首个正式发布版本：在已有测试工程交付物的基础上，完成面向 GitHub 开源标准的工程化与规范化。

### Added 新增

- **持续集成**：新增 GitHub Actions 工作流（`.github/workflows/ci.yml`），在 **JDK 8 与 21** 矩阵上自动运行 `labs-management` 模块的 62 个 JUnit 5 单元/接口测试，发布测试报告。
- **代码覆盖率**：为 `labs-management` 接入 JaCoCo（行/分支/指令覆盖率），CI 产出覆盖率摘要与报告制品。
- **Maven Wrapper**：新增 `mvnw` / `mvnw.cmd`，支持零依赖构建（Maven 3.9.9）。
- **社区健康文件**：`CONTRIBUTING.md`、`CODE_OF_CONDUCT.md`（Contributor Covenant 2.1）、`SECURITY.md`、`SUPPORT.md`、`CITATION.cff`、本 `CHANGELOG.md`。
- **协作模板**：`.github/` 下新增 Issue 模板（Bug / 功能建议 / config）、Pull Request 模板、`CODEOWNERS`、Dependabot 配置。
- **README**：补充 CI/覆盖率/测试数量等动态与真实徽章、页内目录、运行测试小节、端口对照表、内嵌系统架构（Mermaid）图、默认口令安全提示与 JDK 版本澄清。

### Changed 变更

- `labs-management` 的 Surefire 配置移除 `testFailureIgnore=true`，恢复「测试失败即构建失败」的默认行为，使 CI 成为可信的质量门禁。
- `.gitignore` 增加 `.env` 系列忽略规则；将已提交的 `system-under-test/.env` 模板化为 `.env.example`。

### Removed 移除

- 删除误提交的废弃文件 `system-under-test/front/RuoYi-Vue3/src/views/tool/build/DraggableItem copy.vue`（无任何引用的死文件）。

### 既有基线（v1.0.0 之前已具备）

- 覆盖软件测试全生命周期的工程文档（`docs/`：分析 → 计划 → 设计 → 实现 → 缺陷 → 报告）及正式交付物（`.docx` + 答辩大纲）。
- 62 个基于 JUnit 5 + Mockito + MockMvc 的单元/接口测试。
- 被测系统（RuoYi 前后端分离）完整源码与 Docker Compose 一键启动。

[Unreleased]: https://github.com/LeiQingliang/open-lab-reservation-system-test-suite/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/LeiQingliang/open-lab-reservation-system-test-suite/releases/tag/v1.0.0
