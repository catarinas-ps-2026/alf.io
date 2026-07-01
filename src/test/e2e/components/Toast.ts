import type { Locator, Page } from "@playwright/test";

export class Toast {
    private readonly page: Page;
    private readonly toastContainer: Locator;

    constructor(page: Page) {
        this.page = page;
        this.toastContainer = page.locator(".toast-container, .alert, [role='alert']");
    }

    async getSuccessMessage(): Promise<string | null> {
        const successToast = this.page.locator(".alert-success, .toast-success");
        if (await successToast.isVisible()) {
            return successToast.textContent();
        }
        return null;
    }

    async getErrorMessage(): Promise<string | null> {
        const errorToast = this.page.locator(".alert-danger, .toast-error");
        if (await errorToast.isVisible()) {
            return errorToast.textContent();
        }
        return null;
    }

    async getWarningMessage(): Promise<string | null> {
        const warningToast = this.page.locator(".alert-warning, .toast-warning");
        if (await warningToast.isVisible()) {
            return warningToast.textContent();
        }
        return null;
    }

    async waitForSuccess(timeout = 5000): Promise<string | null> {
        try {
            const successToast = this.page.locator(".alert-success, .toast-success");
            await successToast.waitFor({ state: "visible", timeout });
            return successToast.textContent();
        } catch {
            return null;
        }
    }

    async waitForError(timeout = 5000): Promise<string | null> {
        try {
            const errorToast = this.page.locator(".alert-danger, .toast-error");
            await errorToast.waitFor({ state: "visible", timeout });
            return errorToast.textContent();
        } catch {
            return null;
        }
    }

    async dismissAll(): Promise<void> {
        const closeButtons = this.page.locator(".alert .close, .toast .close");
        const count = await closeButtons.count();
        for (let i = 0; i < count; i++) {
            await closeButtons.nth(i).click();
        }
    }
}
