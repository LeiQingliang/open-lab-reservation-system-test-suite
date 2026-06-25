# 开放实验室预约系统 · 软件测试套件

> Open Lab Reservation System — Software Test Suite

![License](https://img.shields.io/badge/license-MIT-green.svg)
![Type](https://img.shields.io/badge/type-software--testing-blue.svg)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%202.5-6DB33F.svg)
![Frontend](https://img.shields.io/badge/frontend-Vue%203-42b883.svg)
![Based on](https://img.shields.io/badge/based%20on-RuoYi%203.8.9-orange.svg)

本仓库是 **「开放实验室网上预约管理系统」** 的完整软件测试工程交付物,涵盖从需求分析、测试计划、用例设计、单元/性能测试实现,到缺陷跟踪与测试报告的全过程文档,并内附被测系统源码以便复现测试环境。

---

## 📁 目录结构

```text
open-lab-reservation-system-test-suite/
├── docs/                        测试工程文档(按软件测试生命周期组织)
│   ├── 01-analysis/             系统与需求分析
│   ├── 02-test-plan/            测试需求与测试计划
│   ├── 03-test-design/          测试设计(用例 / 白盒路径)
│   ├── 04-test-implementation/  测试实现(单元测试代码 / 性能脚本)
│   ├── 05-defects/              缺陷分析与缺陷清单
│   ├── 06-reports/              测试结果汇总
│   ├── diagrams/                图表源码(Mermaid)
│   ├── guides/                  操作指南
│   ├── reference/               参考资料
│   └── deliverables/            正式交付物(Word 文档 + 答辩大纲)
├── system-under-test/           被测系统源码(RuoYi 前后端分离)
│   ├── back/labs/               Spring Boot 后端(Maven 多模块)
│   └── front/RuoYi-Vue3/        Vue 3 前端
├── LICENSE                      MIT 许可证
├── .editorconfig                编辑器格式约定
├── .gitattributes               行尾与二进制规范化
└── .gitignore                   忽略规则
```

---

## 🧭 文档导航

测试过程按生命周期阶段组织,`docs/` 下子目录前缀编号即为推荐阅读顺序。

| 阶段 | 文档 | 说明 |
|------|------|------|
| ① 分析 | [01_系统上下文分析.md](docs/01-analysis/01_系统上下文分析.md) | 系统上下文与边界分析 |
| ① 分析 | [02_功能模块与数据库分析.md](docs/01-analysis/02_功能模块与数据库分析.md) | 功能模块划分与数据库结构分析 |
| ① 分析 | [项目全面分析.md](docs/01-analysis/项目全面分析.md) | 项目整体分析概览 |
| ② 计划 | [03_测试需求与测试计划.md](docs/02-test-plan/03_测试需求与测试计划.md) | 测试需求梳理与总体测试计划 |
| ③ 设计 | [04_测试用例设计.md](docs/03-test-design/04_测试用例设计.md) | 测试用例设计 |
| ③ 设计 | [测试用例总表.md](docs/03-test-design/测试用例总表.md) | 全量测试用例汇总表 |
| ③ 设计 | [06_白盒路径分析.md](docs/03-test-design/06_白盒路径分析.md) | 白盒测试路径分析 |
| ③ 设计 | [书写测试用例举例.md](docs/03-test-design/书写测试用例举例.md) | 测试用例书写示例 |
| ④ 实现 | [05_单元测试代码.md](docs/04-test-implementation/05_单元测试代码.md) | 单元测试代码说明 |
| ④ 实现 | [08_性能测试脚本.md](docs/04-test-implementation/08_性能测试脚本.md) | 性能测试脚本 |
| ⑤ 缺陷 | [07_缺陷分析与修复.md](docs/05-defects/07_缺陷分析与修复.md) | 缺陷分析与修复记录 |
| ⑤ 缺陷 | [T29_缺陷清单.md](docs/05-defects/T29_缺陷清单.md) | 缺陷清单 |
| ⑥ 报告 | [09_测试结果汇总.md](docs/06-reports/09_测试结果汇总.md) | 测试结果汇总 |
| 图表 | [T29_图表源码_mermaid.md](docs/diagrams/T29_图表源码_mermaid.md) | 报告所用图表的 Mermaid 源码 |
| 指南 | [如何启动项目.md](docs/guides/如何启动项目.md) | 被测系统完整启动与开发指南 |
| 参考 | [单元测试共通点检查表](docs/reference/管理信息系统单元测试共通点检查表_图片内容提取.txt) | 单元测试共通点检查表(图片提取) |

### 📦 正式交付物(`docs/deliverables/`)

| 交付物 | 文件 |
|--------|------|
| 测试计划 | `T29_测试计划.docx` |
| 测试设计 | `T29_测试设计.docx` |
| 测试报告 | `T29_测试报告.docx` |
| 测试跟踪日志 | `T29_测试跟踪日志.docx` |
| 课程设计说明书 | `T29_课程设计说明书.docx` |
| 答辩大纲 | [T29_答辩大纲.md](docs/deliverables/T29_答辩大纲.md) |

---

## 🧪 被测系统(System Under Test)

被测系统是基于 **若依(RuoYi)v3.8.9** 前后端分离框架二次开发的「开放实验室网上预约管理系统」,源码完整存放于 [`system-under-test/`](system-under-test/),便于复现测试环境。自写业务集中在后端 `labs-management` 模块(实验室浏览、排课发布、预约)及对应前端视图。

### 🚀 一键启动（推荐）

只需安装 **Docker Desktop**，在仓库根目录运行 `start.bat` / `./start.ps1`（Windows）或 `./start.sh`（Linux/macOS），
即可自动构建并拉起 MySQL + Redis + 后端 + 前端四个容器，无需本机安装 JDK/Maven/Node/MySQL/Redis。

启动后访问 **http://localhost:81** ，使用 `admin / admin123` 登录。停止用 `stop.bat` / `./stop.sh`。

完整环境搭建与启动步骤（含 Docker 一键启动与传统手动方式）见 **[如何启动项目.md](docs/guides/如何启动项目.md)**。

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端 | Spring Boot + MyBatis + Maven | 2.5.15 |
| 前端 | Vue 3 + Element Plus + Vite | 3.4 / 2.4 / 5.0 |
| 数据库 | MySQL | 8.0(兼容 5.7) |
| 缓存 | Redis | 6.x+ |
| 安全 | Spring Security + JWT | 5.7 / jjwt 0.9 |
| 连接池 | Druid | 1.2.23 |
| JDK | OpenJDK / Amazon Corretto | 21(编译目标 1.8) |

> 数据库初始化脚本位于 `system-under-test/back/labs/sql/`。

---

## 📜 许可证

本项目采用 [MIT](LICENSE) 许可证,版权所有 © 2026 QINGLIANG LEI(雷清亮)。

被测系统基于 [RuoYi](https://gitee.com/y_project/RuoYi-Vue) 二次开发,其框架部分版权归原作者所有。
