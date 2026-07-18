import path from "node:path";
import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import {
    SubscriptionFormPage,
    SubscriptionsListPage,
} from "../../../pages/subscriptions";

const TEST_LOGO_PATH = path.resolve(__dirname, "../../../data/test-logo.png");

test.describe("Subscriptions", () => {
    // Single end-to-end flow: create, view it in the list, modify it, and
    // view it again. There's no delete/deactivate button anywhere in this
    // (beta) admin UI - only a backend DELETE endpoint that's never wired
    // up to a click - so this flow stops at re-viewing the modified title.
    test("admin can create, view, and modify a subscription", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const list = new SubscriptionsListPage(page);
        await list.goto();
        await list.addNewLink.click();

        const form = new SubscriptionFormPage(page);
        await form.waitUntilReady();

        const title = `ZZZ E2E Subscription ${randomString(4)}`;
        await form.fillBasicInfo({
            title,
            description: "Created by the E2E suite",
            termsAndConditionsUrl: "https://e2e.test/terms",
        });
        await form.uploadLogo(TEST_LOGO_PATH);
        await form.multiAccessPassType.click();
        await form.fillPricingInfo({
            price: "10",
            currency: "USD",
            maxEntries: "1",
        });
        await form.save();

        await expect(form.titleInput).toHaveValue(title);

        // view detail: back on the list, the new subscription's own card
        // already shows its title, price and description.
        await list.goto();
        expect(await list.isSubscriptionVisible(title)).toBe(true);

        // modify
        await list.clickEditFor(title);
        await form.waitUntilReady();
        const updatedTitle = `${title} Updated`;
        await form.titleInput.fill(updatedTitle);
        await form.save();
        await expect(form.titleInput).toHaveValue(updatedTitle);

        // view detail again
        await list.goto();
        expect(await list.isSubscriptionVisible(updatedTitle)).toBe(true);
        expect(await list.isSubscriptionVisible(title)).toBe(false);
    });
});
