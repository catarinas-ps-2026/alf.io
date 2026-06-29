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

        await authenticatedPage.reload();

        await completeBasicConfigIfVisible(
            authenticatedPage,
            baseURL || "http://localhost:8080",
        );

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

        await adminPage.gotoEditAccount();
        await expect(authenticatedPage).toHaveURL(/.*(profile\/edit).*/);

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

        const statusResponse = await authenticatedPage.request.get(
            "/authentication-status",
        );
        expect(statusResponse.status()).toBe(200);

        const status = await statusResponse.json();
        expect(status.authenticated).toBe(true);
        expect(status.username).not.toBeNull();
    });
});
