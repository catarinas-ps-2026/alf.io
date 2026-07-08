import type { Locator, Page } from "@playwright/test";

export class SuccessPage {
    private readonly page: Page;
    private readonly successAlert: Locator;

    constructor(page: Page) {
        this.page = page;
        this.successAlert = page.locator(".alert.alert-success");
    }

    async isSuccessVisible(): Promise<boolean> {
        try {
            await this.successAlert.waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }

    async getSuccessText(): Promise<string> {
        return (await this.successAlert.textContent()) ?? "";
    }

    async getCurrentUrl(): Promise<string> {
        return this.page.url();
    }
}
