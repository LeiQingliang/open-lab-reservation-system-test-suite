// @ts-check
const { test, expect } = require('@playwright/test');

/**
 * UI 登录流程 —— 模板用例，可能需按环境调整。
 *
 * 重要: RuoYi 登录页带"图形验证码"。自动化无法稳定识别验证码，因此本用例为:
 *   1) 在已"关闭验证码"的测试环境(captchaEnabled=false)下可直接通过; 或
 *   2) 需要人工在 UI 上输入验证码后再继续(headed 模式: `npx playwright test --headed --debug`)。
 *
 * 选择器采用健壮策略(getByPlaceholder / getByRole)，但 RuoYi 模板的占位符文案
 * 可能随版本/汉化不同而变化，标注 TODO 处请按实际页面调整。
 *
 * 账号: admin / admin123  前端: process.env.E2E_BASE_URL (默认 http://localhost:81)
 */

const USERNAME = process.env.E2E_USERNAME || 'admin';
const PASSWORD = process.env.E2E_PASSWORD || 'admin123';

test.describe('UI 登录 (模板, 可能需按环境调整)', () => {
  test('使用 admin 账号登录并进入首页', async ({ page }) => {
    await page.goto('/');

    // 用户名/密码: RuoYi 默认占位符为"账号"/"密码"。
    // TODO: 若页面占位符不同(如英文 "Username"/"Password")，请在此调整。
    const usernameInput = page.getByPlaceholder(/账号|用户名|username/i);
    const passwordInput = page.getByPlaceholder(/密码|password/i);

    await expect(usernameInput, '应能定位到用户名输入框').toBeVisible();
    await usernameInput.fill(USERNAME);
    await passwordInput.fill(PASSWORD);

    // ---- 图形验证码处理 ----
    // RuoYi 登录默认带验证码输入框(占位符通常为"验证码")。
    // 自动化无法识别验证码图片; 处理方式二选一:
    //   A. 测试环境关闭验证码: 该输入框不存在，下面的 if 分支会被跳过。
    //   B. headed/debug 模式下由人工填写: 取消注释 page.pause() 暂停等待手动输入。
    const captchaInput = page.getByPlaceholder(/验证码|captcha|code/i);
    if (await captchaInput.count()) {
      // TODO: 若需人工输入验证码，取消下一行注释，在打开的浏览器中手动填写后点击继续。
      // await page.pause();
      test.info().annotations.push({
        type: 'note',
        description: '检测到验证码输入框: 该环境未关闭图形验证码，纯自动化可能无法通过登录。',
      });
    }

    // 登录按钮: RuoYi 文案为"登 录"(可能含空格)。
    // TODO: 文案不一致时调整正则。
    await page.getByRole('button', { name: /登\s*录|登录|login/i }).click();

    // 断言进入首页: RuoYi 后台首页常见特征为侧边栏 / "首页" 标签。
    // TODO: 若你的首页文案不同，请调整断言目标。
    await expect(page).toHaveURL(/\/index|\/$/, { timeout: 10000 });
    await expect(
      page.getByText(/首页|系统首页|控制台/).first()
    ).toBeVisible({ timeout: 10000 });
  });
});
