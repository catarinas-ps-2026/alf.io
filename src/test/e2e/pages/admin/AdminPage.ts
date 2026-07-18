import type { Locator, Page } from "@playwright/test";

export class AdminPage {
    readonly page: Page;
    readonly loggedInIndicator: Locator;
    readonly logoutLink: Locator;
    readonly editAccountLink: Locator;
    readonly navbar: Locator;

    constructor(page: Page) {
        this.page = page;
        this.navbar = page.locator(".navbar-right");
        this.loggedInIndicator = page.locator(
            '.navbar-right:has-text("Logged in as")',
        );
        this.logoutLink = this.page.getByRole("link", {
            name: /log out/i,
        });
        this.editAccountLink = this.page.getByRole("link", {
            name: /edit account/i,
        });
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/");
        await this.loggedInIndicator.waitFor();
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

    async getLoggedUsername(): Promise<string | null> {
        const text = await this.getLoggedUserIndicatorText();
        if (!text) return null;
        const match = text.match(/Logged in as\s+(.+)/);
        return match?.[1]?.trim() || null;
    }
}
