import type { Locator, Page } from "@playwright/test";

export class BookingPage {
    private readonly page: Page;
    private readonly firstNameInput: Locator;
    private readonly lastNameInput: Locator;
    private readonly emailInput: Locator;
    private readonly submitButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.firstNameInput = page.locator("#first-name");
        this.lastNameInput = page.locator("#last-name");
        this.emailInput = page.locator("#email");
        this.submitButton = page.locator(
            'button[type="submit"].block-button.btn.btn-success',
        );
    }

    async fillContactInfo(
        firstName: string,
        lastName: string,
        email: string,
    ): Promise<void> {
        await this.firstNameInput.fill(firstName);
        await this.lastNameInput.fill(lastName);
        await this.emailInput.fill(email);
    }

    async submit(): Promise<void> {
        await this.submitButton.click();
    }

    async fillAndSubmit(
        firstName: string,
        lastName: string,
        email: string,
    ): Promise<void> {
        await this.fillContactInfo(firstName, lastName, email);
        await this.submit();
    }
}
