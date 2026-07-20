import type { Locator, Page } from "@playwright/test";

/**
 * Componente para modales del admin de alf.io.
 *
 * NOTA: El CRUD principal (orgs, users, eventos) NO usa modales.
 * Usa vistas anidadas in-place via ui-view.
 *
 * Los modales se usan solo para:
 * - Categorías de ticket (TicketCategoryEditorService)
 * - Copiar evento
 * - Rotar API key
 * - Ver QR de API key
 * - Confirmación de eliminar evento (en sidebar)
 */
export class Modal {
    private readonly page: Page;
    private readonly dialog: Locator;
    private readonly title: Locator;
    private readonly body: Locator;
    private readonly confirmButton: Locator;
    private readonly cancelButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.dialog = page.locator(".modal-dialog");
        this.title = page.locator(".modal-title");
        this.body = page.locator(".modal-body");
        this.confirmButton = page.locator(
            ".modal-footer button.btn-success, .modal-footer button.btn-primary",
        );
        this.cancelButton = page.locator(
            '.modal-footer button.btn-default, .modal-footer button[data-dismiss="modal"]',
        );
    }

    async isVisible(): Promise<boolean> {
        try {
            await this.dialog.waitFor({ state: "visible", timeout: 3000 });
            return true;
        } catch {
            return false;
        }
    }

    async getTitle(): Promise<string | null> {
        if (await this.isVisible()) {
            return this.title.textContent();
        }
        return null;
    }

    async getBody(): Promise<string | null> {
        if (await this.isVisible()) {
            return this.body.textContent();
        }
        return null;
    }

    async confirm(): Promise<void> {
        await this.confirmButton.click();
        await this.dialog.waitFor({ state: "hidden", timeout: 5000 });
    }

    async cancel(): Promise<void> {
        await this.cancelButton.click();
        await this.dialog.waitFor({ state: "hidden", timeout: 5000 });
    }
}

/**
 * Maneja $window.confirm() nativo del browser.
 * Se usa para:
 * - Delete user: "The user {username} will be deleted. Are you sure?"
 * - Reset password: "The password for the user {username} will be reset. Are you sure?"
 * - Delete/Disable user
 */
export async function handleNativeConfirm(
    page: Page,
    accept: boolean,
): Promise<void> {
    page.on("dialog", (dialog) => {
        if (accept) {
            dialog.accept();
        } else {
            dialog.dismiss();
        }
    });
}

/**
 * Maneja el diálogo de confirmación del browser y retorna el mensaje.
 */
export async function getConfirmMessage(page: Page): Promise<string | null> {
    return new Promise((resolve) => {
        page.once("dialog", (dialog) => {
            resolve(dialog.message());
            dialog.accept();
        });
    });
}
