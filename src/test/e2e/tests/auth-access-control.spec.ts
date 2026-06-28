import { expect, test } from "../fixtures/test-fixtures";
import { completeBasicConfigIfVisible } from "../helpers/auth-helper";

test.describe("Authorization: Access Control", () => {
    test("should redirect unauthenticated users trying to access administrative UI to login page", async ({
        page,
    }) => {
        // Go to admin dashboard unauthenticated
        await page.goto("/admin/");

        // Should be redirected to the login/authentication page
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
        // Spring Security returns 401 (Unauthorized) or redirects to login/session-expired (giving 3xx or 403)
        // for Ajax/XMLHttpRequest requests. If not marked with X-Requested-With, it might redirect.
        // Let's assert it is not successful (status >= 400 or redirected to authentication/session-expired)
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

        // Log in as supervisor
        await page.goto("/authentication");
        await page.locator("#username").fill(supervisorCredentials.username);
        await page.locator("#password").fill(supervisorCredentials.password);
        await page.locator('button[type="submit"]').click();
        await page.waitForURL(/.*(admin).*/);

        // Access a configuration API endpoint directly via page.request
        // (inherits session cookies from the page)
        const supervisorConfigResponse = await page.request.get(
            "/admin/api/configuration/system",
        );
        // Should be forbidden (403) or rejected
        expect(supervisorConfigResponse.status()).toBe(403);

        // Logout
        await page.goto("/admin/");
        await page.locator('.navbar-right a:has-text("Log out")').click();
        await page.waitForURL(/.*(login|authentication).*/);

        // Log in as admin
        await page.locator("#username").fill(adminCredentials.username);
        await page.locator("#password").fill(adminCredentials.password);
        await page.locator('button[type="submit"]').click();
        await page.waitForURL(/.*(admin).*/);
        await completeBasicConfigIfVisible(page, base);

        // Access configuration API endpoint again
        const adminConfigResponse = await page.request.get(
            "/admin/api/configuration/system",
        );
        // Admin should succeed (200)
        expect(adminConfigResponse.status()).toBe(200);
    });
});
