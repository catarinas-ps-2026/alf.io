import type { Page } from "@playwright/test";
import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { type Credentials, getCurrentUser } from "../../../helpers/auth-helper";
import { LoginPage } from "../../../pages/common/LoginPage";

async function expectSuccessfulLogin(
    page: Page,
    credentials: Credentials,
    baseURL: string,
) {
    await loginAs(page, credentials, baseURL);
    await expect(page).toHaveURL(/admin/);
    const user = await getCurrentUser(page);
    expect(user?.authenticated).toBe(true);
    expect(user?.username).toBe(credentials.username);
}

test.describe("Authentication - Login", () => {
    test("admin can login successfully", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await expectSuccessfulLogin(page, adminCredentials, baseURL!);
    });

    test("organization owner can login successfully", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        await expectSuccessfulLogin(page, ownerCredentials, baseURL!);
    });

    test("check-in supervisor can login successfully", async ({
        page,
        supervisorCredentials,
        baseURL,
    }) => {
        await expectSuccessfulLogin(page, supervisorCredentials, baseURL!);
    });

    test("login fails with invalid credentials", async ({ page }) => {
        const login = new LoginPage(page);
        await login.goto();
        await login.login("fake-user", "wrong-password");
        expect(await login.isErrorVisible()).toBe(true);
        await expect(page).toHaveURL(/authentication|login/);
    });
});
