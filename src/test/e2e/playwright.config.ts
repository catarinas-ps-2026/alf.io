import { defineConfig, devices } from "@playwright/test";

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
    testDir: "./tests",
    timeout: 60000,
    fullyParallel: false,
    forbidOnly: false,
    retries: process.env.CI ? 2 : 0,
    workers: 1,
    reporter: [["html", { open: "never" }], ["list"]],
    use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL || "http://localhost:8080",
        actionTimeout: 15000,
        navigationTimeout: 30000,

        trace: "retain-on-failure",
        screenshot: "only-on-failure",
        video: (process.env.PLAYWRIGHT_VIDEO as "on" | "off" | "retain-on-failure" | "on-first-retry") || "retain-on-failure",
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
