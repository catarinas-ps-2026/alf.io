import type { Page } from "@playwright/test";

export async function waitForPageLoad(
    page: Page,
    timeout = 30000,
): Promise<void> {
    await page.waitForLoadState("networkidle", { timeout });
}

export async function waitForUrlContains(
    page: Page,
    pattern: string | RegExp,
    timeout = 10000,
): Promise<void> {
    await page.waitForURL(pattern, { timeout });
}

export async function waitForSelectorVisible(
    page: Page,
    selector: string,
    timeout = 10000,
): Promise<void> {
    await page.locator(selector).waitFor({ state: "visible", timeout });
}

export async function waitForSelectorHidden(
    page: Page,
    selector: string,
    timeout = 10000,
): Promise<void> {
    await page.locator(selector).waitFor({ state: "hidden", timeout });
}

export async function retryAction(
    action: () => Promise<void>,
    maxRetries = 3,
    delayMs = 1000,
): Promise<void> {
    for (let i = 0; i < maxRetries; i++) {
        try {
            await action();
            return;
        } catch (error) {
            if (i === maxRetries - 1) throw error;
            await new Promise((resolve) => setTimeout(resolve, delayMs));
        }
    }
}
