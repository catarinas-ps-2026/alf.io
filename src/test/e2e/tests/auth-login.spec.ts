import { expect, test } from "../fixtures/test-fixtures";
import { LoginPage } from "../pages/login.page";
import { AdminPage } from "../pages/admin.page";
import { completeBasicConfigIfVisible } from "../helpers/auth-helper";

test.describe("Authentication: Login Flows", () => {
    test("should log in successfully with admin credentials", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        test.skip(
            !adminCredentials,
            "Skipping test: No E2E_ADMIN_USERNAME or E2E_ADMIN_PASSWORD set.",
        );
        if (!adminCredentials) return;

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(
            adminCredentials.username,
            adminCredentials.password,
        );

        const adminPage = new AdminPage(page);

        await completeBasicConfigIfVisible(
            page,
            baseURL || "http://localhost:8080",
        );
        expect(await adminPage.isLoggedIn()).toBe(true);
    });

    test("should log in successfully with owner credentials", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        test.skip(
            !ownerCredentials,
            "Skipping test: No E2E_OWNER_USERNAME or E2E_OWNER_PASSWORD set.",
        );
        if (!ownerCredentials) return;

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(
            ownerCredentials.username,
            ownerCredentials.password,
        );

        const adminPage = new AdminPage(page);
        await completeBasicConfigIfVisible(
            page,
            baseURL || "http://localhost:8080",
        );
        expect(await adminPage.isLoggedIn()).toBe(true);
    });

    test("should log in successfully with supervisor credentials", async ({
        page,
        supervisorCredentials,
        baseURL,
    }) => {
        test.skip(
            !supervisorCredentials,
            "Skipping test: No E2E_SUPERVISOR_USERNAME or E2E_SUPERVISOR_PASSWORD set.",
        );
        if (!supervisorCredentials) return;

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(
            supervisorCredentials.username,
            supervisorCredentials.password,
        );

        const adminPage = new AdminPage(page);
        await completeBasicConfigIfVisible(
            page,
            baseURL || "http://localhost:8080",
        );
        expect(await adminPage.isLoggedIn()).toBe(true);
    });

    test("should show error on login failure with incorrect credentials", async ({
        page,
    }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login("non_existent_user", "invalid_password");

        await expect(page).toHaveURL(/.*(failed|login|authentication).*/);
        expect(await loginPage.isErrorVisible()).toBe(true);

        const errMsg = await loginPage.getErrorMessage();
        expect(errMsg).toContain("wrong password");
    });

    test("should clear fields when reset button is clicked", async ({
        page,
    }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();

        await page.locator("#username").fill("tempuser");
        await page.locator("#password").fill("temppass");

        await loginPage.clickCancel();

        expect(await loginPage.getUsernameValue()).toBe("");
        expect(await loginPage.getPasswordValue()).toBe("");
    });

    test("should redirect to admin page if already authenticated", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        test.skip(
            !adminCredentials,
            "Skipping test: No E2E_ADMIN_USERNAME or E2E_ADMIN_PASSWORD set.",
        );
        if (!adminCredentials) return;

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(
            adminCredentials.username,
            adminCredentials.password,
        );

        const adminPage = new AdminPage(page);
        await completeBasicConfigIfVisible(
            page,
            baseURL || "http://localhost:8080",
        );
        expect(await adminPage.isLoggedIn()).toBe(true);

        await loginPage.goto();

        await expect(page).toHaveURL(/.*(admin).*/);
    });
});
