import type { Locator, Page } from "@playwright/test";

export class AdminPage {
    private readonly page: Page;
    private readonly loggedInIndicator: Locator;
    private readonly logoutLink: Locator;
    private readonly editAccountLink: Locator;

    constructor(page: Page) {
        this.page = page;

        this.loggedInIndicator = page.locator(
            '.navbar-right:has-text("Logged in as")',
        );
        this.logoutLink = page.locator('.navbar-right a:has-text("Log out")');
        this.editAccountLink = page.locator(
            '.navbar-right a[data-ui-sref="edit-current-user"]',
        );
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/");
    }

    async isLoggedIn(): Promise<boolean> {
        try {
            await this.loggedInIndicator.waitFor({
                state: "visible",
                timeout: 5000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async logout(): Promise<void> {
        await this.logoutLink.click();

        await this.page.waitForURL(/.*(login|authentication).*/);
    }

    async gotoEditAccount(): Promise<void> {
        await this.editAccountLink.click();
    }

    async getLoggedUserIndicatorText(): Promise<string | null> {
        if (await this.loggedInIndicator.isVisible()) {
            return this.loggedInIndicator.textContent();
        }
        return null;
    }
}
