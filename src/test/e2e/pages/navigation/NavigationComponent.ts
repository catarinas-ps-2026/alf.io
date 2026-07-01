import type { Locator, Page } from "@playwright/test";

export class NavigationComponent {
    private readonly page: Page;
    readonly eventsLink: Locator;
    readonly subscriptionsLink: Locator;
    readonly organizationsLink: Locator;
    readonly usersLink: Locator;
    readonly apiKeysLink: Locator;
    readonly groupsLink: Locator;
    readonly configurationLink: Locator;
    readonly extensionLink: Locator;
    readonly loggedUserIndicator: Locator;
    readonly editAccountLink: Locator;
    readonly logoutLink: Locator;

    constructor(page: Page) {
        this.page = page;
        this.eventsLink = this.page.locator(".navbar-nav a", {
            hasText: /^Events$/,
        });
        this.subscriptionsLink = this.page.locator(".navbar-nav a", {
            hasText: /^Subscriptions/,
        });
        this.organizationsLink = page.locator(
            '.navbar-nav a[data-ui-sref="organizations"]',
        );
        this.usersLink = page.locator('.navbar-nav a[data-ui-sref="users"]');
        this.apiKeysLink = page.locator('.navbar-nav a[data-ui-sref="apikey"]');
        this.groupsLink = page.locator('.navbar-nav a[data-ui-sref="groups"]');
        this.configurationLink = page.locator(
            '.navbar-nav a[data-ui-sref="configuration.system"]',
        );
        this.extensionLink = page.locator(
            '.navbar-nav a[data-ui-sref="extension.list"]',
        );
        this.loggedUserIndicator = page.locator(
            '.navbar-right:has-text("Logged in as")',
        );
        this.editAccountLink = page.locator(
            '.navbar-right a[data-ui-sref="edit-current-user"]',
        );
        this.logoutLink = page.locator('.navbar-right a:has-text("Log out")');
    }

    async isEventsLinkVisible(): Promise<boolean> {
        return this.eventsLink.isVisible();
    }

    async isSubscriptionsLinkVisible(): Promise<boolean> {
        return this.subscriptionsLink.isVisible();
    }

    async isOrganizationsLinkVisible(): Promise<boolean> {
        return this.organizationsLink.isVisible();
    }

    async isUsersLinkVisible(): Promise<boolean> {
        return this.usersLink.isVisible();
    }

    async isApiKeysLinkVisible(): Promise<boolean> {
        return this.apiKeysLink.isVisible();
    }

    async isGroupsLinkVisible(): Promise<boolean> {
        return this.groupsLink.isVisible();
    }

    async isConfigurationLinkVisible(): Promise<boolean> {
        return this.configurationLink.isVisible();
    }

    async isExtensionLinkVisible(): Promise<boolean> {
        return this.extensionLink.isVisible();
    }

    async gotoEvents(): Promise<void> {
        await this.eventsLink.click();
        await this.page.waitForLoadState("networkidle");
    }

    async gotoSubscriptions(): Promise<void> {
        await this.subscriptionsLink.click();
        await this.page.waitForURL(/.*subscriptions.*/);
    }

    async gotoOrganizations(): Promise<void> {
        await this.organizationsLink.click();
        await this.page.waitForURL(/.*organizations.*/);
    }

    async gotoUsers(): Promise<void> {
        await this.usersLink.click();
        await this.page.waitForURL(/.*users.*/);
    }

    async gotoApiKeys(): Promise<void> {
        await this.apiKeysLink.click();
        await this.page.waitForURL(/.*api-keys.*/);
    }

    async gotoGroups(): Promise<void> {
        await this.groupsLink.click();
        await this.page.waitForURL(/.*group.*/);
    }

    async gotoConfiguration(): Promise<void> {
        await this.configurationLink.click();
        await this.page.waitForURL(/.*configuration.*/);
    }

    async gotoExtension(): Promise<void> {
        await this.extensionLink.click();
        await this.page.waitForURL(/.*extension.*/);
    }

    async isNavItemActive(uiSref: string): Promise<boolean> {
        const navItem = this.page.locator(
            `.navbar-nav li:has(a[data-ui-sref="${uiSref}"])`,
        );
        const className = await navItem.getAttribute("class");
        return className?.includes("active") ?? false;
    }

    async getLoggedUsername(): Promise<string | null> {
        if (await this.loggedUserIndicator.isVisible()) {
            const text = await this.loggedUserIndicator.textContent();
            const match = text?.match(/Logged in as\s+(.+)/);
            return match?.[1]?.trim() || null;
        }
        return null;
    }

    async logout(): Promise<void> {
        await this.logoutLink.click();
        await this.page.waitForURL(/.*(login|authentication).*/);
    }
}
