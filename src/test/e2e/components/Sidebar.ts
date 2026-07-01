import type { Locator, Page } from "@playwright/test";

export class Sidebar {
    private readonly page: Page;
    private readonly sidebar: Locator;
    private readonly categoriesSection: Locator;
    private readonly actionsDropdown: Locator;

    constructor(page: Page) {
        this.page = page;
        this.sidebar = page.locator(".sidebar.menu-spacer");
        this.categoriesSection = this.sidebar.locator("h4:has-text('Categories')");
        this.actionsDropdown = this.sidebar.locator("#actions-dpdwn");
    }

    async isVisible(): Promise<boolean> {
        try {
            await this.sidebar.waitFor({ state: "visible", timeout: 10000 });
            return true;
        } catch {
            return false;
        }
    }

    async hasCategoriesSection(): Promise<boolean> {
        if (!(await this.isVisible())) return false;
        return this.categoriesSection.isVisible();
    }

    async hasActionsDropdown(): Promise<boolean> {
        if (!(await this.isVisible())) return false;
        return this.actionsDropdown.isVisible();
    }

    async clickAction(actionText: string): Promise<void> {
        await this.actionsDropdown.click();
        const actionLink = this.page.locator(`.dropdown-menu a:has-text("${actionText}")`);
        await actionLink.click();
    }

    async getCategoryLinks(): Promise<string[]> {
        const links = this.sidebar.locator(".list-group a");
        const count = await links.count();
        const categories: string[] = [];
        for (let i = 0; i < count; i++) {
            const text = await links.nth(i).textContent();
            if (text) categories.push(text.trim());
        }
        return categories;
    }
}
