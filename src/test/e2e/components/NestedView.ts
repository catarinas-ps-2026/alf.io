import type { Locator, Page } from "@playwright/test";

/**
 * Componente para manejar vistas anidadas in-place del admin.
 *
 * El CRUD de organizaciones y usuarios usa un patrón donde el form
 * se inyecta DENTRO de la lista via <div ui-view="namedView">.
 *
 * Ejemplo en organizations.html:
 *   <div ui-view="newOrganization"></div>
 *
 * Cuando navegas a "organizations.new", ui-router inyecta
 * <organization-edit> dentro de ese div.
 */
export class NestedView {
    private readonly page: Page;
    private readonly container: Locator;

    constructor(page: Page, uiViewName: string) {
        this.page = page;
        this.container = page.locator(`[data-ui-view="${uiViewName}"]`);
    }

    async isVisible(): Promise<boolean> {
        try {
            await this.container.waitFor({ state: "visible", timeout: 5000 });
            return true;
        } catch {
            return false;
        }
    }

    getForm(): Locator {
        return this.container.locator("form");
    }

    getInput(label: string): Locator {
        return this.container.locator(
            `label:has-text("${label}") + input, label:has-text("${label}") ~ input`,
        );
    }

    getSelect(label: string): Locator {
        return this.container.locator(
            `label:has-text("${label}") + select, label:has-text("${label}") ~ select`,
        );
    }

    getSaveButton(): Locator {
        return this.container.locator('button[type="submit"]:has-text("Save")');
    }

    getCancelButton(): Locator {
        return this.container.locator('button:has-text("Cancel")');
    }

    async fillInput(label: string, value: string): Promise<void> {
        const input = this.getInput(label);
        await input.fill(value);
    }

    async selectOption(label: string, optionText: string): Promise<void> {
        const select = this.getSelect(label);
        await select.selectOption({ label: optionText });
    }

    async save(): Promise<void> {
        const button = this.getSaveButton();
        await button.click();
    }

    async cancel(): Promise<void> {
        const button = this.getCancelButton();
        await button.click();
    }
}
