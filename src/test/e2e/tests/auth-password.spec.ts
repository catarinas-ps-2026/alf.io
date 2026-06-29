import { execFileSync, execSync } from "node:child_process";
import { expect, test } from "../fixtures/test-fixtures";
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

        // Navigate to edit profile using the page link to avoid full reload issues
        await adminPage.gotoEditAccount();

        // Wait for AngularJS routing to render the form fields
        const oldPasswordInput = authenticatedPage.locator("#oldPassword");
        await oldPasswordInput.waitFor({ state: "visible", timeout: 15000 });

        // Fill form fields to change password
        const oldPassword = adminCredentials.password;
        const newPassword = "NewP@ssw0rd123!"; // meets complexity rules

        // Read current password hash from DB before changing
        let originalHash = "";
        try {
            const result = execSync(
                "docker exec alfio-w2-db-1 psql -U postgres -d alfio -t -A -c \"SELECT password FROM ba_user WHERE username = 'admin';\"",
            );
            originalHash = result.toString().trim();
        } catch (err) {
            console.error("Failed to read admin password hash:", err);
        }

        try {
            await oldPasswordInput.fill(oldPassword);
            await authenticatedPage.fill("#newPassword", newPassword);
            await authenticatedPage.fill("#newPasswordConfirm", newPassword);

            // Click the update button within the Change Password form (second form in page)
            const updateBtn = authenticatedPage
                .locator('form[name="$ctrl.changePassword"]')
                .locator('button:has-text("Update")');
            await updateBtn.click();

            // Wait for success alert to show up
            const successAlert = authenticatedPage.locator(
                "div[uib-alert].alert-success",
            );
            await expect(successAlert).toBeVisible({ timeout: 15000 });
        } finally {
            // Restore password in database directly to guarantee environment sanity
            if (originalHash) {
                try {
                    execFileSync("docker", [
                        "exec", "alfio-w2-db-1", "psql",
                        "-U", "postgres", "-d", "alfio", "-c",
                        `UPDATE ba_user SET password = '${originalHash}' WHERE username = 'admin';`,
                    ]);
                } catch (err) {
                    console.error(
                        "Failed to restore admin password via DB query:",
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

        // Verify the error message for mismatching passwords is visible
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
        await authenticatedPage.fill("#newPassword", "123"); // too short and simple
        await authenticatedPage.fill("#newPasswordConfirm", "123");

        const updateBtn = authenticatedPage
            .locator('form[name="$ctrl.changePassword"]')
            .locator('button:has-text("Update")');
        await updateBtn.click();

        // Verify complexity error message is shown
        const complexityError = authenticatedPage.locator(
            'div[data-ng-message="alfio.new-password-invalid"]',
        );
        await expect(complexityError).toBeVisible();
    });
});
