import { expect, test } from "../fixtures/test-fixtures";
import { completeBasicConfigIfVisible } from "../helpers/auth-helper";

test.describe("Authorization: Access Control", () => {
    test("should redirect unauthenticated users trying to access administrative UI to login page", async ({
        page,
    }) => {
        await page.goto("/admin/");

        await expect(page).toHaveURL(/.*(login|authentication).*/);
    });

    test("should reject unauthenticated API requests to administrative endpoints with 401/403 status", async ({
        playwright,
        baseURL,
    }) => {
        const url = `${
            baseURL || "http://localhost:8080"
        }/admin/api/users/current`;
        const requestContext = await playwright.request.newContext();

        const response = await requestContext.get(url);

        expect(
            response.status() >= 400 ||
                response.url().includes("authentication") ||
                response.url().includes("session-expired"),
        ).toBe(true);

        await requestContext.dispose();
    });

    test("should allow admin but deny supervisor from accessing system configuration API", async ({
        page,
        adminCredentials,
        supervisorCredentials,
        baseURL,
    }) => {
        test.skip(
            !adminCredentials || !supervisorCredentials,
            "Skipping test: Requires both admin and supervisor credentials.",
        );
        if (!adminCredentials || !supervisorCredentials) return;

        const base = baseURL || "http://localhost:8080";

        await page.goto("/authentication");
        await page.locator("#username").fill(supervisorCredentials.username);
        await page.locator("#password").fill(supervisorCredentials.password);
        await page.locator('button[type="submit"]').click();
        await page.waitForURL(/.*(admin).*/);

        const supervisorConfigResponse = await page.request.get(
            "/admin/api/system/api-key",
            { headers: { "X-Requested-With": "XMLHttpRequest" } },
        );

        expect(supervisorConfigResponse.status()).toBe(403);

        await page.goto("/admin/");
        await page.locator('.navbar-right a:has-text("Log out")').click();
        await page.waitForURL(/.*(login|authentication).*/);

        await page.locator("#username").fill(adminCredentials.username);
        await page.locator("#password").fill(adminCredentials.password);
        await page.locator('button[type="submit"]').click();
        await page.waitForURL(/.*(admin).*/);
        await completeBasicConfigIfVisible(page, base);

        const adminConfigResponse = await page.request.get(
            "/admin/api/system/api-key",
            { headers: { "X-Requested-With": "XMLHttpRequest" } },
        );

        expect(adminConfigResponse.status()).toBe(200);
    });
});
