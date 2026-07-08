import type { Locator, Page } from "@playwright/test";

// list.html doubles as the detail view - each subscription's own panel
// already shows title, price, sale period and description with no
// separate "detail page" to navigate to.
export class SubscriptionsListPage {
    readonly page: Page;
    readonly addNewLink: Locator;

    constructor(page: Page) {
        this.page = page;
        this.addNewLink = page.getByRole("link", { name: "add new" }).first();
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/");
        // Scoped to .navbar-nav like NavigationComponent's own locator -
        // getByRole("link", {name: "Subscriptions beta"}) alone intermittently
        // hangs on actionability here even though it resolves to one element.
        await this.page
            .locator(".navbar-nav a", { hasText: /^Subscriptions/ })
            .click();
        await this.page.waitForURL(/.*subscriptions.*/);
    }

    // The plain nav link lands on whichever organization the container
    // controller defaults to - when a test needs a specific org (e.g. to
    // match the org an event was created in), go straight to its list/create
    // routes instead (subscriptions.js: 'subscriptions.list'/'subscriptions.new').
    async gotoOrg(organizationId: number): Promise<void> {
        await this.page.goto(`/admin/#/subscriptions/${organizationId}/list`);
    }

    async gotoNewFor(organizationId: number): Promise<void> {
        await this.page.goto(
            `/admin/#/subscriptions/${organizationId}/create`,
        );
    }

    linkedEventsButtonFor(title: string): Locator {
        return this.subscriptionCard(title).getByRole("button", {
            name: "Linked Events",
        });
    }

    async openLinkedEventsFor(title: string): Promise<void> {
        await this.linkedEventsButtonFor(title).click();
        await this.page
            .getByRole("heading", { name: "Linked events" })
            .waitFor({ state: "visible" });
    }

    async isEventLinkedInModal(eventDisplayName: string): Promise<boolean> {
        try {
            await this.page
                .getByRole("dialog")
                .getByText(eventDisplayName, { exact: true })
                .waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }

    async closeLinkedEventsModal(): Promise<void> {
        await this.page.getByRole("button", { name: "Close" }).click();
    }

    // Each subscription renders in its own .panel, identified by the
    // .category-name element holding its title (subscriptions/list.html).
    // Exact text match matters here: a substring match would make the
    // pre-edit title look "still visible" merely because it's a prefix of
    // "<title> Updated".
    subscriptionCard(title: string): Locator {
        return this.page.locator(".panel", {
            has: this.page.getByText(title, { exact: true }),
        });
    }

    async isSubscriptionVisible(title: string): Promise<boolean> {
        try {
            await this.subscriptionCard(title).waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async clickEditFor(title: string): Promise<void> {
        await this.subscriptionCard(title)
            .getByRole("link", { name: "Edit" })
            .click();
    }
}
