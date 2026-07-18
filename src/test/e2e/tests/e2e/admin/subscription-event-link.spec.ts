import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { deleteOrganizationViaApi } from "../../../helpers/auth-helper";
import { TEST_LOGO_PATH } from "../../../helpers/paths";
import { randomString } from "../../../helpers/random";
import {
    CreateEventPage,
    EventDetailPage,
    EventsPage,
} from "../../../pages/events";
import { OrganizationsPage } from "../../../pages/organizations/OrganizationsPage";
import {
    SubscriptionFormPage,
    SubscriptionsListPage,
} from "../../../pages/subscriptions";

test.describe("Path: subscription linked to event", () => {
    test("a subscription created for an organization can be linked to one of its events", async ({
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

        const orgName = `ZZZ E2E Link Org ${randomString(6)}`;
        let organizationId: number | undefined;

        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name: orgName,
                email: "e2e-link-org@e2e.test",
                description: "Created by the subscription-event-link path test",
            });
            await organizations.save();
            organizationId = await organizations.getOrganizationIdFor(orgName);

            const events = new EventsPage(page);
            await events.goto();
            await events.openCreateEventForm();

            const shortName = `zzz-e2e-link-${randomString(6)}`;
            const displayName = `ZZZ E2E Link Event ${randomString(4)}`;
            const createForm = new CreateEventPage(page);
            await createForm.waitUntilReady();
            await createForm.selectOrganization(orgName);
            await createForm.fillBasicInfo({
                displayName,
                location: "Remote Test Location",
                description: "Created by the subscription-event-link path test",
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

            const subscriptions = new SubscriptionsListPage(page);
            await subscriptions.gotoNewFor(organizationId);
            const subscriptionForm = new SubscriptionFormPage(page);
            await subscriptionForm.waitUntilReady();

            const subscriptionTitle = `ZZZ E2E Link Subscription ${randomString(4)}`;
            await subscriptionForm.fillBasicInfo({
                title: subscriptionTitle,
                description: "Created by the subscription-event-link path test",
                termsAndConditionsUrl: "https://e2e.test/terms",
            });
            await subscriptionForm.uploadLogo(TEST_LOGO_PATH);
            await subscriptionForm.multiAccessPassType.click();
            await subscriptionForm.fillPricingInfo({
                price: "10",
                currency: "USD",
                maxEntries: "1",
            });
            await subscriptionForm.save();

            await detail.goto(shortName);
            await expect(detail.eventTitle).toContainText(displayName);
            await detail.openEditPrices();
            await detail.linkSubscription(subscriptionTitle);

            // Confirm it stuck: reopen the same dialog and check the box is
            // still checked.
            await detail.openEditPrices();
            expect(await detail.isSubscriptionLinked(subscriptionTitle)).toBe(
                true,
            );
            await detail.closeDialog();

            await subscriptions.gotoOrg(organizationId);
            await subscriptions.openLinkedEventsFor(subscriptionTitle);
            expect(await subscriptions.isEventLinkedInModal(displayName)).toBe(
                true,
            );
            await subscriptions.closeLinkedEventsModal();

            await detail.goto(shortName);
            await detail.delete(shortName);
        } finally {
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });
});
