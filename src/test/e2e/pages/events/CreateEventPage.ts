import type { Locator, Page } from "@playwright/test";

// The create-event form requires, in order: basic info (including a
// mandatory logo image - the "Event logo is missing!" text under the
// upload widget is a real blocking validation error, not a hint), seats
// and payment info, and at least one ticket category before Save succeeds.
export class CreateEventPage {
    readonly page: Page;
    readonly displayNameInput: Locator;
    readonly organizationSelect: Locator;
    readonly locationInput: Locator;
    readonly descriptionInput: Locator;
    readonly shortNameInput: Locator;
    readonly websiteUrlInput: Locator;
    readonly termsAndConditionsUrlInput: Locator;
    readonly availableSeatsInput: Locator;
    readonly regularPriceInput: Locator;
    readonly currencyInput: Locator;
    readonly vatPercentageInput: Locator;
    readonly onSitePaymentCheckbox: Locator;
    readonly logoUploadArea: Locator;
    readonly logoMissingWarning: Locator;
    readonly addCategoryButton: Locator;
    readonly organizerRequiredMessage: Locator;
    readonly saveButton: Locator;
    readonly cancelButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.displayNameInput = page.locator("#displayName");
        this.organizationSelect = page.locator("#organizationId");
        this.locationInput = page.locator("#location");
        this.descriptionInput = page.locator("#description");
        this.shortNameInput = page.locator("#shortName");
        this.websiteUrlInput = page.locator("#websiteUrl");
        this.termsAndConditionsUrlInput = page.locator(
            "#termsAndConditionsUrl",
        );
        this.availableSeatsInput = page.locator("#availableSeats");
        this.regularPriceInput = page.locator("#regularPrice");
        this.currencyInput = page.getByRole("textbox", { name: "Currency" });
        this.vatPercentageInput = page.locator("#vatPercentage");
        this.onSitePaymentCheckbox = page.getByRole("checkbox", {
            name: "On site (cash) payment",
        });
        this.logoUploadArea = page.getByText(
            "Drop image here or click to upload",
        );
        this.logoMissingWarning = page.getByText("Event logo is missing!");
        this.addCategoryButton = page
            .getByRole("button", { name: "Add new" })
            .first();
        this.organizerRequiredMessage = page.getByText(
            "Please select an Organizer to continue",
        );
        this.saveButton = page.getByRole("button", { name: "Save" }).first();
        this.cancelButton = page
            .getByRole("button", { name: "Cancel" })
            .first();
    }

    async waitUntilReady(): Promise<void> {
        await this.displayNameInput.waitFor({ state: "visible" });
    }

    async getFirstAvailableOrganization(): Promise<string> {
        await this.page.waitForFunction(() => {
            const sel = document.querySelector(
                "#organizationId",
            ) as HTMLSelectElement | null;
            return (
                !!sel &&
                Array.from(sel.options).some((o) => o.text.trim() !== "")
            );
        });
        const labels = await this.organizationSelect
            .locator("option")
            .allTextContents();
        const org = labels.map((l) => l.trim()).filter(Boolean)[0];
        if (!org) throw new Error("No organizations available in dropdown");
        return org;
    }

    async isOrganizerRequiredMessageVisible(): Promise<boolean> {
        return this.organizerRequiredMessage.isVisible();
    }

    async selectOrganization(name: string): Promise<void> {
        // AngularJS ng-options populates <option> elements asynchronously
        // after the controller loads organizations from the API. Wait for
        // the specific option to be present before attempting selection.
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
            name,
        );
        await this.organizationSelect.selectOption({ label: name });
        // Selecting an organizer reveals the rest of the form (seats,
        // payment, categories) - wait for it before filling further fields.
        await this.availableSeatsInput.waitFor({ state: "visible" });
    }

    // Angular's own binding on these two url fields doesn't clear a
    // pre-filled default on a plain .fill() - dispatching via the native
    // input value setter avoids ending up with a duplicated prefix.
    private async setNativeValue(
        locator: Locator,
        value: string,
    ): Promise<void> {
        await locator.evaluate((el: HTMLInputElement, val: string) => {
            const nativeSetter = Object.getOwnPropertyDescriptor(
                window.HTMLInputElement.prototype,
                "value",
            )!.set!;
            nativeSetter.call(el, val);
            el.dispatchEvent(new Event("input", { bubbles: true }));
            el.dispatchEvent(new Event("change", { bubbles: true }));
        }, value);
    }

    async fillBasicInfo(data: {
        displayName: string;
        location: string;
        description: string;
        shortName: string;
        websiteUrl: string;
        termsAndConditionsUrl: string;
    }): Promise<void> {
        await this.displayNameInput.fill(data.displayName);
        await this.locationInput.fill(data.location);
        await this.descriptionInput.fill(data.description);
        await this.shortNameInput.fill(data.shortName);
        await this.setNativeValue(this.websiteUrlInput, data.websiteUrl);
        await this.setNativeValue(
            this.termsAndConditionsUrlInput,
            data.termsAndConditionsUrl,
        );
    }

    async fillSeatsAndPayment(data: {
        availableSeats: string;
        regularPrice: string;
        currency: string;
        vatPercentage: string;
    }): Promise<void> {
        await this.availableSeatsInput.fill(data.availableSeats);
        await this.regularPriceInput.fill(data.regularPrice);
        await this.currencyInput.fill(data.currency);
        await this.vatPercentageInput.fill(data.vatPercentage);
        await this.onSitePaymentCheckbox.check();
    }

    async uploadLogo(filePath: string): Promise<void> {
        const fileChooserPromise = this.page.waitForEvent("filechooser");
        await this.logoUploadArea.click();
        const fileChooser = await fileChooserPromise;
        await fileChooser.setFiles(filePath);
    }

    // The category dialog's other fields (dates, price, allocation
    // strategy) come pre-filled with sensible defaults derived from the
    // event's own dates/price - only the name is required.
    async addCategory(name: string): Promise<void> {
        await this.addCategoryButton.click();
        const dialog = this.page.getByRole("dialog");
        await dialog.locator("#name").fill(name);
        await dialog.getByRole("button", { name: "Save" }).click();
        await dialog.waitFor({ state: "hidden" });
    }

    private async dismissDevModeBanner(): Promise<void> {
        const closeButton = this.page.getByRole("button", { name: "x" });
        if (await closeButton.isVisible().catch(() => false)) {
            await closeButton.click();
        }
    }

    // Resolves once the app navigates to the new event's detail page,
    // which only happens after a real successful creation.
    async save(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.saveButton.click();
        await this.page.waitForURL(/\/events\/[^/]+\/detail/);
    }

    async cancel(): Promise<void> {
        await this.dismissDevModeBanner();
        await this.cancelButton.click();
    }
}
