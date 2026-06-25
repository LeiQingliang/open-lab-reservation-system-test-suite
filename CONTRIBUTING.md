# 贡献指南

感谢你对 **开放实验室预约系统 · 软件测试套件** 的关注！本仓库是一份软件测试工程交付物（课程设计 T29），同时也欢迎以开源方式参与改进：补充测试用例、完善文档、修复缺陷或提升工程化水平。

> 参与本项目即表示你同意遵守本仓库的 [行为准则](CODE_OF_CONDUCT.md)。

---

## 目录

- [可以贡献什么](#可以贡献什么)
- [开发环境](#开发环境)
- [本地运行测试](#本地运行测试)
- [分支与提交规范](#分支与提交规范)
- [Pull Request 流程](#pull-request-流程)
- [代码与文档约定](#代码与文档约定)

---

## 可以贡献什么

- 🧪 **补充/完善测试**：为 `labs-management` 模块新增单元测试、接口测试、并发或异常分支测试。
- 🐛 **缺陷修复**：修复被测系统已记录的缺陷（见 [`docs/05-defects/`](docs/05-defects/)）并补充对应回归测试。
- 📚 **文档改进**：完善测试文档、补充图表、修正笔误与链接。
- 🛠️ **工程化**：改进 CI、覆盖率、构建脚本、Docker 编排等。

不确定从何入手？欢迎先开一个 [Issue](https://github.com/LeiQingliang/open-lab-reservation-system-test-suite/issues) 讨论。

---

## 开发环境

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | **8**（推荐）或 21 | 编译目标为 Java 8；测试在 JDK 8 与 21 均验证通过。**JDK 25+ 不受支持**（已移除 `-source/-target 8`） |
| Maven | 3.9+ | 仓库已内置 Maven Wrapper（`./mvnw`），无需本机预装 |
| Docker Desktop | 任意较新版本 | 仅在需要完整拉起被测系统（MySQL+Redis+前后端）时使用 |

> 被测系统源码位于 [`system-under-test/`](system-under-test/)；其中后端 Maven 多模块工程在 `system-under-test/back/labs`，**其上方没有聚合 pom**，构建命令需在该目录执行。

---

## 本地运行测试

测试为纯 **JUnit 5 + Mockito + MockMvc（standaloneSetup）**，不依赖数据库/Redis/Spring 上下文，可离线运行：

```bash
cd system-under-test/back/labs

# 使用内置 Maven Wrapper（推荐）
./mvnw -B -pl labs-management -am clean test          # Linux / macOS
.\mvnw.cmd -B -pl labs-management -am clean test       # Windows PowerShell

# 或使用本机 Maven
mvn -B -pl labs-management -am clean test
```

- `-pl labs-management`：仅构建测试所在模块。
- `-am`（**必须保留**）：一并编译其依赖的上游模块（`labs-framework`/`labs-system`/`labs-common`），否则 `SysPostServiceImplTest` 编译失败。
- 覆盖率报告（JaCoCo）：测试结束后见 `labs-management/target/site/jacoco/index.html`。

提交前请确保 **全部测试通过**（当前基线：62 个测试全绿）。

---

## 分支与提交规范

- 请基于 `main` 创建特性分支，命名建议：`feat/xxx`、`fix/xxx`、`docs/xxx`、`test/xxx`、`ci/xxx`。
- 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)：

  ```text
  <type>(<scope>): <简明描述>

  常用 type：feat | fix | docs | test | refactor | ci | chore
  示例：
    test(reservation): 补充预约时段冲突的并发测试
    fix(front): 补充 dayjs 依赖并配置 pnpm allowBuilds
    ci: 新增 GitHub Actions 在 JDK 8/21 上运行测试套件
  ```

---

## Pull Request 流程

1. Fork 并创建特性分支。
2. 完成改动，确保本地 `mvn -pl labs-management -am test` 全绿。
3. 如涉及行为变更，**同步补充/更新测试**。
4. 按 [PR 模板](.github/PULL_REQUEST_TEMPLATE.md) 填写说明，关联相关 Issue。
5. 等待 CI（`Test (JDK 8/21)`）通过后请求评审。

PR 应保持聚焦、单一目的；大型改动请先开 Issue 对齐方案。

---

## 代码与文档约定

- **编码格式**：遵循根目录 [`.editorconfig`](.editorconfig)（UTF-8、LF、缩进规则）与 [`.gitattributes`](.gitattributes)（行尾规范化）。
- **测试命名**：测试方法使用唯一编号的中文 `@DisplayName`（如 `UT-SCH-01`、`API-RSV-03`），类级 `@DisplayName` 按业务模块（M1~M5）组织。
- **文档结构**：测试工程文档置于 [`docs/`](docs/)，按软件测试生命周期编号组织（`01-analysis` → `06-reports`），子目录前缀编号即推荐阅读顺序。
- **被测系统（`system-under-test/`）** 基于 [RuoYi](https://gitee.com/y_project/RuoYi-Vue) 二次开发，属第三方 vendored 代码；除自研业务模块（`labs-management` 及对应前端视图）外，请勿改动框架代码，以保持可复现性。

感谢你的贡献！🎉
