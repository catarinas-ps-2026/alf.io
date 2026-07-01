import type { Locator, Page } from "@playwright/test";

export class LoginPage {
    readonly page: Page;
    readonly usernameInput: Locator;
    readonly passwordInput: Locator;
    readonly loginButton: Locator;
    readonly cancelButton: Locator;
    readonly alertDanger: Locator;
    readonly alertSuccess: Locator;

    constructor(page: Page) {
        this.page = page;
        this.usernameInput = page.locator("#username");
        this.passwordInput = page.locator("#password");
        this.loginButton = page.locator('button[type="submit"]');
        this.cancelButton = page.locator('button[type="reset"]');
        this.alertDanger = page.locator(".alert-danger");
        this.alertSuccess = page.locator(".alert-success");
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

    async isSuccessVisible(): Promise<boolean> {
        return this.alertSuccess.isVisible();
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

    async isLoginFormVisible(): Promise<boolean> {
        return (
            (await this.usernameInput.isVisible()) &&
            (await this.passwordInput.isVisible()) &&
            (await this.loginButton.isVisible())
        );
    }
}
