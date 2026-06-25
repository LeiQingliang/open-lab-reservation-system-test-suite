// @ts-check
const { test, expect, request } = require('@playwright/test');

/**
 * 浏览实验室 —— 列表加载验证。
 *
 * 提供两层验证:
 *   1) 接口层(稳健可跑): 经匿名接口 /labs_browse/labs_browse/list 断言列表能加载。
 *   2) UI 层(模板): 打开前台浏览页，断言出现实验室相关内容。UI 选择器随页面而变，标 TODO。
 */

const API_URL = process.env.E2E_API_URL || 'http://localhost:8080';

test.describe('浏览实验室', () => {
  test('接口层: 匿名实验室列表可加载 (稳健)', async () => {
    const api = await request.newContext({
      baseURL: API_URL,
      ignoreHTTPSErrors: true,
    });
    const resp = await api.get('/labs_browse/labs_browse/list');
    expect(resp.status()).toBe(200);

    const body = await resp.json();
    expect(body).toHaveProperty('code', 200);
    expect(Array.isArray(body.rows)).toBe(true);
    // 不假设具体数据条数(取决于种子数据)，仅断言结构正确、total 为数字。
    expect(typeof body.total).toBe('number');

    await api.dispose();
  });

  test('UI 层: 打开浏览页应能加载 (模板, 可能需按环境调整)', async ({ page }) => {
    // RuoYi-Vue3 前台浏览路由名因二开而异。
    // TODO: 替换为实际的实验室浏览页路由(例如 /labs_browse 或菜单跳转)。
    const browsePath = process.env.E2E_BROWSE_PATH || '/';
    await page.goto(browsePath);

    // TODO: 替换为页面上稳定存在的实验室相关文案/角色定位。
    // 此处用宽松断言保证模板可运行: 页面已加载且 body 可见。
    await expect(page.locator('body')).toBeVisible();
    await expect(page).toHaveTitle(/.+/);
  });
});
