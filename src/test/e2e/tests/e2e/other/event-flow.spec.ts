import { expect, test } from "../../../fixtures/event-flow-fixtures";
import { CheckInPage } from "../../../pages/check-in.page";
import { PublicEventPage } from "../../../pages/public-event.page";
import { ReservationPage } from "../../../pages/reservation.page";

const ATTENDEE_FIRST_NAME = "Test";
const ATTENDEE_LAST_NAME = "Attendee";
const ATTENDEE_EMAIL = "test.attendee@e2e.test";

test.describe
    .serial("Event Flow: Creation → Purchase → Check-in", () => {
        let eventSlug: string;
        let reservationId: string;

        test.beforeAll(async ({ event }) => {
            eventSlug = event.slug;
        });

        test("Test 1: Event is created and accessible via API", async ({
            request,
            baseURL,
        }) => {
            const response = await request.get(
                `${baseURL}/api/v2/public/event/${eventSlug}`,
            );
            expect(response.ok()).toBeTruthy();

            const eventData = await response.json();
            expect(eventData).toHaveProperty("shortName");
            expect(eventData.shortName).toBe(eventSlug);
            expect(eventData.displayName).toBeTruthy();
        });

        test("Test 2: Event is visible on public page with correct details", async ({
            page,
        }) => {
            const publicEventPage = new PublicEventPage(page);
            await publicEventPage.goto(eventSlug);

            const title = await publicEventPage.getEventTitle();
            expect(title).toBeTruthy();
            expect(title.length).toBeGreaterThan(0);

            const categoryCount = await publicEventPage.getCategoryCount();
            expect(categoryCount).toBeGreaterThanOrEqual(1);

            const categoryName = await publicEventPage.getCategoryName(0);
            expect(categoryName).toBe("Standard");
        });

        test("Test 3: Ticket purchase with ON_SITE payment", async ({
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
            expect(categoryId).toBeTruthy();

            await publicEventPage.selectCategoryQuantity(Number(categoryId), 1);
            await publicEventPage.clickContinue();

            reservationId = await publicEventPage.waitForReservationCreated();
            expect(reservationId).toBeTruthy();

            const reservationPage = new ReservationPage(page);
            await reservationPage.fillAndSubmitAttendeeInfo(
                ATTENDEE_FIRST_NAME,
                ATTENDEE_LAST_NAME,
                ATTENDEE_EMAIL,
            );

            await page.waitForURL(/.*\/reservation\/.*\/overview/, {
                timeout: 15000,
            });

            await reservationPage.completePurchase();

            await page.waitForURL(/.*\/reservation\/.*\/success/, {
                timeout: 15000,
            });
        });

        test("Test 4: Reservation success page is displayed", async ({
            page,
        }) => {
            await page.goto(
                `/event/${eventSlug}/reservation/${reservationId}/success`,
            );

            const successAlert = page.locator(".alert.alert-success");
            await expect(successAlert).toBeVisible({ timeout: 10000 });
            expect(page.url()).toMatch(/\/success/);
        });

        test("Test 5: Check-in via admin UI", async ({ authenticatedPage }) => {
            const checkInPage = new CheckInPage(authenticatedPage);
            await checkInPage.goto(eventSlug);

            await checkInPage.waitForTableLoad();

            await checkInPage.searchAttendee(ATTENDEE_LAST_NAME);

            const pendingCount = await checkInPage.getPendingRowCount();
            expect(pendingCount).toBeGreaterThanOrEqual(1);

            const attendeeName = await checkInPage.getAttendeeName(0);
            expect(attendeeName).toContain(ATTENDEE_LAST_NAME);

            await checkInPage.manualCheckIn(0);

            await checkInPage.refresh();
            await checkInPage.switchToCheckedInTab();

            const checkedInCount = await checkInPage.getCheckedInRowCount();
            expect(checkedInCount).toBeGreaterThanOrEqual(1);
        });
    });
