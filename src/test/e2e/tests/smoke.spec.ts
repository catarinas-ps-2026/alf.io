import { expect, test } from "../fixtures/test-fixtures";

test.describe("Alf.io E2E Smoke Tests", () => {
    test("should load the home/landing page", async ({ page }) => {
        await page.goto("/");

        await expect(page).toHaveURL(/.*(login|event|admin|public)?/);

        // Capture a screenshot of the landing page and attach it to the report
        await test.info().attach("landing-page", {
            body: await page.screenshot(),
            contentType: "image/png",
        });
    });

    test("should load a dynamically created event page", async ({
        page,
        event,
    }) => {
        test.skip(
            !event,
            "Skipping dynamic event page test because E2E_SERVER_APIKEY is not set or event creation failed.",
        );

        if (!event) return;

        await page.goto(event.url);

        // Verify the page loaded successfully (Angular app renders with the event route)
        await expect(page).toHaveURL(new RegExp(`.*event/${event.slug}.*`));
        await expect(page.locator("app-root")).toBeAttached({ timeout: 10000 });
    });
});
