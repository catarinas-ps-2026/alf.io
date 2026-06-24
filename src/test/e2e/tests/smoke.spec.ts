import { expect, test } from "../fixtures/test-fixtures";

test.describe("Alf.io E2E Smoke Tests", () => {
    test("should load the home/landing page", async ({ page }) => {
        await page.goto("/");

        await expect(page).toHaveURL(/.*(login|event|admin|public)?/);
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

        const markdownContent = page.locator("div.markdown-content");
        await expect(markdownContent).toBeVisible({ timeout: 10000 });
    });
});
