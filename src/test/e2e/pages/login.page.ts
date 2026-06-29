import type { Locator, Page } from "@playwright/test";

export class LoginPage {
    private readonly page: Page;
    private readonly usernameInput: Locator;
    private readonly passwordInput: Locator;
    private readonly loginButton: Locator;
    private readonly cancelButton: Locator;
    private readonly alertDanger: Locator;

    constructor(page: Page) {
        this.page = page;
        this.usernameInput = page.locator("#username");
        this.passwordInput = page.locator("#password");
        this.loginButton = page.locator('button[type="submit"]');
        this.cancelButton = page.locator('button[type="reset"]');
        this.alertDanger = page.locator(".alert-danger");
    }

    async goto(): Promise<void> {
        await this.page.goto("/authentication");
    }

    async login(username: string, password: string): Promise<void> {
        await this.usernameInput.fill(username);
        await this.passwordInput.fill(password);
        await this.loginButton.click();
    }

    async getErrorMessage(): Promise<string | null> {
        if (await this.alertDanger.isVisible()) {
            return this.alertDanger.textContent();
        }
        return null;
    }

    async isErrorVisible(): Promise<boolean> {
        return this.alertDanger.isVisible();
    }

    async clickCancel(): Promise<void> {
        await this.cancelButton.click();
    }

    async getUsernameValue(): Promise<string> {
        return this.usernameInput.inputValue();
    }

    async getPasswordValue(): Promise<string> {
        return this.passwordInput.inputValue();
    }
}
