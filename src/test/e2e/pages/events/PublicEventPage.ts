import type { Locator, Page } from "@playwright/test";

// The public storefront is a separate frontend from the admin panel.
// Used to confirm a just-published event is reachable at its public URL
// and that its key details (name, category, location) render correctly.
export class PublicEventPage {
    readonly page: Page;
    readonly eventTitle: Locator;
    readonly ticketCategories: Locator;

    constructor(page: Page) {
        this.page = page;
        this.eventTitle = page.locator(
            ".col-12.col-sm-5.col-md-8.text-center h1",
        );
        this.ticketCategories = page.locator("div.card.mt-4");
    }

    async goto(url: string): Promise<void> {
        await this.page.goto(url);
    }

    async isEventNameVisible(displayName: string): Promise<boolean> {
        try {
            await this.page
                .getByText(displayName)
                .first()
                .waitFor({ state: "visible", timeout: 15000 });
            return true;
        } catch {
            return false;
        }
    }

    async getCategoryCount(): Promise<number> {
        return this.ticketCategories.count();
    }

    async getCategoryName(index: number): Promise<string> {
        const name = this.ticketCategories
            .nth(index)
            .locator("span.item-title");
        return ((await name.textContent()) ?? "").trim();
    }

    async isCategoryVisible(categoryName: string): Promise<boolean> {
        try {
            await this.page
                .getByText(categoryName)
                .first()
                .waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }
}
