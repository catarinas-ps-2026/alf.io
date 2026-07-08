import type { Locator, Page } from "@playwright/test";

export class ReservationPage {
    private readonly page: Page;
    private readonly firstNameInput: Locator;
    private readonly lastNameInput: Locator;
    private readonly emailInput: Locator;
    private readonly continueButton: Locator;
    private readonly acceptTermsLabel: Locator;
    private readonly submitPurchaseButton: Locator;
    private readonly validationErrors: Locator;

    constructor(page: Page) {
        this.page = page;
        this.firstNameInput = page.locator("#first-name");
        this.lastNameInput = page.locator("#last-name");
        this.emailInput = page.locator("#email");
        this.continueButton = page.locator(
            'button[type="submit"].block-button.btn.btn-success',
        );
        this.acceptTermsLabel = page.locator("label#terms-conditions-label");
        this.submitPurchaseButton = page.locator(
            'button[type="submit"].block-button.btn.btn-success',
        );
        this.validationErrors = page.locator(".has-error");
    }

    async fillAttendeeInfo(
        firstName: string,
        lastName: string,
        email: string,
    ): Promise<void> {
        await this.firstNameInput.fill(firstName);
        await this.lastNameInput.fill(lastName);
        await this.emailInput.fill(email);
    }

    async submitAttendeeInfo(): Promise<void> {
        await this.continueButton.click();
    }

    async fillAndSubmitAttendeeInfo(
        firstName: string,
        lastName: string,
        email: string,
    ): Promise<void> {
        await this.fillAttendeeInfo(firstName, lastName, email);
        await this.submitAttendeeInfo();
    }

    async hasValidationErrors(): Promise<boolean> {
        return this.validationErrors.count().then((c) => c > 0);
    }

    async getValidationErrorCount(): Promise<number> {
        return this.validationErrors.count();
    }

    async acceptTerms(): Promise<void> {
        await this.acceptTermsLabel.click();
    }

    async waitForReadyAndSubmit(): Promise<void> {
        await this.page.waitForFunction(
            () => {
                const btn = document.querySelector(
                    'button[type="submit"].block-button.btn.btn-success',
                ) as HTMLButtonElement;
                return btn && !btn.disabled;
            },
            { timeout: 15000 },
        );
        await this.submitPurchaseButton.click();
    }

    async completePurchase(): Promise<void> {
        await this.acceptTerms();
        await this.waitForReadyAndSubmit();
    }

    async isOnBookingPage(): Promise<boolean> {
        return this.page.url().includes("/reservation/") && this.page.url().includes("/book");
    }

    async isOnOverviewPage(): Promise<boolean> {
        return this.page.url().includes("/reservation/") && this.page.url().includes("/overview");
    }

    async isOnSuccessPage(): Promise<boolean> {
        return this.page.url().includes("/reservation/") && this.page.url().includes("/success");
    }

    async getReservationId(): Promise<string> {
        const url = this.page.url();
        const match = url.match(/\/reservation\/([^/]+)\//);
        return match?.[1] ?? "";
    }
}
