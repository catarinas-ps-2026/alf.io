import type { Locator, Page } from "@playwright/test";

// edit.html is shared by both the create and edit routes (same field ids),
// so one page object covers "fill out a new subscription" and "modify an
// existing one".
export class SubscriptionFormPage {
    readonly page: Page;
    readonly titleInput: Locator;
    readonly descriptionInput: Locator;
    readonly termsAndConditionsUrlInput: Locator;
    readonly multiAccessPassType: Locator;
    readonly priceInput: Locator;
    readonly currencyInput: Locator;
    readonly vatStatusSelect: Locator;
    readonly vatInput: Locator;
    readonly maxEntriesInput: Locator;
    readonly logoUploadArea: Locator;
    readonly paypalCheckbox: Locator;
    readonly saveButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.titleInput = page.locator("#title-en");
        this.descriptionInput = page.locator("#description-en");
        this.termsAndConditionsUrlInput = page.locator(
            "#termsAndConditionsUrl",
        );
        this.multiAccessPassType = page.getByRole("link", {
            name: /Multi-Access Pass/,
        });
        this.priceInput = page.locator("#price");
        this.currencyInput = page.locator("#currency");
        this.vatStatusSelect = page.locator("#vatStatus");
        this.vatInput = page.locator("#vat");
        this.maxEntriesInput = page.locator("#maxEntries");
        this.logoUploadArea = page.getByText(
            "Drop image here or click to upload",
        );
        this.paypalCheckbox = page.getByRole("checkbox", { name: "PayPal" });
        this.saveButton = page.getByRole("button", { name: "Save" });
    }

    async waitUntilReady(): Promise<void> {
        await this.titleInput.waitFor({ state: "visible" });
    }

    // Plain .fill() duplicates the https:// prefix on this field, same bug
    // as the Events create form's website/terms URL inputs.
    private async setNativeValue(
        locator: Locator,
        value: string,
    ): Promise<void> {
        await locator.evaluate((el: HTMLInputElement, v: string) => {
            const setter = Object.getOwnPropertyDescriptor(
                window.HTMLInputElement.prototype,
                "value",
            )!.set!;
            setter.call(el, v);
            el.dispatchEvent(new Event("input", { bubbles: true }));
            el.dispatchEvent(new Event("change", { bubbles: true }));
        }, value);
    }

    async uploadLogo(filePath: string): Promise<void> {
        const fileChooserPromise = this.page.waitForEvent("filechooser");
        await this.logoUploadArea.click();
        const fileChooser = await fileChooserPromise;
        await fileChooser.setFiles(filePath);
    }

    async fillBasicInfo(options: {
        title: string;
        description: string;
        termsAndConditionsUrl: string;
    }): Promise<void> {
        await this.titleInput.fill(options.title);
        await this.descriptionInput.fill(options.description);
        await this.setNativeValue(
            this.termsAndConditionsUrlInput,
            options.termsAndConditionsUrl,
        );
    }

    // Price/currency/vat/maxEntries only exist in the DOM once a type
    // (Multi-Access Pass / period / custom) has been picked - edit.html
    // wraps the whole "Pricing and Sales Period" + "Subscription Details"
    // sections in ng-if="$ctrl.preset". Call multiAccessPassType.click()
    // (or another type) before this, not after.
    //
    // Terms and Conditions URL and Taxes % are both required despite
    // looking optional in the UI - found via angular.element(form).scope()
    // form.$error.required inspection when Save silently no-opped.
    async fillPricingInfo(options: {
        price: string;
        currency: string;
        maxEntries: string;
    }): Promise<void> {
        await this.priceInput.fill(options.price);
        await this.currencyInput.fill(options.currency);
        await this.vatStatusSelect.selectOption("string:INCLUDED");
        await this.vatInput.fill("0");
        await this.maxEntriesInput.fill(options.maxEntries);
    }

    async save(): Promise<void> {
        // Save silently no-ops (no request fired) unless at least one
        // payment method is checked - not enforced visually, only inside
        // the controller's save() function.
        if (!(await this.paypalCheckbox.isChecked())) {
            await this.paypalCheckbox.check();
        }
        await this.saveButton.click();
        await this.page.waitForURL((url) => url.href.includes("/edit"));
    }
}
