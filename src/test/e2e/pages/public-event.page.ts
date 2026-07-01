import type { Locator, Page } from "@playwright/test";

export class PublicEventPage {
    private readonly page: Page;
    private readonly eventTitle: Locator;
    private readonly eventLocation: Locator;
    private readonly eventOrganization: Locator;
    private readonly continueButton: Locator;
    private readonly ticketCategories: Locator;

    constructor(page: Page) {
        this.page = page;
        this.eventTitle = page.locator(
            ".col-12.col-sm-5.col-md-8.text-center h1",
        );
        this.eventLocation = page.locator("dl.row dd.col-11").nth(2);
        this.eventOrganization = page.locator("dl.row dd.col-11").first();
        this.continueButton = page.locator("#show-event-continue");
        this.ticketCategories = page.locator("div.card.mt-4");
    }

    async goto(slug: string): Promise<void> {
        await this.page.goto(`/event/${slug}`);
    }

    async getEventTitle(): Promise<string> {
        await this.eventTitle.waitFor({ state: "visible", timeout: 10000 });
        return (await this.eventTitle.textContent()) ?? "";
    }

    async getEventLocation(): Promise<string> {
        return (await this.eventLocation.textContent()) ?? "";
    }

    async getEventOrganization(): Promise<string> {
        return (await this.eventOrganization.textContent()) ?? "";
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

    async selectCategoryQuantity(
        categoryId: number,
        quantity: number,
    ): Promise<void> {
        const select = this.page.locator(`#category-${categoryId}-qty`);
        await select.selectOption(String(quantity));
    }

    async clickContinue(): Promise<void> {
        await this.continueButton.click();
    }

    async waitForReservationCreated(): Promise<string> {
        // After clicking continue, the page navigates to /event/:slug/reservation/:id/book
        await this.page.waitForURL(/.*\/reservation\/.*\/book/, {
            timeout: 15000,
        });
        const url = this.page.url();
        const match = url.match(/\/reservation\/([^/]+)\/book/);
        return match?.[1] ?? "";
    }
}
