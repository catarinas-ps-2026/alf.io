import { test, expect } from "../fixtures/event-flow-fixtures";
import { PublicEventPage } from "../pages/public-event.page";

test.describe("Event Flow: Creation → Publication → Purchase → Check-in", () => {
    let eventSlug: string;

    test.beforeAll(async ({ event }) => {
        eventSlug = event.slug;
    });

    test("Test 1: Event is created and accessible via API", async ({
        event,
        request,
        baseURL,
    }) => {
        const response = await request.get(
            `${baseURL}/api/v2/public/event/${event.slug}`,
        );
        expect(response.ok()).toBeTruthy();

        const eventData = await response.json();
        expect(eventData).toHaveProperty("shortName");
        expect(eventData.shortName).toBe(event.slug);
        expect(eventData.displayName).toBeTruthy();
    });

    test("Test 2: Event is visible on public page with correct details", async ({
        page,
        event,
    }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(event.slug);

        // Verify event title is displayed
        const title = await publicEventPage.getEventTitle();
        expect(title).toBeTruthy();
        expect(title.length).toBeGreaterThan(0);

        // Verify at least one ticket category is shown
        const categoryCount = await publicEventPage.getCategoryCount();
        expect(categoryCount).toBeGreaterThanOrEqual(1);

        // Verify category name
        const categoryName = await publicEventPage.getCategoryName(0);
        expect(categoryName).toBe("Standard");
    });
});
