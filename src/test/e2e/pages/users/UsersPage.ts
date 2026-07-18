import type { Locator, Page } from "@playwright/test";

// List page: table queries and row-level actions (Reset Password, Disable,
// Enable, Delete). Create/Edit forms live in CreateUserPage/EditUserPage.
export class UsersPage {
    readonly page: Page;
    readonly addNewUserLink: Locator;
    readonly enabledUsersHeading: Locator;
    readonly disabledUsersHeading: Locator;
    readonly credentialsModal: Locator;
    readonly credentialsModalCloseButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.addNewUserLink = page
            .getByRole("link", { name: "add new" })
            .first();
        this.enabledUsersHeading = page.getByRole("heading", {
            name: "Users enabled",
        });
        this.disabledUsersHeading = page.getByRole("heading", {
            name: "Users not enabled",
        });
        this.credentialsModal = page.getByRole("heading", {
            name: "User credentials",
        });
        this.credentialsModalCloseButton = page.getByRole("button", {
            name: "close",
        });
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/#/users/");
        await this.enabledUsersHeading.waitFor({ state: "visible" });
    }

    async openCreateForm(): Promise<void> {
        await this.addNewUserLink.click();
    }

    findRowByUsername(username: string): Locator {
        return this.page.locator("tr", { hasText: username });
    }

    async isUserVisible(username: string): Promise<boolean> {
        try {
            await this.findRowByUsername(username).waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    // For asserting a row is gone after delete(): waiting for "visible" (as
    // isUserVisible does) resolves true immediately if the row is still
    // present at call-time, which races with Angular's async re-render.
    async waitForUserRemoved(username: string): Promise<void> {
        await this.findRowByUsername(username).waitFor({
            state: "detached",
            timeout: 10000,
        });
    }

    // Column order per users.html: username(0), name(1), description(2,
    // hidden - only used for the apikey variant of this same table),
    // role(3), member of(4), actions(5). The hidden description <td> is
    // still present in the DOM for user rows, so it always counts towards
    // nth() even though ng-show keeps it empty and invisible.
    async getRoleFor(username: string): Promise<string | null> {
        return this.findRowByUsername(username)
            .locator("td")
            .nth(3)
            .textContent();
    }

    roleCellFor(username: string): Locator {
        return this.findRowByUsername(username).locator("td").nth(3);
    }

    async getOrganizationFor(username: string): Promise<string | null> {
        return this.findRowByUsername(username)
            .locator("td")
            .nth(4)
            .textContent();
    }

    // The row's own username link and its "Edit" action link point to the
    // same URL and both match a plain hasText/name lookup once a username
    // contains the word "edit" - only the action link carries the `.btn`
    // class, so scope by that instead of by (exact) accessible name. Exact
    // name matching doesn't work here either: the action buttons' FontAwesome
    // icons contribute a private-use-area glyph to the computed accessible
    // name, so it never equals the plain label text exactly.
    // Every row also has a hidden (ng-hide) API-key edit link with the same
    // class and label - ":visible" excludes it.
    async clickEditFor(username: string): Promise<void> {
        await this.findRowByUsername(username)
            .locator("a.btn:visible", { hasText: "Edit" })
            .click();
    }

    // Reset Password / Disable / Delete all trigger a native confirm() -
    // Playwright auto-dismisses dialogs unless a handler is registered first.
    private async withConfirmAccepted(
        action: () => Promise<void>,
    ): Promise<void> {
        this.page.once("dialog", (dialog) => {
            // Guards against an occasional duplicate dialog event for the
            // same confirm() - accepting an already-resolved dialog throws.
            dialog.accept().catch(() => {});
        });
        await action();
    }

    async resetPasswordFor(username: string): Promise<void> {
        await this.withConfirmAccepted(async () => {
            await this.findRowByUsername(username)
                .getByRole("button", { name: "Reset Password" })
                .click();
        });
        await this.credentialsModal.waitFor({ state: "visible" });
    }

    async closeCredentialsModal(): Promise<void> {
        await this.credentialsModalCloseButton.click();
    }

    async disableUser(username: string): Promise<void> {
        await this.withConfirmAccepted(async () => {
            await this.findRowByUsername(username)
                .getByRole("button", { name: "Disable" })
                .click();
        });
    }

    async enableUser(username: string): Promise<void> {
        await this.withConfirmAccepted(async () => {
            await this.findRowByUsername(username)
                .getByRole("button", { name: "Enable" })
                .click();
        });
    }

    async deleteUser(username: string): Promise<void> {
        await this.withConfirmAccepted(async () => {
            await this.findRowByUsername(username)
                .getByRole("button", { name: "Delete" })
                .click();
        });
    }

    async isUserInEnabledTable(username: string): Promise<boolean> {
        const row = this.enabledUsersHeading
            .locator("xpath=following::table[1]")
            .locator("tr", { hasText: username });
        try {
            await row.waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }

    async isUserInDisabledTable(username: string): Promise<boolean> {
        const row = this.disabledUsersHeading
            .locator("xpath=following::table[1]")
            .locator("tr", { hasText: username });
        try {
            await row.waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }
}
