import path from "node:path";
import { expect, test } from "../../fixtures/auth";
import { loginAs } from "../../flows/auth";
import { randomString } from "../../helpers/random";
import {
    CreateEventPage,
    EventDetailPage,
    EventsPage,
} from "../../pages/events";

const TEST_LOGO_PATH = path.resolve(__dirname, "../../data/test-logo.png");

test.describe("Events - Edit", () => {
    test("admin can edit an event's general information and the changes are saved", async ({
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

        const shortName = `zzz-e2e-edit-event-${randomString(6)}`;
        const originalName = `ZZZ E2E Edit Event ${randomString(4)}`;
        const createForm = new CreateEventPage(page);
        await createForm.waitUntilReady();
        await createForm.selectOrganization("E2E Org");
        await createForm.fillBasicInfo({
            displayName: originalName,
            location: "Remote Test Location",
            description: "Will be edited by the Events E2E suite",
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
        try {
            const updatedName = `${originalName} Updated`;
            await detail.openEditBasicInfo();
            await detail.editDisplayName(updatedName);

            await expect(detail.eventTitle).toContainText(updatedName);
        } finally {
            await detail.delete(shortName);
        }
    });
});
