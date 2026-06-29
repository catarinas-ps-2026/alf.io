import { defineConfig, devices } from "@playwright/test";

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
    testDir: "./tests",
    timeout: 60000, // 60s timeout for resource-constrained test runs
    fullyParallel: false, // Run tests sequentially to avoid DB conflicts
    forbidOnly: false,
    retries: process.env.CI ? 2 : 0,
    workers: 1, // Single worker to ensure sequential DB test execution
    reporter: [["html", { open: "never" }], ["list"]],
    use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL || "http://localhost:8080",
        actionTimeout: 15000,
        navigationTimeout: 30000,

        trace: "retain-on-failure",
        screenshot: "only-on-failure",
        video: "retain-on-failure",
        locale: "en-US",
    },

    webServer: {
        command: "cd ../../.. && ./gradlew -Pprofile=dev :bootRun",
        url: "http://localhost:8080/admin",
        reuseExistingServer: !process.env.CI,
        timeout: 180000,
        env: {
            ALFIO_OVERRIDE_SYSTEM_SETTINGS_SYSTEM_API_KEY:
                process.env.ALFIO_OVERRIDE_SYSTEM_SETTINGS_SYSTEM_API_KEY ||
                "e2e-test-api-key",
        },
    },

    projects: [
        {
            name: "chromium",
            use: { ...devices["Desktop Chrome"] },
        },
        {
            name: "firefox",
            use: { ...devices["Desktop Firefox"] },
        },
    ],
});
