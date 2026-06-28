import { expect, test } from "../fixtures/test-fixtures";
import { AdminPage } from "../pages/admin.page";
import { getAuthStatus } from "../helpers/auth-helper";

test.describe("Authentication: Logout Flows", () => {
    test("should log out successfully and prevent re-entry", async ({
        authenticatedPage,
        baseURL,
        playwright,
    }) => {
        test.skip(
            !authenticatedPage,
            "Skipping test: No authenticated session available (needs admin credentials).",
        );
        if (!authenticatedPage) return;

        const adminPage = new AdminPage(authenticatedPage);
        expect(await adminPage.isLoggedIn()).toBe(true);

        // Perform logout
        await adminPage.logout();

        // Verify redirection to login/authentication page
        await expect(authenticatedPage).toHaveURL(/.*(login|authentication).*/);

        // Try navigating back to admin page, should be redirected to login page
        await authenticatedPage.goto("/admin/");
        await expect(authenticatedPage).toHaveURL(/.*(login|authentication).*/);

        // Verify via API that session is truly unauthenticated
        const requestContext = await playwright.request.newContext();
        const status = await getAuthStatus(
            requestContext,
            baseURL || "http://localhost:8080",
        );
        expect(status.authenticated).toBe(false);
        await requestContext.dispose();
    });
});
