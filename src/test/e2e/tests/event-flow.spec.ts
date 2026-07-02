import { test, expect } from "../fixtures/event-flow-fixtures";
import { PublicEventPage } from "../pages/public-event.page";
import { BookingPage } from "../pages/booking.page";
import { OverviewPage } from "../pages/overview.page";
import { SuccessPage } from "../pages/success.page";
import { CheckInPage } from "../pages/check-in.page";
import { loginViaUI } from "../helpers/auth-helper";
import { completeBasicConfigIfVisible } from "../helpers/auth-helper";

const ATTENDEE_FIRST_NAME = "Test";
const ATTENDEE_LAST_NAME = "Attendee";
const ATTENDEE_EMAIL = "test.attendee@e2e.test";

test.describe.serial(
    "Event Flow: Creation → Purchase → Check-in",
    () => {
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

            // Get the category ID from the quantity select element
            const qtySelect = page.locator('select[id^="category-"][id$="-qty"]').first();
            const selectId = await qtySelect.getAttribute("id");
            const categoryId = selectId?.replace("category-", "").replace("-qty", "");
            expect(categoryId).toBeTruthy();

            // Select quantity 1 for the first category
            await publicEventPage.selectCategoryQuantity(Number(categoryId), 1);
            await publicEventPage.clickContinue();

            // Wait for reservation to be created and navigate to booking page
            reservationId = await publicEventPage.waitForReservationCreated();
            expect(reservationId).toBeTruthy();

            // Fill booking form
            const bookingPage = new BookingPage(page);
            await bookingPage.fillAndSubmit(
                ATTENDEE_FIRST_NAME,
                ATTENDEE_LAST_NAME,
                ATTENDEE_EMAIL,
            );

            // Wait for overview page
            await page.waitForURL(/.*\/reservation\/.*\/overview/, {
                timeout: 15000,
            });

            // Complete purchase (ON_SITE is auto-selected as the only payment method)
            const overviewPage = new OverviewPage(page);
            await overviewPage.completePurchase();

            // Wait for success page
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

            const successPage = new SuccessPage(page);
            const isSuccess = await successPage.isSuccessVisible();
            expect(isSuccess).toBe(true);

            expect(page.url()).toMatch(/\/success/);
        });

        test("Test 5: Check-in via admin UI", async ({
            page,
            adminCredentials,
            baseURL,
        }) => {
            // Login as admin first
            await loginViaUI(
                page,
                adminCredentials.username,
                adminCredentials.password,
            );
            await page.waitForURL(/.*(admin).*/);
            await completeBasicConfigIfVisible(
                page,
                baseURL || "http://localhost:8080",
            );

            const checkInPage = new CheckInPage(page);
            await checkInPage.goto(eventSlug);

            // Wait for the check-in table to load
            await checkInPage.waitForTableLoad();

            // Search for the attendee
            await checkInPage.searchAttendee(ATTENDEE_LAST_NAME);

            // Verify attendee appears in pending list
            const pendingCount = await checkInPage.getPendingRowCount();
            expect(pendingCount).toBeGreaterThanOrEqual(1);

            // Get the first attendee name and verify it matches
            const attendeeName = await checkInPage.getAttendeeName(0);
            expect(attendeeName).toContain(ATTENDEE_LAST_NAME);

            // Perform manual check-in
            await checkInPage.manualCheckIn(0);

            // Refresh and switch to checked-in tab
            await checkInPage.refresh();
            await checkInPage.switchToCheckedInTab();

            // Verify attendee is in checked-in list
            const checkedInCount = await checkInPage.getCheckedInRowCount();
            expect(checkedInCount).toBeGreaterThanOrEqual(1);
        });
    },
);
