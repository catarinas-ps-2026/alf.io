import type { Locator, Page } from "@playwright/test";

export class CreateUserPage {
    readonly page: Page;
    readonly organizationSelect: Locator;
    readonly roleSelect: Locator;
    readonly usernameInput: Locator;
    readonly firstNameInput: Locator;
    readonly lastNameInput: Locator;
    readonly emailInput: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;
    readonly credentialsModal: Locator;
    readonly credentialsModalCloseButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.organizationSelect = page.locator("#organizationId");
        this.roleSelect = page.locator("#role");
        this.usernameInput = page.locator("#username");
        this.firstNameInput = page.locator("#firstName");
        this.lastNameInput = page.locator("#lastName");
        this.emailInput = page.locator("#emailAddress");
        this.saveButton = page.getByRole("button", { name: "Save" });
        this.cancelButton = page.getByRole("button", { name: "Cancel" });
        this.credentialsModal = page.getByRole("heading", {
            name: "User credentials",
        });
        this.credentialsModalCloseButton = page.getByRole("button", {
            name: "close",
        });
    }

    async waitUntilReady(): Promise<void> {
        await this.usernameInput.waitFor({ state: "visible" });
        // Organization/Role <select>s populate asynchronously after the
        // form appears - wait for real options before interacting with them.
        await this.page.waitForFunction(() => {
            const select = document.querySelector(
                "#organizationId",
            ) as HTMLSelectElement | null;
            return (
                !!select &&
                Array.from(select.options).some((o) => o.text.trim() !== "")
            );
        });
        await this.page.waitForFunction(() => {
            const select = document.querySelector(
                "#role",
            ) as HTMLSelectElement | null;
            return (
                !!select &&
                Array.from(select.options).some((o) => o.text.trim() !== "")
            );
        });
    }

    async getFirstAvailableOrganization(): Promise<string> {
        const labels = await this.organizationSelect
            .locator("option")
            .allTextContents();
        const org = labels.map((l) => l.trim()).filter(Boolean)[0];
        if (!org) throw new Error("No organizations available in dropdown");
        return org;
    }

    // Angular inserts an empty placeholder option until ng-model resolves -
    // filter it out so callers see only real, selectable organizations.
    async getOrganizationOptionLabels(): Promise<string[]> {
        const labels = await this.organizationSelect
            .locator("option")
            .allTextContents();
        return labels.map((label) => label.trim()).filter(Boolean);
    }

    async fillForm(data: {
        organization?: string;
        role: string;
        username: string;
        firstName: string;
        lastName: string;
        email: string;
    }): Promise<void> {
        if (data.organization) {
            // AngularJS ng-options populates <option> elements asynchronously.
            // Wait for the specific option label to be present before selecting.
            await this.page.waitForFunction(
                (optLabel: string) => {
                    const sel = document.querySelector(
                        "#organizationId",
                    ) as HTMLSelectElement | null;
                    return (
                        !!sel &&
                        Array.from(sel.options).some(
                            (o) => o.text.trim() === optLabel,
                        )
                    );
                },
                data.organization,
            );
            await this.organizationSelect.selectOption({
                label: data.organization,
            });
        }
        await this.roleSelect.selectOption({ label: data.role });
        await this.usernameInput.fill(data.username);
        await this.firstNameInput.fill(data.firstName);
        await this.lastNameInput.fill(data.lastName);
        await this.emailInput.fill(data.email);
    }

    private async dismissDevModeBanner(): Promise<void> {
        const closeButton = this.page.getByRole("button", { name: "x" });
        if (await closeButton.isVisible().catch(() => false)) {
            await closeButton.click();
        }
    }

    // Saving a new (non-API-key) user always shows a "User credentials"
    // modal with the generated password - this reads it (needed to log in
    // as that user later) then closes the modal, landing back on the list
    // like a real admin's click-through.
    async save(): Promise<string> {
        await this.dismissDevModeBanner();
        await this.saveButton.click();
        await this.credentialsModal.waitFor({ state: "visible" });
        const text =
            (await this.page.getByText(/Password:/).textContent()) ?? "";
        const password = text.match(/Password:\s*(\S+)/)?.[1] ?? "";
        await this.credentialsModalCloseButton.click();
        return password;
    }

    async cancel(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.cancelButton.click();
    }
}
