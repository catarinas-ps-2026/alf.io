import type { Locator, Page } from "@playwright/test";

export class OrganizationsPage {
    readonly page: Page;
    readonly addNewLink: Locator;
    readonly nameInput: Locator;
    readonly emailInput: Locator;
    readonly descriptionInput: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;
    readonly organizationsTable: Locator;

    constructor(page: Page) {
        this.page = page;
        this.addNewLink = page.getByRole("link", { name: "add new" });
        this.nameInput = page.locator("#org-name");
        this.emailInput = page.locator("#org-email");
        this.descriptionInput = page.locator("#org-description");
        this.saveButton = page.getByRole("button", { name: "Save" });
        this.cancelButton = page.getByRole("button", { name: "Cancel" });
        this.organizationsTable = page.locator("table").filter({
            has: page.locator("th", { hasText: "name" }),
        });
    }

    async goto(): Promise<void> {
        await this.page.goto("/admin/#/organizations/");
    }

    async openCreateForm(): Promise<void> {
        await this.addNewLink.click();
        await this.nameInput.waitFor({ state: "visible" });
    }

    async dismissDevModeBanner(): Promise<void> {
        const closeButton = this.page.getByRole("button", { name: "x" });
        if (await closeButton.isVisible().catch(() => false)) {
            await closeButton.click();
        }
    }

    async fillForm(data: {
        name: string;
        email: string;
        description: string;
    }): Promise<void> {
        await this.nameInput.fill(data.name);
        await this.emailInput.fill(data.email);
        await this.descriptionInput.fill(data.description);
    }

    async save(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.saveButton.click();
        await this.organizationsTable.waitFor({ state: "visible" });
    }

    async cancel(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.cancelButton.click();
        await this.organizationsTable.waitFor({ state: "visible" });
    }

    findRowByName(name: string): Locator {
        return this.page.locator("tr", { hasText: name });
    }

    async isOrganizationVisible(name: string): Promise<boolean> {
        try {
            await this.findRowByName(name).waitFor({
                state: "visible",
                timeout: 10000,
            });
            return true;
        } catch {
            return false;
        }
    }

    async waitForDescription(name: string, description: string): Promise<void> {
        await this.findRowByName(name)
            .locator("td")
            .nth(1)
            .filter({ hasText: description })
            .waitFor({ state: "visible", timeout: 10000 });
    }

    async getDescriptionFor(name: string): Promise<string | null> {
        return this.findRowByName(name).locator("td").nth(1).textContent();
    }

    async clickEditFor(name: string): Promise<void> {
        await this.findRowByName(name)
            .getByRole("link", { name: "Edit" })
            .click();
        await this.nameInput.waitFor({ state: "visible" });
    }

    async getOrganizationIdFor(name: string): Promise<number> {
        const href = await this.findRowByName(name)
            .getByRole("link", { name: "Edit" })
            .getAttribute("href");
        const match = href?.match(/organizations\/(\d+)\/edit/);
        if (!match) {
            throw new Error(
                `Could not extract organization id from href: ${href}`,
            );
        }
        return Number(match[1]);
    }
}
