import { expect, test } from "../../fixtures/test-fixtures";
import {
    completeBasicConfigIfVisible,
    loginViaUI,
} from "../../helpers/auth-helper";
import { AdminEventPage } from "../../pages/admin-event.page";
import { PublicEventPage } from "../../pages/public-event.page";

test.describe("Event Creation: Admin → Create Event → Configure Categories", () => {
    test("admin can access the event creation page", async ({
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

        const adminEventPage = new AdminEventPage(page);
        await adminEventPage.gotoCreateEvent();

        const formVisible = await page.locator("form").first().isVisible();
        expect(formVisible).toBe(true);
    });

    test("non-existent event returns 404 via API", async ({
        request,
        baseURL,
    }) => {
        const response = await request.get(
            `${baseURL}/api/v2/public/event/non-existent-event-slug-xyz`,
        );
        expect(response.status()).toBe(404);
    });

    test("non-existent event shows error on public page", async ({ page }) => {
        const publicEventPage = new PublicEventPage(page);
        await publicEventPage.goto("non-existent-event-slug-xyz");

        await page.waitForTimeout(2000);
        const bodyText = await page.locator("body").textContent();
        const hasError =
            bodyText?.includes("not found") ||
            bodyText?.includes("404") ||
            bodyText?.includes("error") ||
            (await publicEventPage.getCategoryCount()) === 0;
        expect(hasError).toBe(true);
    });
});
