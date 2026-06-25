// @ts-check
const { test, expect, request } = require('@playwright/test');

/**
 * 接口级冒烟测试 —— 本套件中"最稳健、真正可跑"的部分。
 *
 * 直接打后端 REST API(默认 http://localhost:8080)，不依赖浏览器渲染，
 * 也尽量绕开图形验证码对自动化的干扰(见下方登录说明)。
 *
 * 前置: 仓库根目录执行 start.sh / start.ps1 / start.bat 已把 SUT 拉起。
 *
 * 环境变量:
 *   E2E_API_URL    后端基址，默认 http://localhost:8080
 *   E2E_USERNAME   登录账号，默认 admin
 *   E2E_PASSWORD   登录密码，默认 admin123
 */

const API_URL = process.env.E2E_API_URL || 'http://localhost:8080';
const USERNAME = process.env.E2E_USERNAME || 'admin';
const PASSWORD = process.env.E2E_PASSWORD || 'admin123';

/** 构造一个指向后端的 APIRequestContext。 */
async function newApiContext() {
  return await request.newContext({
    baseURL: API_URL,
    extraHTTPHeaders: { 'Content-Type': 'application/json' },
    ignoreHTTPSErrors: true,
  });
}

/**
 * 尝试登录获取 token。
 *
 * RuoYi 登录默认开启图形验证码: POST /login 需要 {username,password,code,uuid}。
 * 若环境关闭了验证码(captchaEnabled=false)，仅传 {username,password} 即可拿到 token。
 * 本函数做"尽力而为"登录: 成功返回 token 字符串，失败返回 null(由调用方决定跳过)。
 */
async function tryLogin(api) {
  // 先探测验证码开关，便于在断言失败时给出清晰提示。
  const captchaResp = await api.get('/captchaImage');
  let captchaEnabled = true;
  if (captchaResp.ok()) {
    const body = await captchaResp.json();
    // RuoYi: {code, captchaEnabled, uuid, img}
    captchaEnabled = body.captchaEnabled !== false;
  }

  const loginResp = await api.post('/login', {
    data: { username: USERNAME, password: PASSWORD },
  });

  if (!loginResp.ok()) {
    return { token: null, captchaEnabled };
  }
  const loginBody = await loginResp.json();
  // 验证码开启时 code 通常为 500 且 msg 提示验证码错误/为空。
  return { token: loginBody.token || null, captchaEnabled, body: loginBody };
}

test.describe('API 冒烟 (后端 REST)', () => {
  /** @type {import('@playwright/test').APIRequestContext} */
  let api;

  test.beforeAll(async () => {
    api = await newApiContext();
  });

  test.afterAll(async () => {
    await api.dispose();
  });

  test('匿名接口: 浏览实验室列表返回 200 且含 rows/total', async () => {
    // GET /labs_browse/labs_browse/list 标注 @Anonymous，无需登录 —— 最可靠的活性探针。
    const resp = await api.get('/labs_browse/labs_browse/list');
    expect(resp.status(), '匿名实验室列表应返回 200').toBe(200);

    const body = await resp.json();
    // RuoYi TableDataInfo 结构: {total, rows, code, msg}
    expect(body).toHaveProperty('code', 200);
    expect(body).toHaveProperty('rows');
    expect(Array.isArray(body.rows), 'rows 应为数组').toBe(true);
    expect(body).toHaveProperty('total');
    expect(typeof body.total).toBe('number');
  });

  test('登录获取 token (验证码关闭时稳健)', async () => {
    const { token, captchaEnabled } = await tryLogin(api);

    if (!token) {
      // 不伪造结果: 验证码开启导致无法纯接口登录时，明确跳过并说明原因。
      test.skip(
        captchaEnabled,
        'RuoYi 图形验证码已开启，纯接口登录需要 code/uuid。请在测试环境关闭验证码(captchaEnabled=false)后重跑，或改用 UI 用例。'
      );
    }
    expect(token, '应返回非空 token').toBeTruthy();
  });

  test('带 token 查询预约记录列表 (需要鉴权)', async () => {
    const { token, captchaEnabled } = await tryLogin(api);
    test.skip(!token, captchaEnabled
      ? '验证码开启，无法获取 token，跳过鉴权接口冒烟。'
      : '未能获取 token，跳过鉴权接口冒烟。');

    const authApi = await request.newContext({
      baseURL: API_URL,
      extraHTTPHeaders: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      ignoreHTTPSErrors: true,
    });

    // GET /reservation/reservation/list 受 @PreAuthorize 保护。
    const resp = await authApi.get('/reservation/reservation/list');
    expect(resp.status(), '带 token 的预约列表应返回 200').toBe(200);

    const body = await resp.json();
    expect(body).toHaveProperty('code', 200);
    expect(body).toHaveProperty('rows');
    expect(Array.isArray(body.rows)).toBe(true);

    await authApi.dispose();
  });
});
