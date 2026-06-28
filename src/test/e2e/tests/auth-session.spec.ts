import { expect, test } from "../fixtures/test-fixtures";
import { AdminPage } from "../pages/admin.page";
import { completeBasicConfigIfVisible } from "../helpers/auth-helper";

test.describe("Authentication: Session Persistence", () => {
    test("should keep user authenticated after page reload", async ({
        authenticatedPage,
        baseURL,
    }) => {
        test.skip(
            !authenticatedPage,
            "Skipping test: No authenticated session available.",
        );
        if (!authenticatedPage) return;

        const adminPage = new AdminPage(authenticatedPage);
        expect(await adminPage.isLoggedIn()).toBe(true);

        // Reload page
        await authenticatedPage.reload();

        // Auto-complete basic configuration if reload showed the dialog again
        await completeBasicConfigIfVisible(
            authenticatedPage,
            baseURL || "http://localhost:8080",
        );

        // User should still be logged in
        expect(await adminPage.isLoggedIn()).toBe(true);
    });

    test("should keep user authenticated when navigating between pages", async ({
        authenticatedPage,
    }) => {
        test.skip(
            !authenticatedPage,
            "Skipping test: No authenticated session available.",
        );
        if (!authenticatedPage) return;

        const adminPage = new AdminPage(authenticatedPage);
        expect(await adminPage.isLoggedIn()).toBe(true);

        // Navigate to edit account page using internal state change
        await adminPage.gotoEditAccount();
        await expect(authenticatedPage).toHaveURL(/.*(profile\/edit).*/);

        // Navigate back to admin dashboard
        await adminPage.goto();
        expect(await adminPage.isLoggedIn()).toBe(true);
    });

    test("authentication-status API should reflect active session state", async ({
        authenticatedPage,
    }) => {
        test.skip(
            !authenticatedPage,
            "Skipping test: No authenticated session available.",
        );
        if (!authenticatedPage) return;

        // Query the authentication status API using the page's request context
        // to share current cookies/session
        const statusResponse = await authenticatedPage.request.get(
            "/authentication-status",
        );
        expect(statusResponse.status()).toBe(200);

        const status = await statusResponse.json();
        expect(status.authenticated).toBe(true);
        expect(status.username).not.toBeNull();
    });
});
