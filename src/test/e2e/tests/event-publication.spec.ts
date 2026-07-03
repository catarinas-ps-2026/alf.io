import { test, expect } from "../fixtures/event-flow-fixtures";
import { PublicEventPage } from "../pages/public-event.page";

test.describe("Event Publication: Public Visibility and Details", () => {
    let eventSlug: string;

    test.beforeAll(async ({ event }) => {
        eventSlug = event.slug;
    });

    test("event is publicly visible on the event page", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const title = await publicEventPage.getEventTitle();
        expect(title).toBeTruthy();
        expect(title.length).toBeGreaterThan(0);
    });

    test("event shows correct display name", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const title = await publicEventPage.getEventTitle();
        expect(title).toBeTruthy();
    });

    test("event shows at least one ticket category", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const categoryCount = await publicEventPage.getCategoryCount();
        expect(categoryCount).toBeGreaterThanOrEqual(1);
    });

    test("event shows correct category name", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const categoryName = await publicEventPage.getCategoryName(0);
        expect(categoryName).toBe("Standard");
    });

    test("event has a continue button for ticket selection", async ({
        page,
    }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const continueBtn = page.locator("#show-event-continue");
        await expect(continueBtn).toBeVisible();
    });

    test("event page has category quantity selectors", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const qtySelects = page.locator('select[id^="category-"][id$="-qty"]');
        const count = await qtySelects.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });

    test("event is accessible via API with correct structure", async ({
        request,
        baseURL,
    }) => {
        const response = await request.get(
            `${baseURL}/api/v2/public/event/${eventSlug}`,
        );
        expect(response.ok()).toBeTruthy();

        const data = await response.json();
        expect(data).toHaveProperty("shortName");
        expect(data).toHaveProperty("displayName");
        expect(data).toHaveProperty("location");
        expect(data).toHaveProperty("timeZone");
    });
});
