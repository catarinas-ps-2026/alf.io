import type { Locator, Page } from "@playwright/test";

export class CategoriesPage {
    private readonly page: Page;

    // Category list (in event detail)
    readonly categoriesSection: Locator;
    readonly categoryPanels: Locator;
    readonly addCategoryButton: Locator;
    readonly rearrangeButton: Locator;

    // Category form (edit/create modal or inline)
    readonly categoryNameInput: Locator;
    readonly categoryDescriptionInput: Locator;
    readonly maxTicketsInput: Locator;
    readonly priceInput: Locator;
    readonly saveCategoryButton: Locator;
    readonly cancelCategoryButton: Locator;
    readonly deleteCategoryButton: Locator;

    // Category detail panel
    readonly editCategoryButton: Locator;

    constructor(page: Page) {
        this.page = page;

        this.categoriesSection = page.locator(
            ".page-header:has-text('Categories')",
        );
        this.categoryPanels = page.locator(
            "div.panel:has(.category-name), div[id^='ticket-category-']",
        );
        this.addCategoryButton = page.locator(
            'button:has-text("add category"), button:has-text("Add new")',
        );
        this.rearrangeButton = page.locator(
            'button:has-text("Rearrange categories"), label:has-text("Rearrange")',
        );

        this.categoryNameInput = page.locator(
            'input[name*="name"], input[ng-model*="ticketCategory.name"]',
        );
        this.categoryDescriptionInput = page.locator(
            'textarea[name*="description"], textarea[ng-model*="ticketCategory.description"]',
        );
        this.maxTicketsInput = page.locator(
            'input[name*="maxTickets"], input[ng-model*="ticketCategory.maxTickets"]',
        );
        this.priceInput = page.locator(
            'input[name*="price"], input[ng-model*="ticketCategory.price"]',
        );
        this.saveCategoryButton = page.locator(
            'button[type="submit"]:has-text("Save"), button.btn-success:has-text("Save")',
        );
        this.cancelCategoryButton = page.locator(
            'button:has-text("Cancel"), button.btn-default:has-text("Cancel")',
        );
        this.deleteCategoryButton = page.locator(
            'button.btn-danger:has-text("delete"), a.btn-danger:has-text("delete")',
        );

        this.editCategoryButton = page.locator(
            'button:has-text("Edit category"), button.btn-warning:has-text("Edit")',
        );
    }

    async getCategoryCount(): Promise<number> {
        return this.categoryPanels.count();
    }

    async getCategoryNames(): Promise<string[]> {
        const names = this.page.locator(".category-name");
        const count = await names.count();
        const result: string[] = [];
        for (let i = 0; i < count; i++) {
            const text = await names.nth(i).textContent();
            if (text) {
                result.push(text.trim());
            }
        }
        return result;
    }

    async clickCategory(categoryName: string): Promise<void> {
        const categoryLink = this.page.locator(
            `.nav-stacked a:has-text("${categoryName}"), .category-name:has-text("${categoryName}")`,
        );
        await categoryLink.click();
    }

    async clickAddCategory(): Promise<void> {
        await this.addCategoryButton.first().click();
    }

    async fillCategoryForm(data: {
        name: string;
        description?: string;
        maxTickets?: string;
        price?: string;
    }): Promise<void> {
        await this.categoryNameInput.first().clear();
        await this.categoryNameInput.first().fill(data.name);

        if (
            data.description &&
            (await this.categoryDescriptionInput.first().count()) > 0
        ) {
            await this.categoryDescriptionInput.first().clear();
            await this.categoryDescriptionInput.first().fill(data.description);
        }

        if (
            data.maxTickets &&
            (await this.maxTicketsInput.first().count()) > 0
        ) {
            await this.maxTicketsInput.first().clear();
            await this.maxTicketsInput.first().fill(data.maxTickets);
        }

        if (data.price && (await this.priceInput.first().count()) > 0) {
            await this.priceInput.first().clear();
            await this.priceInput.first().fill(data.price);
        }
    }

    async saveCategory(): Promise<void> {
        await this.saveCategoryButton.first().click();
    }

    async deleteCategory(categoryName: string): Promise<void> {
        const deleteBtn = this.page
            .locator(
                `div[id^="ticket-category-"]:has(.category-name:has-text("${categoryName}"))`,
            )
            .locator(
                'button.btn-danger:has-text("delete"), a.btn-danger:has-text("delete")',
            );
        await deleteBtn.click();

        const confirmButton = this.page.locator(
            '.modal button.btn-danger, .modal button:has-text("Confirm"), .modal button:has-text("Delete")',
        );
        if (await confirmButton.isVisible({ timeout: 3000 })) {
            await confirmButton.click();
        }
    }

    async isCategoryVisible(categoryName: string): Promise<boolean> {
        const category = this.page.locator(
            `.category-name:has-text("${categoryName}"), div[id^="ticket-category-"]:has(.category-name:has-text("${categoryName}"))`,
        );
        return category.isVisible();
    }

    async clickRearrange(): Promise<void> {
        await this.rearrangeButton.click();
    }

    async moveCategoryUp(categoryName: string): Promise<void> {
        const moveUpBtn = this.page
            .locator(`div:has(.category-name:has-text("${categoryName}"))`)
            .locator('button:has-text("Up"), a:has-text("Up")');
        await moveUpBtn.click();
    }

    async moveCategoryDown(categoryName: string): Promise<void> {
        const moveDownBtn = this.page
            .locator(`div:has(.category-name:has-text("${categoryName}"))`)
            .locator('button:has-text("Down"), a:has-text("Down")');
        await moveDownBtn.click();
    }
}
