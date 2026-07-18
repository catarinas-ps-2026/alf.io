import path from "node:path";
import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import {
    CreateEventPage,
    EventDetailPage,
    EventsPage,
} from "../../../pages/events";

const TEST_LOGO_PATH = path.resolve(__dirname, "../../../data/test-event.png");

test.describe("Events - delete", () => {
    test("admin can delete an event and it no longer appears in the list", async ({
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

        const shortName = `zzz-e2e-delete-event-${randomString(6)}`;
        const displayName = `ZZZ E2E Delete Event ${randomString(4)}`;
        const createForm = new CreateEventPage(page);
        await createForm.waitUntilReady();
        const orgName = await createForm.getFirstAvailableOrganization();
        await createForm.selectOrganization(orgName);
        await createForm.fillBasicInfo({
            displayName,
            location: "Remote Test Location",
            description: "Will be deleted by the Events E2E suite",
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
        await detail.delete(shortName);

        await events.goto();
        await page.reload();
        await events.waitForEventRemoved(displayName);
        expect(await events.isEventVisible(displayName)).toBe(false);
    });
});
