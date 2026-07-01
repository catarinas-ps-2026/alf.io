import type { Locator, Page } from "@playwright/test";

export class OverviewPage {
    private readonly page: Page;
    private readonly privacyCheckbox: Locator;
    private readonly termsCheckbox: Locator;
    private readonly submitButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.privacyCheckbox = page.locator("#privacyPolicyAccepted");
        this.termsCheckbox = page.locator("#termsAndConditionsAccepted");
        this.submitButton = page.locator(
            'button[type="submit"].block-button.btn.btn-success',
        );
    }

    async acceptPrivacyAndTerms(): Promise<void> {
        // Only check if visible (may not be present for all events)
        if (await this.privacyCheckbox.isVisible({ timeout: 3000 }).catch(() => false)) {
            await this.privacyCheckbox.check();
        }
        if (await this.termsCheckbox.isVisible({ timeout: 3000 }).catch(() => false)) {
            await this.termsCheckbox.check();
        }
    }

    async selectPaymentMethod(methodId: string): Promise<void> {
        const radio = this.page.locator(`#${methodId}`);
        await radio.check();
    }

    async submit(): Promise<void> {
        await this.submitButton.click();
    }

    async completePurchase(methodId?: string): Promise<void> {
        await this.acceptPrivacyAndTerms();
        if (methodId) {
            await this.selectPaymentMethod(methodId);
        }
        await this.submit();
    }
}
