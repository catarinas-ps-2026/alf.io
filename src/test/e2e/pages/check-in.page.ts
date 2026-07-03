import type { Locator, Page } from "@playwright/test";

export class CheckInPage {
    private readonly page: Page;
    private readonly searchInput: Locator;
    private readonly pendingTable: Locator;
    private readonly checkedInTable: Locator;
    private readonly checkedInTab: Locator;

    constructor(page: Page) {
        this.page = page;
        this.searchInput = page.locator(
            'input.form-control[placeholder*="Search"]',
        ).first();
        this.pendingTable = page.locator("table.check-in-data").first();
        this.checkedInTable = page.locator("table.check-in-data").nth(1);
        this.checkedInTab = page.locator("ul.nav-tabs li").nth(1);
    }

    async goto(eventSlug: string): Promise<void> {
        await this.page.goto(`/admin/#/events/${eventSlug}/check-in`);
    }

    async waitForTableLoad(): Promise<void> {
        await this.pendingTable.waitFor({ state: "visible", timeout: 15000 });
        // Wait for AngularJS to render the rows
        await this.page.waitForFunction(
            () => {
                const table = document.querySelector("table.check-in-data");
                if (!table) return false;
                const tbody = table.querySelector("tbody");
                return tbody !== null;
            },
            { timeout: 15000 },
        );
    }

    async searchAttendee(name: string): Promise<void> {
        await this.searchInput.fill(name);
        // Wait for debounce
        await this.page.waitForTimeout(1000);
    }

    async getPendingRowCount(): Promise<number> {
        const rows = this.pendingTable.locator("tbody tr");
        return rows.count();
    }

    async getAttendeeName(rowIndex: number): Promise<string> {
        const row = this.pendingTable.locator("tbody tr").nth(rowIndex);
        const nameCell = row.locator("td").nth(1);
        return ((await nameCell.textContent()) ?? "").trim();
    }

    async manualCheckIn(rowIndex: number): Promise<void> {
        const row = this.pendingTable.locator("tbody tr").nth(rowIndex);
        const checkInButton = row.locator(
            'button:has-text("Check-In")',
        );
        await checkInButton.click();
        // Wait for the success notification
        await this.page.waitForTimeout(2000);
    }

    async switchToCheckedInTab(): Promise<void> {
        await this.checkedInTab.click();
        await this.page.waitForTimeout(1000);
    }

    async getCheckedInRowCount(): Promise<number> {
        const rows = this.checkedInTable.locator("tbody tr");
        return rows.count();
    }

    async getCheckedInAttendeeName(rowIndex: number): Promise<string> {
        const row = this.checkedInTable.locator("tbody tr").nth(rowIndex);
        const nameCell = row.locator("td").nth(1);
        return ((await nameCell.textContent()) ?? "").trim();
    }

    async refresh(): Promise<void> {
        const refreshButton = this.page.locator(
            'button:has-text("Refresh")',
        ).first();
        await refreshButton.click();
        await this.page.waitForTimeout(1000);
    }
}
