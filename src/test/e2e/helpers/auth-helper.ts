import type { APIRequestContext, Page } from "@playwright/test";
import { LoginPage } from "../pages/login.page";
import { AdminPage } from "../pages/admin.page";

export interface UserStatus {
    authenticated: boolean;
    username: string | null;
    version: string;
    demoMode: boolean;
    devMode: boolean;
    prodMode: boolean;
}

export async function loginViaUI(
    page: Page,
    username: string,
    password: string,
): Promise<void> {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(username, password);
}

export async function logoutViaUI(page: Page): Promise<void> {
    const adminPage = new AdminPage(page);
    await adminPage.goto();
    if (await adminPage.isLoggedIn()) {
        await adminPage.logout();
    }
}

export async function getAuthStatus(
    request: APIRequestContext,
    baseURL: string,
): Promise<UserStatus> {
    const response = await request.get(`${baseURL}/authentication-status`);
    if (!response.ok()) {
        throw new Error(
            `Failed to fetch auth status: ${response.status()} ${await response.text()}`,
        );
    }
    return response.json() as Promise<UserStatus>;
}

export async function getCSRFToken(page: Page): Promise<string | null> {
    const csrfInput = page.locator('input[name="_csrf"]');
    if ((await csrfInput.count()) > 0) {
        return csrfInput.getAttribute("value");
    }
    return null;
}

export async function completeBasicConfigIfVisible(
    page: Page,
    baseURL: string,
): Promise<void> {
    const basicConfigHeader = page.locator(
        'h1:has-text("Basic Configuration")',
    );
    try {
        await basicConfigHeader.waitFor({ state: "visible", timeout: 2000 });
        const baseUrlInput = page.getByRole("textbox", {
            name: "Base application url",
        });
        await baseUrlInput.fill(baseURL);
        await page.locator('input[type="radio"][value="disabled"]').click();
        await page.locator('input[type="radio"][value="NONE"]').click();
        await page.locator('button[type="submit"]:has-text("save")').click();
        await basicConfigHeader.waitFor({ state: "detached", timeout: 10000 });
    } catch {
        // Modal not visible, or already completed
    }
}
