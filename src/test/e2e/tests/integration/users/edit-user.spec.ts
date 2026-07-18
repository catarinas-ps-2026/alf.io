import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { logoutViaUI } from "../../../helpers/auth-helper";
import { randomString } from "../../../helpers/random";
import { EventsPage } from "../../../pages/events";
import { CreateUserPage } from "../../../pages/users/CreateUserPage";
import { EditUserPage } from "../../../pages/users/EditUserPage";
import { UsersPage } from "../../../pages/users/UsersPage";

test.describe("Users - Edit", () => {
    test("admin can edit an organization owner's personal information and the changes are saved", async ({
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

        const username = `zzz-e2e-edit-${randomString(6)}`;
        await users.openCreateForm();
        const createForm = new CreateUserPage(page);
        await createForm.waitUntilReady();
        const orgName = await createForm.getFirstAvailableOrganization();
        await createForm.fillForm({
            organization: orgName,
            role: "Organization owner",
            username,
            firstName: "Original",
            lastName: "Name",
            email: `${username}@e2e.test`,
        });
        await createForm.save();

        try {
            await users.clickEditFor(username);
            const editForm = new EditUserPage(page);
            await editForm.waitUntilReady();
            expect(await editForm.waitForRoleSelectPopulated()).toBe(true);
            await editForm.fillForm({ firstName: "Updated" });
            await editForm.save();

            await expect(users.findRowByUsername(username)).toContainText(
                "Updated",
            );
            await expect(users.roleCellFor(username)).toHaveText(
                "Organization owner",
                { timeout: 20000 },
            );
        } finally {
            await users.deleteUser(username);
        }
    });

    test("admin can change a user's role between owner and supervisor, and the new permissions take effect on login", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        const url = baseURL || "http://localhost:8080";
        await loginAs(page, adminCredentials, url);

        const users = new UsersPage(page);
        await users.goto();

        const username = `zzz-e2e-role-${randomString(6)}`;
        await users.openCreateForm();
        const createForm = new CreateUserPage(page);
        await createForm.waitUntilReady();
        const orgName = await createForm.getFirstAvailableOrganization();
        await createForm.fillForm({
            organization: orgName,
            role: "Organization owner",
            username,
            firstName: "Role",
            lastName: "ChangeUser",
            email: `${username}@e2e.test`,
        });
        const password = await createForm.save();
        expect(password).toBeTruthy();

        try {
            // Owner -> Supervisor
            await users.clickEditFor(username);
            const editForm = new EditUserPage(page);
            await editForm.waitUntilReady();
            expect(await editForm.waitForRoleSelectPopulated()).toBe(true);
            await editForm.selectRole("Check-in supervisor");
            await editForm.save();
            await expect(users.roleCellFor(username)).toHaveText(
                "Check-in supervisor",
                { timeout: 20000 },
            );

            await logoutViaUI(page);
            await loginAs(page, { username, password }, url);
            const eventsAsSupervisor = new EventsPage(page);
            await eventsAsSupervisor.goto();
            expect(await eventsAsSupervisor.isCreateEventLinkVisible()).toBe(
                false,
            );
            await logoutViaUI(page);

            // Supervisor -> Owner
            await loginAs(page, adminCredentials, url);
            await users.goto();
            await users.clickEditFor(username);
            await editForm.waitUntilReady();
            expect(await editForm.waitForRoleSelectPopulated()).toBe(true);
            await editForm.selectRole("Organization owner");
            await editForm.save();
            await expect(users.roleCellFor(username)).toHaveText(
                "Organization owner",
                { timeout: 20000 },
            );

            await logoutViaUI(page);
            await loginAs(page, { username, password }, url);
            const eventsAsOwner = new EventsPage(page);
            await eventsAsOwner.goto();
            expect(await eventsAsOwner.isCreateEventLinkVisible()).toBe(true);
            await logoutViaUI(page);
        } finally {
            await loginAs(page, adminCredentials, url);
            await users.goto();
            await users.deleteUser(username);
        }
    });
    test("organization owner can change that user's role of their own org", async ({
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
        await createForm.save();

        try {
            expect(await users.isUserVisible(username)).toBe(true);

            await users.clickEditFor(username);
            const editForm = new EditUserPage(page);
            await editForm.waitUntilReady();
            expect(await editForm.waitForRoleSelectPopulated()).toBe(true);
            await editForm.selectRole("Organization owner");
            await editForm.save();

            await expect(users.roleCellFor(username)).toHaveText(
                "Organization owner",
                { timeout: 20000 },
            );
        } finally {
            await users.deleteUser(username);
        }
    });
});
