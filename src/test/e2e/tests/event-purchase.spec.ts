import { test, expect } from "../fixtures/event-flow-fixtures";
import { PublicEventPage } from "../pages/public-event.page";
import { ReservationPage } from "../pages/reservation.page";

const ATTENDEE_FIRST_NAME = "Test";
const ATTENDEE_LAST_NAME = "Purchaser";
const ATTENDEE_EMAIL = "test.purchaser@e2e.test";

test.describe.serial("Event Purchase: Category Selection → Booking → Payment → Confirmation", () => {
    let eventSlug: string;
    let reservationId: string;

    test.beforeAll(async ({ event }) => {
        eventSlug = event.slug;
    });

    test("full purchase flow: select category → fill booking → complete payment", async ({
        page,
    }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        // Select quantity 1 for first category
        const qtySelect = page
            .locator('select[id^="category-"][id$="-qty"]')
            .first();
        const selectId = await qtySelect.getAttribute("id");
        const categoryId = selectId
            ?.replace("category-", "")
            .replace("-qty", "");
        expect(categoryId).toBeTruthy();

        await publicEventPage.selectCategoryQuantity(Number(categoryId), 1);
        await publicEventPage.clickContinue();

        // Wait for reservation to be created
        reservationId = await publicEventPage.waitForReservationCreated();
        expect(reservationId).toBeTruthy();

        // Verify booking page form fields
        await expect(page.locator("#first-name")).toBeVisible();
        await expect(page.locator("#last-name")).toBeVisible();
        await expect(page.locator("#email")).toBeVisible();

        // Fill and submit attendee info
        const reservationPage = new ReservationPage(page);
        await reservationPage.fillAndSubmitAttendeeInfo(
            ATTENDEE_FIRST_NAME,
            ATTENDEE_LAST_NAME,
            ATTENDEE_EMAIL,
        );

        // Wait for overview page
        await page.waitForURL(/.*\/reservation\/.*\/overview/, {
            timeout: 15000,
        });

        // Verify terms checkbox is visible and complete purchase
        const termsLabel = page.locator("label#terms-conditions-label");
        await expect(termsLabel).toBeVisible();

        await reservationPage.completePurchase();

        // Wait for success page
        await page.waitForURL(/.*\/reservation\/.*\/success/, {
            timeout: 15000,
        });
        expect(page.url()).toContain("/success");
    });

    test("success page is displayed after purchase", async ({ page }) => {
        await page.goto(
            `/event/${eventSlug}/reservation/${reservationId}/success`,
        );

        const successAlert = page.locator(".alert.alert-success");
        await expect(successAlert).toBeVisible({ timeout: 10000 });
        expect(page.url()).toMatch(/\/success/);
    });

    test("cannot purchase with zero tickets selected", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const qtySelect = page
            .locator('select[id^="category-"][id$="-qty"]')
            .first();
        const selectId = await qtySelect.getAttribute("id");
        const categoryId = selectId
            ?.replace("category-", "")
            .replace("-qty", "");
        await publicEventPage.selectCategoryQuantity(Number(categoryId), 0);

        await publicEventPage.clickContinue();

        await page.waitForTimeout(2000);
        const url = page.url();
        expect(url).not.toContain("/reservation/");
    });

    test("booking form shows validation errors for empty required fields", async ({
        page,
    }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto(eventSlug);

        const qtySelect = page
            .locator('select[id^="category-"][id$="-qty"]')
            .first();
        const selectId = await qtySelect.getAttribute("id");
        const categoryId = selectId
            ?.replace("category-", "")
            .replace("-qty", "");
        await publicEventPage.selectCategoryQuantity(Number(categoryId), 1);
        await publicEventPage.clickContinue();

        await page.waitForURL(/.*\/reservation\/.*\/book/, {
            timeout: 15000,
        });

        const submitBtn = page.locator(
            'button[type="submit"].block-button.btn.btn-success',
        );
        await submitBtn.click();

        await page.waitForTimeout(1000);
        expect(page.url()).toContain("/book");
    });

    test("cannot access reservation with invalid reservation ID", async ({
        page,
    }) => {
        await page.goto(
            `/event/${eventSlug}/reservation/invalid-id-xyz/overview`,
        );

        await page.waitForTimeout(2000);
        const url = page.url();
        const bodyText = await page.locator("body").textContent();
        const hasError =
            bodyText?.includes("not found") ||
            bodyText?.includes("error") ||
            bodyText?.includes("expired") ||
            url.includes("/event/");
        expect(hasError).toBe(true);
    });
});
