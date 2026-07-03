import type { Locator, Page } from "@playwright/test";

export class AdminEventPage {
    private readonly page: Page;
    private readonly eventRows: Locator;

    constructor(page: Page) {
        this.page = page;
        this.eventRows = page.locator("table tbody tr");
    }

    async gotoEventList(): Promise<void> {
        await this.page.goto("/admin/#/events/");
        await this.page.waitForTimeout(1000);
    }

    async gotoCreateEvent(): Promise<void> {
        await this.page.goto("/admin/#/events/new");
        await this.page.waitForTimeout(1000);
    }

    async isEventListVisible(): Promise<boolean> {
        try {
            await this.page
                .locator("table")
                .first()
                .waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }

    async getEventCount(): Promise<number> {
        return this.eventRows.count();
    }

    async findEventBySlug(slug: string): Promise<boolean> {
        const text = await this.page.locator("body").textContent();
        return text?.includes(slug) ?? false;
    }

    async getEventStatus(slug: string): Promise<string | null> {
        const row = this.page.locator(`tr:has-text("${slug}")`).first();
        if ((await row.count()) === 0) return null;
        const statusCell = row.locator("td").last();
        return (await statusCell.textContent())?.trim() ?? null;
    }

    async clickEventBySlug(slug: string): Promise<void> {
        const link = this.page.locator(`a:has-text("${slug}")`).first();
        await link.click();
        await this.page.waitForTimeout(1000);
    }

    async deleteEvent(slug: string): Promise<void> {
        const row = this.page.locator(`tr:has-text("${slug}")`).first();
        if ((await row.count()) === 0) return;

        const deleteBtn = row.locator('button:has-text("Delete")');
        if ((await deleteBtn.count()) > 0) {
            await deleteBtn.click();
            // Confirm deletion in modal if present
            const confirmBtn = this.page.locator(
                '.modal button:has-text("Delete")',
            );
            if ((await confirmBtn.count()) > 0) {
                await confirmBtn.click();
            }
            await this.page.waitForTimeout(2000);
        }
    }

    async gotoEventDetail(slug: string): Promise<void> {
        await this.page.goto(`/admin/#/events/${slug}`);
        await this.page.waitForTimeout(1000);
    }
}
