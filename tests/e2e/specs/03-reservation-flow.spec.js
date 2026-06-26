// @ts-check
const { test, expect, request } = require('@playwright/test');

/**
 * 预约流程 —— happy path(UI 模板) + BUG-001 冲突检测回归(接口)。
 *
 * 说明:
 * - UI happy path 为"模板": 真实预约页面的选择器/字段未在此固化，含 TODO，
 *   需在运行 SUT 后按页面结构补全; 因此默认 test.fixme 跳过，避免误报。
 * - BUG-001 回归: 真实手工测试发现"同实验室同日期同时段可重复预约"(无冲突检测)。
 *   SUT 后端 ReservationController#insertReservation 现已加入冲突检测(见源码注释 BUG-001 [已修复])。
 *   本用例以接口方式做"回归探针": 期望第二次相同预约被拒绝。
 *
 *   ⚠️ 已知局限(诚实标注): insert 路径构造的 conflictQuery 未设 id(null)，
 *   而 mapper 的 selectConflictingReservations SQL 末尾含 `AND id != #{id}`。
 *   MySQL 中 `id != NULL` 恒为 UNKNOWN，该过滤会排除所有行 → 冲突检测在 insert 路径可能不命中，
 *   第二次预约可能仍返回 code:200。因此本断言**可能失败**——这正是"回归探针"应当暴露的真实情况:
 *   若失败，说明 BUG-001 的修复在 insert 路径并不完整(参见 tests/e2e/README.md 的说明)，应反馈给 SUT。
 *
 *   注意: /reservation/reservation/insert 需要登录鉴权且依赖有效 labId 与种子数据，
 *   纯接口在带验证码环境无法稳定获取 token，故默认条件跳过，仅作为可复测模板保留。
 */

const API_URL = process.env.E2E_API_URL || 'http://localhost:8080';
const USERNAME = process.env.E2E_USERNAME || 'admin';
const PASSWORD = process.env.E2E_PASSWORD || 'admin123';

/** 尽力而为登录，返回 token 或 null(验证码开启时通常为 null)。 */
async function tryLogin(api) {
  const resp = await api.post('/login', {
    data: { username: USERNAME, password: PASSWORD },
    headers: { 'Content-Type': 'application/json' },
  });
  if (!resp.ok()) return null;
  const body = await resp.json();
  return body.token || null;
}

test.describe('预约流程', () => {
  // ---- UI happy path (模板) ----
  test.fixme('UI: 提交一条预约 (happy path, 模板含 TODO 选择器)', async ({ page }) => {
    // 前置: 需先完成登录(见 01-login.spec.js)，此处假设已有会话或在同一上下文登录。
    await page.goto('/');

    // TODO: 进入"我要预约"/预约表单页面的导航。
    // 例: await page.getByRole('link', { name: '实验室预约' }).click();

    // TODO: 选择实验室(下拉/卡片)。
    // 例: await page.getByLabel('实验室').selectOption({ label: '软件实验室A' });

    // TODO: 选择预约日期(未来日期)。
    // 例: await page.getByLabel('预约日期').fill('2026-07-01');

    // TODO: 选择时间段(1-4 合法范围)。
    // 例: await page.getByLabel('时间段').selectOption('1');

    // TODO: 填写预约用途(purpose 不能为空)。
    // 例: await page.getByLabel('用途').fill('课程实验');

    // TODO: 提交并断言成功提示。
    // 例: await page.getByRole('button', { name: '提交' }).click();
    //     await expect(page.getByText('提交成功')).toBeVisible();
    expect(true).toBe(true);
  });

  // ---- BUG-001 接口回归: 重复预约应被冲突检测拒绝 ----
  test('接口回归 BUG-001: 同实验室/日期/时段重复预约应被拒绝', async () => {
    const api = await request.newContext({
      baseURL: API_URL,
      extraHTTPHeaders: { 'Content-Type': 'application/json' },
      ignoreHTTPSErrors: true,
    });

    const token = await tryLogin(api);
    test.skip(
      !token,
      'RuoYi 验证码开启或登录失败，无法获取 token; BUG-001 回归需鉴权 + 种子数据，默认跳过(模板)。'
    );

    const authApi = await request.newContext({
      baseURL: API_URL,
      extraHTTPHeaders: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      ignoreHTTPSErrors: true,
    });

    // TODO: 替换为环境中真实存在的 labId(可先 GET /labs_browse/labs_browse/list 取一个 id)。
    const labId = Number(process.env.E2E_LAB_ID || 1);
    // 取未来日期，规避 BUG-003(过去日期)校验。
    const future = new Date(Date.now() + 7 * 24 * 3600 * 1000);
    const reservationDate = future.toISOString().slice(0, 10); // yyyy-MM-dd

    const payload = {
      labId,
      reservationDate,
      timeSlot: 1, // 合法范围 1-4，规避 BUG-002
      purpose: 'E2E BUG-001 回归', // 非空，规避 BUG-006
    };

    // 第一次预约: 期望成功(code 200)。
    const first = await authApi.post('/reservation/reservation/insert', { data: payload });
    expect(first.status()).toBe(200);
    const firstBody = await first.json();

    // 若首条因种子数据/权限未成功，则不继续断言冲突，避免误判。
    test.skip(
      firstBody.code !== 200,
      `首条预约未成功(code=${firstBody.code}, msg=${firstBody.msg}); 需要有效 labId 与权限，跳过冲突断言。`
    );

    // 第二次相同预约: BUG-001 修复后应被冲突检测拒绝(code !== 200)。
    const second = await authApi.post('/reservation/reservation/insert', { data: payload });
    const secondBody = await second.json();

    // 回归断言: 重复预约必须被拒绝。若此处失败，说明 BUG-001 冲突检测回退。
    expect(
      secondBody.code,
      `BUG-001 回归: 重复预约应被拒绝，但返回 code=${secondBody.code}, msg=${secondBody.msg}`
    ).not.toBe(200);
    expect(secondBody.msg || '').toMatch(/已被预约|冲突|占用/);

    await authApi.dispose();
    await api.dispose();
  });
});
