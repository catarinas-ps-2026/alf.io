import path from "node:path";
import { expect, test } from "../../fixtures/auth";
import { deleteOrganizationViaApi } from "../../helpers/auth-helper";
import { loginAs } from "../../flows/auth";
import { randomString } from "../../helpers/random";
import {
    CreateEventPage,
    EventDetailPage,
    EventsPage,
} from "../../pages/events";
import { OrganizationsPage } from "../../pages/organizations/OrganizationsPage";
import { CreateUserPage } from "../../pages/users/CreateUserPage";
import { UsersPage } from "../../pages/users/UsersPage";

const TEST_LOGO_PATH = path.resolve(__dirname, "../../data/test-logo.png");

// Organizations have no delete action in the admin UI (same gap as
// Subscriptions), so this flow can't test an actual delete cascade. Instead
// it proves containment: an event and a user created inside a fresh
// organization both correctly show up as belonging to it.
test.describe("Path: organization containment", () => {
    test("an event and a user created inside an organization both correctly belong to it", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const organizations = new OrganizationsPage(page);
        await organizations.goto();

        const orgName = `ZZZ E2E Contain Org ${randomString(6)}`;
        let organizationId: number | undefined;
        const username = `zzz-e2e-contain-${randomString(6)}`;

        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name: orgName,
                email: "e2e-contain-org@e2e.test",
                description: "Created by the organization-cascade path test",
            });
            await organizations.save();
            organizationId = await organizations.getOrganizationIdFor(
                orgName,
            );

            const events = new EventsPage(page);
            await events.goto();
            await events.openCreateEventForm();

            const shortName = `zzz-e2e-contain-${randomString(6)}`;
            const displayName = `ZZZ E2E Contain Event ${randomString(4)}`;
            const createForm = new CreateEventPage(page);
            await createForm.waitUntilReady();
            await createForm.selectOrganization(orgName);
            await createForm.fillBasicInfo({
                displayName,
                location: "Remote Test Location",
                description: "Created by the organization-cascade path test",
                shortName,
                websiteUrl: "https://e2e.test",
                termsAndConditionsUrl: "https://e2e.test/terms",
            });
            await createForm.fillSeatsAndPayment({
                availableSeats: "50",
                regularPrice: "10",
                currency: "USD",
                vatPercentage: "0",
            });
            await createForm.uploadLogo(TEST_LOGO_PATH);
            await createForm.addCategory("Standard");
            await createForm.save();

            const detail = new EventDetailPage(page);
            await expect(detail.eventTitle).toContainText(displayName);
            await expect(detail.organizedByText).toContainText(orgName);

            const users = new UsersPage(page);
            await users.goto();
            await users.openCreateForm();
            const createUserForm = new CreateUserPage(page);
            await createUserForm.waitUntilReady();
            await createUserForm.fillForm({
                organization: orgName,
                role: "Check-in supervisor",
                username,
                firstName: "Contain",
                lastName: "TestUser",
                email: `${username}@e2e.test`,
            });
            await createUserForm.save();

            expect(await users.isUserVisible(username)).toBe(true);
            expect(await users.getOrganizationFor(username)).toContain(
                orgName,
            );

            await users.deleteUser(username);
            await detail.goto(shortName);
            await detail.delete(shortName);
        } finally {
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });
});
