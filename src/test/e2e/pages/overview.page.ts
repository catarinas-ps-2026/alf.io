import type { Page } from "@playwright/test";

export class OverviewPage {
    private readonly page: Page;

    constructor(page: Page) {
        this.page = page;
    }

    async acceptTerms(): Promise<void> {
        // Click the label which has `for="termsAndConditionsAccepted"`
        // This triggers native browser checkbox toggle + Angular change detection
        const label = this.page.locator("label#terms-conditions-label");
        await label.click();
    }

    async waitForReadyAndSubmit(): Promise<void> {
        // Wait for button to become enabled
        await this.page.waitForFunction(
            () => {
                const btn = document.querySelector(
                    'button[type="submit"].block-button.btn.btn-success',
                ) as HTMLButtonElement;
                return btn && !btn.disabled;
            },
            { timeout: 15000 },
        );
        await this.page
            .locator('button[type="submit"].block-button.btn.btn-success')
            .click();
    }

    async completePurchase(): Promise<void> {
        await this.acceptTerms();
        await this.waitForReadyAndSubmit();
    }
}
