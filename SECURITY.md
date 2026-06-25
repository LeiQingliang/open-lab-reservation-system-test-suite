# 安全策略 Security Policy

## 项目性质说明

本仓库是 **开放实验室预约系统** 的软件测试套件（教学 / 课程设计用途），并在 [`system-under-test/`](system-under-test/) 内附带了被测系统源码，仅用于**本地复现测试环境**。

被测系统基于 [RuoYi](https://gitee.com/y_project/RuoYi-Vue) 二次开发，出于演示便利，包含一批**已知的默认弱口令与示例密钥**，例如：

- 管理员账号 `admin / admin123`
- 数据库 `root / root`、Druid 控制台默认账号
- 示例 JWT 签名密钥

> ⚠️ **这些默认凭据仅供本地演示与测试，切勿用于公网或生产环境。** 任何对外部署前必须更换全部口令与密钥。上述属于框架公开默认值，不视为本仓库的安全漏洞。

## 支持的版本

| 版本 | 是否提供安全更新 |
|------|------------------|
| `main`（最新） | ✅ |
| 历史 tag | ❌（仅作存档） |

## 报告漏洞

如果你在**测试套件本身**或被测系统中发现了**非上述已知默认值**的真实安全问题，请**私下**报告，不要直接公开 Issue：

- 首选：通过 GitHub 的 [**Security Advisories**](https://github.com/LeiQingliang/open-lab-reservation-system-test-suite/security/advisories/new) 私密上报；
- 或发送邮件至 **qinglianglei0912@gmail.com**，标题注明 `[SECURITY]`。

请在报告中尽量包含：

- 受影响的文件 / 接口 / 组件；
- 复现步骤与影响评估；
- 可能的修复建议（如有）。

## 响应预期

作为教学项目，本仓库以尽力而为（best-effort）方式处理安全报告：

- 收到报告后 **7 天内**确认；
- 评估后在合理时间内修复并在 [CHANGELOG.md](CHANGELOG.md) 中说明；
- 在你同意的前提下，于致谢中署名。

感谢你以负责任的方式披露安全问题。🙏
