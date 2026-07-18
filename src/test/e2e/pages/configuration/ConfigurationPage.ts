import type { Locator, Page } from "@playwright/test";

export class ConfigurationPage {
    private readonly page: Page;

    // Tabs
    readonly settingsTab: Locator;
    readonly internationalizationTab: Locator;
    readonly templatesTab: Locator;

    // System configuration
    readonly systemConfigHeader: Locator;
    readonly generalSection: Locator;
    readonly reservationUiSection: Locator;
    readonly emailSection: Locator;
    readonly paymentSection: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;
    readonly accessDeniedMessage: Locator;

    constructor(page: Page) {
        this.page = page;

        this.settingsTab = page.locator(
            '.nav-tabs li:has(a):has-text("Settings")',
        );
        this.internationalizationTab = page.locator(
            '.nav-tabs li:has(a):has-text("Internationalization")',
        );
        this.templatesTab = page.locator(
            '.nav-tabs li:has(a):has-text("Templates")',
        );

        this.systemConfigHeader = page.locator(
            "h1:has-text('System configuration')",
        );
        this.generalSection = page.locator("#GENERAL");
        this.reservationUiSection = page.locator("#RESERVATION_UI");
        this.emailSection = page.locator("#MAIL");
        this.paymentSection = page.locator("#PAYMENT");
        this.saveButton = page.locator(
            'button[type="submit"]:has-text("Save"), button.btn-success:has-text("Save")',
        );
        this.cancelButton = page.locator(
            'button:has-text("Cancel"), a:has-text("Cancel")',
        );
        // Rendered by system.html when the backend rejects
        // /admin/api/configuration/load for the current role (e.g. an
        // Organization Owner, who isn't allowed system-wide config).
        this.accessDeniedMessage = page.locator(
            ".alert-warning:has-text('not authorized to see system-wide configuration')",
        );
    }

    /**
     * Navigates to the system configuration screen. Does NOT wait for
     * either outcome, since the same URL renders a different result
     * depending on the caller's role - use isSystemConfigVisible() or
     * isAccessDeniedVisible() afterwards to assert on the outcome.
     */
    async gotoSystemConfiguration(): Promise<void> {
        await this.page.goto("/admin/#/configuration/system");
    }

    async gotoOrganizationConfiguration(orgId: number): Promise<void> {
        await this.page.goto(`/admin/#/configuration/organization/${orgId}`);
    }

    async gotoEventConfiguration(
        orgId: number,
        eventId: number,
    ): Promise<void> {
        await this.page.goto(
            `/admin/#/configuration/organization/${orgId}/event/${eventId}`,
        );
    }

    async isSystemConfigVisible(): Promise<boolean> {
        try {
            await this.systemConfigHeader.waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async isAccessDeniedVisible(): Promise<boolean> {
        try {
            await this.accessDeniedMessage.waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async getSettingInputByKey(key: string): Promise<Locator | null> {
        const inputs = this.page.locator(
            `input[id*="${key}"], textarea[id*="${key}"], select[id*="${key}"]`,
        );
        if ((await inputs.count()) > 0) {
            return inputs.first();
        }
        return null;
    }

    async setSettingValue(key: string, value: string): Promise<void> {
        const input = this.page.locator(
            `input[id*="${key}"], textarea[id*="${key}"]`,
        );
        if ((await input.count()) > 0) {
            await input.first().clear();
            await input.first().fill(value);
        }
    }

    async selectSettingRadio(key: string, value: string): Promise<void> {
        const radio = this.page.locator(
            `input[type="radio"][id*="${key}"][value="${value}"], input[type="radio"][name*="${key}"][value="${value}"]`,
        );
        if ((await radio.count()) > 0) {
            await radio.first().click();
        }
    }

    async selectSettingDropdown(key: string, value: string): Promise<void> {
        const select = this.page.locator(`select[id*="${key}"]`);
        if ((await select.count()) > 0) {
            await select.first().selectOption(value);
        }
    }

    async save(): Promise<void> {
        await this.saveButton.first().click();
    }

    async isSettingsTabActive(): Promise<boolean> {
        return this.settingsTab
            .locator("...")
            .getAttribute("class")
            .then((c) => c?.includes("active") || false);
    }
}
