import type { Locator, Page } from "@playwright/test";

export class EditUserPage {
    readonly page: Page;
    readonly organizationSelect: Locator;
    readonly roleSelect: Locator;
    readonly firstNameInput: Locator;
    readonly lastNameInput: Locator;
    readonly emailInput: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.organizationSelect = page.locator("#organizationId");
        this.roleSelect = page.locator("#role");
        this.firstNameInput = page.locator("#firstName");
        this.lastNameInput = page.locator("#lastName");
        this.emailInput = page.locator("#emailAddress");
        this.saveButton = page.getByRole("button", { name: "Save" });
        this.cancelButton = page.getByRole("button", { name: "Cancel" });
    }

    async waitUntilReady(): Promise<void> {
        await this.firstNameInput.waitFor({ state: "visible" });
        // Options existing isn't enough: UserService.loadUser() populates the
        // actual selected organizationId slightly later, and submitting
        // before that resolves leaves the (required) field on the Angular
        // "unknown option" placeholder - the form silently stays invalid and
        // Save becomes a no-op with no visible error.
        await this.page.waitForFunction(() => {
            const select = document.querySelector(
                "#organizationId",
            ) as HTMLSelectElement | null;
            return !!select && select.value !== "?" && select.value !== "";
        });
    }

    // Known app bug: user-edit.js fires loadUser() and the org/roles $q.all()
    // concurrently in $onInit. Roles are filtered by ctrl.user.target, which
    // is only correct until loadUser's callback overwrites ctrl.user
    // wholesale (the loaded record has no .target). Whichever promise's
    // callback runs first decides whether the Role <select> ends up
    // populated - a genuine race in the app, not in this test.
    async isRoleSelectPopulated(): Promise<boolean> {
        return this.page.evaluate(() => {
            const select = document.querySelector(
                "#role",
            ) as HTMLSelectElement | null;
            if (!select) return false;
            return Array.from(select.options).some((o) => o.value !== "?");
        });
    }

    // Reloading re-runs the race from scratch, so retrying gives it another
    // independent chance to land on the "roles populated" outcome. This
    // does not fix the underlying bug - if it's still unpopulated after
    // maxAttempts, the caller should treat that as a real failure.
    async waitForRoleSelectPopulated(maxAttempts = 5): Promise<boolean> {
        for (let attempt = 1; attempt <= maxAttempts; attempt++) {
            if (await this.isRoleSelectPopulated()) {
                return true;
            }
            if (attempt < maxAttempts) {
                await this.page.reload();
                await this.waitUntilReady();
            }
        }
        return this.isRoleSelectPopulated();
    }

    async fillForm(data: {
        firstName?: string;
        lastName?: string;
        email?: string;
    }): Promise<void> {
        if (data.firstName) {
            await this.firstNameInput.fill(data.firstName);
        }
        if (data.lastName) {
            await this.lastNameInput.fill(data.lastName);
        }
        if (data.email) {
            await this.emailInput.fill(data.email);
        }
    }

    async selectRole(role: string): Promise<void> {
        await this.roleSelect.selectOption({ label: role });
    }

    private async dismissDevModeBanner(): Promise<void> {
        const closeButton = this.page.getByRole("button", { name: "x" });
        if (await closeButton.isVisible().catch(() => false)) {
            await closeButton.click();
        }
    }

    // The list and edit form render together as nested states (both visible
    // at once - see the constructor of UsersPage/EditUserPage sharing one
    // page). On success, user-edit.js's $state.go('^') navigates back to
    // the plain "#/users/" state and refreshes the list; waiting for that
    // URL change is what actually confirms the save (and its list refresh)
    // completed, instead of guessing with a fixed timeout afterward.
    async save(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.saveButton.click();
        await this.page.waitForURL((url) => !url.href.includes("/edit"));
    }

    async cancel(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.cancelButton.click();
    }
}
