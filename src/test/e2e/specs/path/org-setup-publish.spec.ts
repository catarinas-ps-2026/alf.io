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
import { PublicEventPage } from "../../pages/events";
import { TEST_LOGO_PATH } from "../../helpers/paths";

test.describe("Path: organization setup to public event", () => {
    test("admin sets up an organization, creates and publishes an event, and it becomes visible at its public URL", async ({
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

        const orgName = `ZZZ E2E Path Org ${randomString(6)}`;
        let organizationId: number | undefined;

        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name: orgName,
                email: "e2e-path-org@e2e.test",
                description: "Created by the org-setup-publish path test",
            });
            await organizations.save();
            expect(await organizations.isOrganizationVisible(orgName)).toBe(
                true,
            );
            organizationId = await organizations.getOrganizationIdFor(orgName);

            const events = new EventsPage(page);
            await events.goto();
            await events.openCreateEventForm();

            const shortName = `zzz-e2e-path-${randomString(6)}`;
            const displayName = `ZZZ E2E Path Event ${randomString(4)}`;
            const createForm = new CreateEventPage(page);
            await createForm.waitUntilReady();
            await createForm.selectOrganization(orgName);
            await createForm.fillBasicInfo({
                displayName,
                location: "Remote Test Location",
                description: "Created by the org-setup-publish path test",
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

            await detail.publishEvent();
            const publicUrl = await detail.getPublicUrl();

            const publicEventPage = new PublicEventPage(page);
            await publicEventPage.goto(publicUrl);
            expect(await publicEventPage.isEventNameVisible(displayName)).toBe(
                true,
            );

            await detail.goto(shortName);
            await expect(detail.eventTitle).toContainText(displayName);
            await detail.delete(shortName);
        } finally {
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });
});
