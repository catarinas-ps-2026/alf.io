import type { Locator, Page } from "@playwright/test";

export class ProfilePage {
    readonly page: Page;

    readonly firstNameInput: Locator;
    readonly lastNameInput: Locator;
    readonly emailInput: Locator;
    readonly updateInfoButton: Locator;

    readonly currentPasswordInput: Locator;
    readonly newPasswordInput: Locator;
    readonly confirmNewPasswordInput: Locator;
    readonly updatePasswordButton: Locator;

    readonly successAlert: Locator;
    readonly oldPasswordInvalidError: Locator;

    constructor(page: Page) {
        this.page = page;

        const infoForm = page.locator('form[name="$ctrl.editUser"]');
        this.firstNameInput = infoForm.locator("#firstName");
        this.lastNameInput = infoForm.locator("#lastName");
        this.emailInput = infoForm.locator("#emailAddress");
        this.updateInfoButton = infoForm.getByRole("button", {
            name: "Update",
        });

        const passwordForm = page.locator('form[name="$ctrl.changePassword"]');
        this.currentPasswordInput = passwordForm.locator("#oldPassword");
        this.newPasswordInput = passwordForm.locator("#newPassword");
        this.confirmNewPasswordInput = passwordForm.locator(
            "#newPasswordConfirm",
        );
        this.updatePasswordButton = passwordForm.getByRole("button", {
            name: "Update",
        });

        this.successAlert = page.getByRole("alert").filter({
            hasText: "Success!",
        });
        // exact text rendered by the ng-messages "old-password-invalid" key
        this.oldPasswordInvalidError = page.getByText(
            "Current password is not valid",
        );
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/#/profile/edit");
    }

    async updatePersonalInfo(data: {
        firstName?: string;
        lastName?: string;
        emailAddress?: string;
    }): Promise<void> {
        if (data.firstName !== undefined) {
            await this.firstNameInput.fill(data.firstName);
        }
        if (data.lastName !== undefined) {
            await this.lastNameInput.fill(data.lastName);
        }
        if (data.emailAddress !== undefined) {
            await this.emailInput.fill(data.emailAddress);
        }
        await this.updateInfoButton.click();
    }

    async changePassword(
        currentPassword: string,
        newPassword: string,
        confirmPassword: string = newPassword,
    ): Promise<void> {
        await this.currentPasswordInput.fill(currentPassword);
        await this.newPasswordInput.fill(newPassword);
        await this.confirmNewPasswordInput.fill(confirmPassword);
        await this.updatePasswordButton.click();
    }

    async isSuccessMessageVisible(): Promise<boolean> {
        try {
            await this.successAlert.waitFor({
                state: "visible",
                timeout: 5000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async isOldPasswordErrorVisible(): Promise<boolean> {
        try {
            await this.oldPasswordInvalidError.waitFor({
                state: "visible",
                timeout: 5000,
            });
            return true;
        } catch {
            return false;
        }
    }
}
