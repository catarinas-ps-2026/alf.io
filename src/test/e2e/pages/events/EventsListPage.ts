import type { Locator, Page } from "@playwright/test";

// The active-events list and the entry point into create/detail flows.
// Create form lives in CreateEventPage, detail view in EventDetailPage.
export class EventsPage {
    private readonly page: Page;

    readonly activeEventsHeading: Locator;
    readonly expiredEventsHeading: Locator;
    readonly createEventButton: Locator;
    // Unlike createEventButton (which also matches the always-shown link in
    // the empty-list state), this one only matches the header action that
    // the app actually gates behind $ctrl.isOwner - use it to assert on
    // role-based access, not just presence of any "create event" link.
    readonly createEventNavLink: Locator;
    readonly eventRows: Locator;
    readonly eventNameInput: Locator;
    readonly categoriesHeader: Locator;

    constructor(page: Page) {
        this.page = page;

        this.activeEventsHeading = page.locator("h1:has-text('Active Events')");
        this.expiredEventsHeading = page.locator("h1:has-text('Past events')");
        this.createEventButton = page.locator(
            'a[data-ui-sref="events.new"], button:has-text("Create Event"), a:has-text("Create Event")',
        );
        this.createEventNavLink = page.locator(
            '.panel-heading a[data-ui-sref="events.new"]',
        );
        // The active-events list is a <ul class="list-group"> of
        // <li class="list-group-item">, not a table - active-events-list.html
        // has no <table>/<tr> anywhere.
        this.eventRows = page.locator("li.list-group-item");
        this.eventNameInput = page.locator('input[name="displayName"]');
        this.categoriesHeader = page.locator("h3:has-text('Categories')");
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/");
    }

    async gotoCreateEvent(): Promise<void> {
        await this.page.goto("/admin/#/events/new");
    }

    // Clicks the real nav link instead of navigating by URL, for flows that
    // need to prove the button is actually there and usable.
    async openCreateEventForm(): Promise<void> {
        await this.createEventNavLink.click();
    }

    async gotoEventDetail(eventShortName: string): Promise<void> {
        await this.page.goto(`/admin/#/events/${eventShortName}/detail`);
    }

    async gotoEventConfiguration(eventShortName: string): Promise<void> {
        await this.page.goto(`/admin/#/events/${eventShortName}/configuration`);
    }

    async isEventListVisible(): Promise<boolean> {
        return this.activeEventsHeading.isVisible();
    }

    async isCreateEventLinkVisible(): Promise<boolean> {
        return this.createEventNavLink.isVisible();
    }

    async isCreateEventFormVisible(): Promise<boolean> {
        try {
            await this.eventNameInput.waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async isEventDetailVisible(): Promise<boolean> {
        try {
            await this.categoriesHeader.waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async getEventCount(): Promise<number> {
        return this.eventRows.count();
    }

    findEventByName(name: string): Locator {
        return this.page.locator("li.list-group-item", { hasText: name });
    }

    async isEventVisible(name: string): Promise<boolean> {
        try {
            await this.findEventByName(name).waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    // For asserting an event is gone after delete(): waiting for "visible"
    // resolves true immediately if the row is still present at call-time,
    // which races with the list's own async reload.
    async waitForEventRemoved(name: string): Promise<void> {
        await this.findEventByName(name).waitFor({
            state: "detached",
            timeout: 10000,
        });
    }

    async clickEvent(name: string): Promise<void> {
        const link = this.page.locator(`a:has-text("${name}")`).first();
        await link.click();
    }
}
