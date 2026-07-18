import { expect, test } from "../../../fixtures/auth";
import { deleteOrganizationViaApi } from "../../../helpers/auth-helper";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import {
    CreateEventPage,
    EventDetailPage,
    EventsPage,
    PublicEventPage,
} from "../../../pages/events";
import { OrganizationsPage } from "../../../pages/organizations/OrganizationsPage";
import { TEST_LOGO_PATH } from "../../../helpers/paths";

test.describe("Path: organization setup to public event", () => {
    test("admin sets up an organization, creates and publishes an event, and it becomes visible at its public URL", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        // ── Step 0: Login ──────────────────────────────────────────────
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        // ── Step 1: Create organization ────────────────────────────────
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

            // ── Step 2: Create event ───────────────────────────────────
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

            // ── Step 3: Add category ───────────────────────────────────
            await createForm.addCategory("Standard");
            await createForm.save();

            // ── Verify: event detail page ──────────────────────────────
            const detail = new EventDetailPage(page);
            await expect(detail.eventTitle).toContainText(displayName);
            await expect(detail.organizedByText).toContainText(orgName);
            expect(await detail.isCategoryVisible("Standard")).toBe(true);

            // ── Step 4: Publish event ──────────────────────────────────
            await detail.publishEvent();
            const publicUrl = await detail.getPublicUrl();

            // ── Step 5: Verify on public storefront ────────────────────
            const publicEventPage = new PublicEventPage(page);
            await publicEventPage.goto(publicUrl);
            expect(await publicEventPage.isEventNameVisible(displayName)).toBe(
                true,
            );
            expect(await publicEventPage.isCategoryVisible("Standard")).toBe(
                true,
            );

            // ── Cleanup: delete event ──────────────────────────────────
            await detail.goto(shortName);
            await expect(detail.eventTitle).toContainText(displayName);
            await detail.delete(shortName);
        } finally {
            // ── Cleanup: delete organization ────────────────────────────
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });
});
