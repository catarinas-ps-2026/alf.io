import { expect, test } from "../../fixtures/auth";
import { loginAs } from "../../flows/auth";
import { AdminPage } from "../../pages/admin/AdminPage";

test.describe("Authentication - Session", () => {
    test("authenticated user stays logged after page reload", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(page, adminCredentials, baseURL!);
        const admin = new AdminPage(page);
        await page.reload();
        expect(await admin.isLoggedIn()).toBe(true);
    });

    test("unauthenticated user cannot access admin panel", async ({ page }) => {
        await page.goto("/admin/");
        await expect(page).toHaveURL(/authentication|login/);
    });
});
