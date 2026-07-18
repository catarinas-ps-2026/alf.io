import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import { CreateUserPage } from "../../../pages/users/CreateUserPage";
import { UsersPage } from "../../../pages/users/UsersPage";

// Disable, reset password, and delete are one continuous admin workflow on
// the same user rather than independent behaviors, so they share one test.
test.describe("Users - Disable, reset password, and delete", () => {
    test("admin can disable, reset the password of, and delete a user", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const users = new UsersPage(page);
        await users.goto();

        const username = `zzz-e2e-manage-${randomString(6)}`;
        await users.openCreateForm();
        const createForm = new CreateUserPage(page);
        await createForm.waitUntilReady();
        await createForm.fillForm({
            organization: "E2E Org",
            role: "Check-in supervisor",
            username,
            firstName: "Manage",
            lastName: "TestUser",
            email: `${username}@e2e.test`,
        });
        await createForm.save();

        let deleted = false;
        try {
            expect(await users.isUserInEnabledTable(username)).toBe(true);

            // Reset Password only exists as a row action while the user is
            // enabled - the disabled state's row only offers Enable/Delete.
            await users.resetPasswordFor(username);
            await expect(users.credentialsModal).toBeVisible();
            await users.closeCredentialsModal();

            await users.disableUser(username);
            expect(await users.isUserInDisabledTable(username)).toBe(true);

            await users.deleteUser(username);
            deleted = true;
            await users.waitForUserRemoved(username);
        } finally {
            if (!deleted) {
                await users.deleteUser(username);
            }
        }
    });
});
