# 探索式测试 Charter — 鉴权与安全 (Auth & Security)

> 基于会话的探索式测试(SBET)。带着 mission 在时间盒内自由探索。
> 实际结果填入 [`../execution-log-template.md`](../execution-log-template.md)。

## Mission(任务)

探索登录认证、令牌鉴权与权限边界,验证未授权/越权访问被正确拦截,注入类攻击被安全处理,
并重点考察**未授权访问的错误码(401 vs 500)**与**敏感信息泄露**。

## 时间盒

60 分钟(建议)。

## 覆盖区域

- 前端:登录页 `/login`(用户名/密码/验证码)、注册页、退出登录、401/404 页。
- 接口:`POST /login`、`GET /getInfo`、`GET /captchaImage`、`POST /logout`;
  受保护接口(如 `/reservation/reservation/list`)与匿名接口(`/labs_browse/labs_browse/list`)。
- 框架:Spring Security + JWT;XssFilter;操作日志 `sys_oper_log` / 登录日志 `sys_logininfor`。

## 测试点子(取自真实用例)

- 登录:正确凭证返回 token;错误密码;不存在用户;验证码错误/过期(FT-USR-001~005)。
- 令牌:无 token / 伪造随机 token / 过期 token 访问受保护接口(API-01~03、SEC-001/002/014)。
- 匿名接口可访问、匿名接口 POST 应 405(API-04/05)。
- 权限:低权限用户访问管理端点应 403;无按钮权限不显示(SEC-003/004、UI-021/022)。
- 注入:labName=`' OR '1'='1`、id=`1 OR 1=1`、purpose=`<script>`、labName=`<img onerror>`
  (SEC-005~008)。
- 敏感信息:登录/改密后查 `sys_oper_log`,oper_param 是否含明文密码(SEC-009/010)。
- 暴力破解:连续错误登录是否触发重试限制(SEC-012,maxRetryCount=5)。

## 已知缺陷观察点

- **未授权返回 500 而非 401**(报告 API-08 / SEC-01 历史 FAIL):
  不带 token 访问 `/reservation/reservation/list`,观察响应码——预期应为 401,历史为 500。这是反复出现的核心安全短板。
- **BUG-012 操作日志可能泄露敏感参数**(未修复):
  查 `sys_oper_log` 是否记录了含密码等敏感字段的完整请求 JSON(SEC-009/010)。
- SQL 注入历史安全(MyBatis 参数化,SEC-02 PASS);XSS 历史由前端转义(SEC-03 PASS)——做回归确认,而非假设。
- 暴力破解阈值未验证(遗留问题 L-06),可补测。

## 启发式提示

- 登录带图形验证码,接口直测需先 `GET /captchaImage` 拿 uuid+img,再带 code 调 `/login`。
- 401 vs 403 vs 500 要分清:无认证应 401、有认证无权限应 403、服务端异常才 500。
- 注入测试关注"是否返回额外数据/是否破坏数据/是否执行脚本",而非仅看是否报错。
- 直连 MySQL `localhost:3307` 查 `sys_oper_log` / `sys_logininfor` 核对日志内容。

## 会话产出(填入执行日志)

- 覆盖到的功能区:`__________`
- 主要观察 / 发现:`__________`
- 复现的已知缺陷:`__________`
- 新疑点:`__________`
