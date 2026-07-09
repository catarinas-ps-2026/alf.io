import type { Page } from "@playwright/test";

// The public storefront is a separate frontend from the admin panel - this
// is intentionally minimal, only used to confirm a just-published event is
// really reachable at its public URL, not to drive a purchase.
export class PublicEventPage {
    readonly page: Page;

    constructor(page: Page) {
        this.page = page;
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
}
