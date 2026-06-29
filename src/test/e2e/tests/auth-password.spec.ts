import { expect, test } from "../fixtures/test-fixtures";
import { getCurrentUserId, resetUserPassword } from "../helpers/auth-helper";
import { AdminPage } from "../pages/admin.page";

test.describe("Authentication: Password Management", () => {
    test("should successfully change password and then revert it", async ({
        authenticatedPage,
        adminCredentials,
    }) => {
        test.skip(
            !authenticatedPage || !adminCredentials,
            "Skipping test: No authenticated session or credentials available.",
        );
        if (!authenticatedPage || !adminCredentials) return;

        const adminPage = new AdminPage(authenticatedPage);
        await adminPage.goto();

        await adminPage.gotoEditAccount();

        const oldPasswordInput = authenticatedPage.locator("#oldPassword");
        await oldPasswordInput.waitFor({ state: "visible", timeout: 15000 });

        const oldPassword = adminCredentials.password;
        const newPassword = "NewP@ssw0rd123!";

        let adminUserId: number | null = null;
        try {
            adminUserId = await getCurrentUserId(authenticatedPage);
        } catch (err) {
            console.error("Failed to get admin user ID:", err);
        }

        try {
            await oldPasswordInput.fill(oldPassword);
            await authenticatedPage.fill("#newPassword", newPassword);
            await authenticatedPage.fill("#newPasswordConfirm", newPassword);

            const updateBtn = authenticatedPage
                .locator('form[name="$ctrl.changePassword"]')
                .locator('button:has-text("Update")');
            await updateBtn.click();

            const successAlert = authenticatedPage.locator(
                "div[uib-alert].alert-success",
            );
            await expect(successAlert).toBeVisible({ timeout: 15000 });
        } finally {
            if (adminUserId !== null) {
                try {
                    await resetUserPassword(authenticatedPage, adminUserId);
                } catch (err) {
                    console.error(
                        "Failed to restore admin password via API:",
                        err,
                    );
                }
            }
        }
    });

    test("should show validation error if confirmation password does not match", async ({
        authenticatedPage,
        adminCredentials,
    }) => {
        test.skip(
            !authenticatedPage || !adminCredentials,
            "Skipping test: No authenticated session available.",
        );
        if (!authenticatedPage || !adminCredentials) return;

        const adminPage = new AdminPage(authenticatedPage);
        await adminPage.goto();
        await adminPage.gotoEditAccount();

        const oldPasswordInput = authenticatedPage.locator("#oldPassword");
        await oldPasswordInput.waitFor({ state: "visible", timeout: 15000 });

        await oldPasswordInput.fill(adminCredentials.password);
        await authenticatedPage.fill("#newPassword", "ValidP@ss123!");
        await authenticatedPage.fill("#newPasswordConfirm", "NonMatchingPass");

        const updateBtn = authenticatedPage
            .locator('form[name="$ctrl.changePassword"]')
            .locator('button:has-text("Update")');
        await updateBtn.click();

        const mismatchError = authenticatedPage.locator(
            'div[data-ng-message="alfio.new-password-does-not-match"]',
        );
        await expect(mismatchError).toBeVisible();
    });

    test("should show validation error if new password does not meet complexity rules", async ({
        authenticatedPage,
        adminCredentials,
    }) => {
        test.skip(
            !authenticatedPage || !adminCredentials,
            "Skipping test: No authenticated session available.",
        );
        if (!authenticatedPage || !adminCredentials) return;

        const adminPage = new AdminPage(authenticatedPage);
        await adminPage.goto();
        await adminPage.gotoEditAccount();

        const oldPasswordInput = authenticatedPage.locator("#oldPassword");
        await oldPasswordInput.waitFor({ state: "visible", timeout: 15000 });

        await oldPasswordInput.fill(adminCredentials.password);
        await authenticatedPage.fill("#newPassword", "123");
        await authenticatedPage.fill("#newPasswordConfirm", "123");

        const updateBtn = authenticatedPage
            .locator('form[name="$ctrl.changePassword"]')
            .locator('button:has-text("Update")');
        await updateBtn.click();

        const complexityError = authenticatedPage.locator(
            'div[data-ng-message="alfio.new-password-invalid"]',
        );
        await expect(complexityError).toBeVisible();
    });
});
