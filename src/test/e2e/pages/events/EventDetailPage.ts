import type { Locator, Page } from "@playwright/test";

export class EventDetailPage {
    readonly page: Page;
    readonly eventTitle: Locator;
    readonly editLogisticInfoButton: Locator;
    readonly actionsDropdown: Locator;
    readonly deleteMenuItem: Locator;
    readonly addCategoryButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.eventTitle = page.locator("h1").first();
        // "Logistic info and description", "Seats and payment info", and
        // each ticket category all have their own "Edit" button - a broad
        // hasText div match plus .last() previously grabbed the wrong one
        // (it matched the whole page and landed on a category's edit
        // button). Anchor on the section's own <strong> heading instead and
        // take the next "Edit" button that follows it in the DOM.
        this.editLogisticInfoButton = page
            .locator("strong", { hasText: "Logistic info and description" })
            .locator("xpath=following::button[normalize-space(text())='Edit'][1]");
        this.actionsDropdown = page.locator("#actions-dpdwn");
        this.deleteMenuItem = page.getByRole("menuitem", { name: "Delete" });
        this.addCategoryButton = page
            .getByRole("button", { name: "add category" })
            .first();
    }

    async goto(eventShortName: string): Promise<void> {
        await this.page.goto(`/admin/#/events/${eventShortName}/detail`);
    }

    async openEditBasicInfo(): Promise<void> {
        await this.editLogisticInfoButton.click();
        const dialog = this.page.getByRole("dialog");
        await dialog.locator("#displayName").waitFor({ state: "visible" });
    }

    async editDisplayName(newName: string): Promise<void> {
        const dialog = this.page.getByRole("dialog");
        await dialog.locator("#displayName").fill(newName);
        await dialog.getByRole("button", { name: "Save" }).click();
        await dialog.waitFor({ state: "hidden" });
    }

    async getDisplayName(): Promise<string | null> {
        return this.eventTitle.textContent();
    }

    // Deletion requires typing the exact short name plus a confirmation
    // checkbox as a safety measure before the button is enabled.
    async delete(shortName: string): Promise<void> {
        await this.actionsDropdown.click();
        await this.deleteMenuItem.click();

        const dialog = this.page.getByRole("dialog");
        await dialog
            .getByPlaceholder("Enter short name")
            .fill(shortName);
        await dialog
            .getByRole("checkbox", { name: /I confirm that I want to delete/ })
            .check();
        await dialog
            .getByRole("button", { name: "Confirm deletion" })
            .click();
        await this.page.waitForURL((url) => !url.href.includes("/detail"));
    }

    // Each category renders in its own .panel, identified by the
    // .category-name element inside it (ticket-category-detail.html).
    categoryPanel(name: string): Locator {
        return this.page.locator(".panel", {
            has: this.page.locator(".category-name", { hasText: name }),
        });
    }

    // Unlike the category dialog opened mid-event-creation (which inherits
    // a price default from the event form), adding a category from the
    // detail page has no such default - "price" is required and empty,
    // so it must always be filled explicitly here or Save silently no-ops.
    async addCategory(name: string, price = "10"): Promise<void> {
        await this.addCategoryButton.click();
        const dialog = this.page.getByRole("dialog");
        await dialog.locator("#name").fill(name);
        await dialog.locator("#price").fill(price);
        await dialog.getByRole("button", { name: "Save" }).click();
        await dialog.waitFor({ state: "hidden" });
    }

    async editCategory(name: string, newName: string): Promise<void> {
        await this.categoryPanel(name)
            .getByRole("button", { name: "Edit category" })
            .click();
        const dialog = this.page.getByRole("dialog");
        await dialog.locator("#name").fill(newName);
        await dialog.getByRole("button", { name: "Save" }).click();
        await dialog.waitFor({ state: "hidden" });
    }

    // Only available when the event has more than one category (the app
    // won't let the last remaining category be deleted) and the category
    // itself has no checked-in/sold/pending tickets - see canBeDeleted() in
    // admin-application.js. A freshly-created second category satisfies both.
    async deleteCategory(name: string): Promise<void> {
        // Not a role-based lookup: this <a> has ng-click but no href (unlike
        // the ui-sref links in the same panel), so per ARIA it has no
        // implicit "link" role and getByRole("link") never matches it even
        // though it's visibly on screen.
        await this.categoryPanel(name).locator("a.btn-danger").click();
        const dialog = this.page.getByRole("dialog");
        await dialog.getByRole("button", { name: "Confirm deletion" }).click();
        await dialog.waitFor({ state: "hidden" });
    }

    async isCategoryVisible(name: string): Promise<boolean> {
        try {
            await this.categoryPanel(name).waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async waitForCategoryRemoved(name: string): Promise<void> {
        await this.categoryPanel(name).waitFor({
            state: "detached",
            timeout: 10000,
        });
    }
}
