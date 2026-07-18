import path from "node:path";
import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import {
    CreateEventPage,
    EventDetailPage,
    EventsPage,
} from "../../../pages/events";

const TEST_LOGO_PATH = path.resolve(__dirname, "../../../data/test-logo.png");

test.describe("Events - Creae", () => {
    test("admin can create an event with a valid organization and the required information", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const events = new EventsPage(page);
        await events.goto();
        await events.openCreateEventForm();

        const shortName = `zzz-e2e-event-${randomString(6)}`;
        const createForm = new CreateEventPage(page);
        await createForm.waitUntilReady();
        await createForm.selectOrganization("E2E Org");
        await createForm.fillBasicInfo({
            displayName: `ZZZ E2E Create Event ${randomString(4)}`,
            location: "Remote Test Location",
            description: "Created by the Events E2E suite",
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
        await expect(detail.eventTitle).toContainText("ZZZ E2E Create Event");

        await detail.delete(shortName);
    });

    test("creating an event without selecting an organization is blocked", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const events = new EventsPage(page);
        await events.goto();
        await events.openCreateEventForm();

        const createForm = new CreateEventPage(page);
        await createForm.waitUntilReady();

        // Fill in everything the form allows before an organizer is picked -
        // Organization is the only thing left unset here.
        await createForm.fillBasicInfo({
            displayName: `ZZZ E2E Org Required ${randomString(4)}`,
            location: "Remote Test Location",
            description: "Should not be saveable without an organizer",
            shortName: `zzz-e2e-org-required-${randomString(6)}`,
            websiteUrl: "https://e2e.test",
            termsAndConditionsUrl: "https://e2e.test/terms",
        });
        await createForm.uploadLogo(TEST_LOGO_PATH);

        // Scroll to it like a real user would, instead of only checking a
        // hidden DOM state - the banner must actually be on screen.
        await createForm.organizerRequiredMessage.scrollIntoViewIfNeeded();
        await expect(createForm.organizerRequiredMessage).toBeVisible();
        await expect(createForm.saveButton).toHaveCount(0);
    });
});
