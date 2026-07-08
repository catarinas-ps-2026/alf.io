import { test, expect } from "../fixtures/event-flow-fixtures";
import { PublicEventPage } from "../pages/public-event.page";
import { ReservationPage } from "../pages/reservation.page";
import { CheckInPage } from "../pages/check-in.page";
import {
    loginViaUI,
    completeBasicConfigIfVisible,
} from "../helpers/auth-helper";

const ATTENDEE_FIRST_NAME = "CheckIn";
const ATTENDEE_LAST_NAME = "TestUser";
const ATTENDEE_EMAIL = "checkin.test@e2e.test";

test.describe.serial("Event Check-in: QR Validation → Manual Check-in → Verification", () => {
    let eventSlug: string;
    let reservationId: string;

    test.beforeAll(async ({ event }) => {
        eventSlug = event.slug;
    });

    test("purchase a ticket for check-in tests", async ({ page }) => {
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

    test("admin can access check-in page for the event", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
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

        await checkInPage.waitForTableLoad();
    });

    test("attendee appears in pending check-in list", async ({
        authenticatedPage,
    }) => {
        const checkInPage = new CheckInPage(authenticatedPage);
        await checkInPage.goto(eventSlug);
        await checkInPage.waitForTableLoad();

        await checkInPage.searchAttendee(ATTENDEE_LAST_NAME);

        const pendingCount = await checkInPage.getPendingRowCount();
        expect(pendingCount).toBeGreaterThanOrEqual(1);
    });

    test("attendee name matches purchased ticket", async ({
        authenticatedPage,
    }) => {
        const checkInPage = new CheckInPage(authenticatedPage);
        await checkInPage.goto(eventSlug);
        await checkInPage.waitForTableLoad();

        await checkInPage.searchAttendee(ATTENDEE_LAST_NAME);

        const attendeeName = await checkInPage.getAttendeeName(0);
        expect(attendeeName).toContain(ATTENDEE_LAST_NAME);
    });

    test("manual check-in marks attendee as checked in", async ({
        authenticatedPage,
    }) => {
        const checkInPage = new CheckInPage(authenticatedPage);
        await checkInPage.goto(eventSlug);
        await checkInPage.waitForTableLoad();

        await checkInPage.searchAttendee(ATTENDEE_LAST_NAME);

        // Perform check-in
        await checkInPage.manualCheckIn(0);

        // Refresh and verify in checked-in tab
        await checkInPage.refresh();
        await checkInPage.switchToCheckedInTab();

        const checkedInCount = await checkInPage.getCheckedInRowCount();
        expect(checkedInCount).toBeGreaterThanOrEqual(1);
    });

    test("checked-in attendee shows correct name", async ({
        authenticatedPage,
    }) => {
        const checkInPage = new CheckInPage(authenticatedPage);
        await checkInPage.goto(eventSlug);
        await checkInPage.waitForTableLoad();

        await checkInPage.switchToCheckedInTab();

        const checkedInName = await checkInPage.getCheckedInAttendeeName(0);
        expect(checkedInName).toContain(ATTENDEE_LAST_NAME);
    });

    test("searching for non-existent attendee returns no results", async ({
        authenticatedPage,
    }) => {
        const checkInPage = new CheckInPage(authenticatedPage);
        await checkInPage.goto(eventSlug);
        await checkInPage.waitForTableLoad();

        await checkInPage.searchAttendee("NonExistentName12345");

        const pendingCount = await checkInPage.getPendingRowCount();
        expect(pendingCount).toBe(0);
    });

    test("check-in page is not accessible without admin login", async ({
        page,
    }) => {
        // Try accessing check-in page without logging in
        await page.goto(`/admin/#/events/${eventSlug}/check-in`);

        // Should redirect to login
        await page.waitForTimeout(2000);
        const url = page.url();
        const isOnLogin =
            url.includes("/authentication") || url.includes("/login");
        expect(isOnLogin).toBe(true);
    });
});
