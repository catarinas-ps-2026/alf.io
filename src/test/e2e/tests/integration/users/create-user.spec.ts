import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import { CreateUserPage } from "../../../pages/users/CreateUserPage";
import { UsersPage } from "../../../pages/users/UsersPage";

test.describe("Users - Create", () => {
    test("admin can create a user associated to an existing organization and it appears in the list", async ({
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

        const username = `zzz-e2e-create-${randomString(6)}`;
        await users.openCreateForm();
        const createForm = new CreateUserPage(page);
        await createForm.waitUntilReady();
        const orgName = await createForm.getFirstAvailableOrganization();
        await createForm.fillForm({
            organization: orgName,
            role: "Check-in supervisor",
            username,
            firstName: "Create",
            lastName: "TestUser",
            email: `${username}@e2e.test`,
        });
        try {
            await createForm.save();
            expect(await users.isUserVisible(username)).toBe(true);
        } finally {
            await users.deleteUser(username);
        }
    });

    test("organization owner can create a user in their org and change that user's role", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            ownerCredentials,
            baseURL || "http://localhost:8080",
        );

        const users = new UsersPage(page);
        await users.goto();

        const username = `zzz-e2e-ownerscope-${randomString(6)}`;
        await users.openCreateForm();
        const createForm = new CreateUserPage(page);
        await createForm.waitUntilReady();
        const orgName = await createForm.getFirstAvailableOrganization();
        await createForm.fillForm({
            organization: orgName,
            role: "Check-in supervisor",
            username,
            firstName: "Owner",
            lastName: "CreatedUser",
            email: `${username}@e2e.test`,
        });

        try {
            await createForm.save();
            expect(await users.isUserVisible(username)).toBe(true);
        } finally {
            await users.deleteUser(username);
        }
    });
});
