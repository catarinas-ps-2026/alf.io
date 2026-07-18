import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { randomString } from "../../../helpers/random";
import { EventDetailPage } from "../../../pages/events";

test.describe("Events - Ticket categories", () => {
    // Uses the `event` fixture (an already-existing event created via API,
    // not by this test) so this only exercises category CRUD, not event
    // creation. The fixture event ships with one category of its own -
    // deleting a category requires the event to have more than one (see
    // canBeDeleted() in admin-application.js), which a freshly-added
    // second category satisfies.
    test("admin can create, edit, and delete a ticket category on an existing event", async ({
        page,
        adminCredentials,
        baseURL,
        event,
    }) => {
        test.skip(!event, "Skipping test: Requires a test event.");
        if (!event) return;

        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const detail = new EventDetailPage(page);
        await detail.goto(event.slug);

        const categoryName = `ZZZ E2E Category ${randomString(4)}`;
        await detail.addCategory(categoryName);
        expect(await detail.isCategoryVisible(categoryName)).toBe(true);

        const updatedName = `${categoryName} Updated`;
        await detail.editCategory(categoryName, updatedName);
        expect(await detail.isCategoryVisible(updatedName)).toBe(true);

        await detail.deleteCategory(updatedName);
        await detail.waitForCategoryRemoved(updatedName);
        expect(await detail.isCategoryVisible(updatedName)).toBe(false);
    });
});
