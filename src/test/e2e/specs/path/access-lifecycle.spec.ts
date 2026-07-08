import { expect, test } from "../../fixtures/auth";
import {
    deleteOrganizationViaApi,
    logoutViaUI,
} from "../../helpers/auth-helper";
import { loginAs } from "../../flows/auth";
import { randomString } from "../../helpers/random";
import { EventsPage } from "../../pages/events";
import { OrganizationsPage } from "../../pages/organizations/OrganizationsPage";
import { CreateUserPage } from "../../pages/users/CreateUserPage";
import { EditUserPage } from "../../pages/users/EditUserPage";
import { UsersPage } from "../../pages/users/UsersPage";

test.describe("Path: access lifecycle", () => {
    test("a user's access is scoped to their organization and changes when admin changes their role", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        const url = baseURL || "http://localhost:8080";
        await loginAs(page, adminCredentials, url);

        const organizations = new OrganizationsPage(page);
        await organizations.goto();

        const orgName = `ZZZ E2E Access Org ${randomString(6)}`;
        const otherOrgName = `ZZZ E2E Access Other Org ${randomString(6)}`;
        let organizationId: number | undefined;
        let otherOrganizationId: number | undefined;
        const username = `zzz-e2e-access-${randomString(6)}`;

        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name: orgName,
                email: "e2e-access-org@e2e.test",
                description: "Created by the access-lifecycle path test",
            });
            await organizations.save();
            organizationId = await organizations.getOrganizationIdFor(
                orgName,
            );

            await organizations.openCreateForm();
            await organizations.fillForm({
                name: otherOrgName,
                email: "e2e-access-other-org@e2e.test",
                description: "Sibling org used to prove access scoping",
            });
            await organizations.save();
            otherOrganizationId = await organizations.getOrganizationIdFor(
                otherOrgName,
            );

            const users = new UsersPage(page);
            await users.goto();
            await users.openCreateForm();
            const createForm = new CreateUserPage(page);
            await createForm.waitUntilReady();
            await createForm.fillForm({
                organization: orgName,
                role: "Organization owner",
                username,
                firstName: "Access",
                lastName: "LifecycleUser",
                email: `${username}@e2e.test`,
            });
            const password = await createForm.save();

            // As the new owner: can start creating events, and only sees
            // their own organization - not the sibling one.
            await logoutViaUI(page);
            await loginAs(page, { username, password }, url);

            const events = new EventsPage(page);
            await events.goto();
            expect(await events.isCreateEventLinkVisible()).toBe(true);

            await organizations.goto();
            expect(await organizations.isOrganizationVisible(orgName)).toBe(
                true,
            );
            expect(
                await organizations.isOrganizationVisible(otherOrgName),
            ).toBe(false);

            // Admin downgrades the user's role.
            await logoutViaUI(page);
            await loginAs(page, adminCredentials, url);
            await users.goto();
            await users.clickEditFor(username);
            const editForm = new EditUserPage(page);
            await editForm.waitUntilReady();
            await editForm.waitForRoleSelectPopulated();
            await editForm.selectRole("Check-in supervisor");
            await editForm.save();

            // Logging back in as the same user reflects the new, narrower role.
            await logoutViaUI(page);
            await loginAs(page, { username, password }, url);
            await events.goto();
            expect(await events.isCreateEventLinkVisible()).toBe(false);
        } finally {
            await logoutViaUI(page);
            await loginAs(page, adminCredentials, url);
            const users = new UsersPage(page);
            await users.goto();
            if (await users.isUserVisible(username)) {
                await users.deleteUser(username);
            }
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
            if (otherOrganizationId) {
                await deleteOrganizationViaApi(page, otherOrganizationId);
            }
        }
    });
});
