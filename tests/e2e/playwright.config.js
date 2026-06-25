// @ts-check
const { defineConfig, devices } = require('@playwright/test');

/**
 * Playwright 配置 —— 开放实验室网上预约管理系统(SUT) E2E / 接口冒烟。
 *
 * 设计要点:
 * - baseURL 指向运行中的前端(默认 http://localhost:81)，由 Docker Compose(start.sh) 在外部拉起。
 * - 故意不配置 webServer: 应用生命周期由仓库根目录的 start.* / stop.* 管理，
 *   测试只负责"对准已运行的 SUT"，避免 Playwright 重复启动服务。
 * - 后端 API 基址通过环境变量 E2E_API_URL 传给接口用例(见 specs/api-smoke.spec.js)。
 *
 * 环境变量:
 *   E2E_BASE_URL  前端地址，默认 http://localhost:81
 *   E2E_API_URL   后端地址，默认 http://localhost:8080 (在用例内读取)
 *   CI            存在时启用更严格的重试与禁止 test.only
 */
const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:81';

module.exports = defineConfig({
  testDir: './specs',
  // 单个用例最长 30s; 断言默认 5s。SUT 为本地 Docker，留出冷启动余量。
  timeout: 30 * 1000,
  expect: {
    timeout: 5 * 1000,
  },
  // CI 上禁止误提交 test.only。
  forbidOnly: !!process.env.CI,
  // CI 上失败重试 1 次(便于产出 trace)，本地不重试。
  retries: process.env.CI ? 1 : 0,
  // 本地并行，CI 单 worker 保证结果稳定。
  workers: process.env.CI ? 1 : undefined,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: BASE_URL,
    // 仅失败时截图，便于排查 UI 用例。
    screenshot: 'only-on-failure',
    // 首次重试时记录 trace，可用 `npx playwright show-trace` 回放。
    trace: 'on-first-retry',
    actionTimeout: 10 * 1000,
    navigationTimeout: 15 * 1000,
    ignoreHTTPSErrors: true,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
