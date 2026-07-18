import type { Page } from "@playwright/test";
import type { Credentials } from "../helpers/auth-helper";
import {
    completeBasicConfigIfVisible,
    isSessionActive,
    loginAs as loginAsHelper,
    logoutViaUI as logoutViaUIHelper,
} from "../helpers/auth-helper";
import { AdminPage } from "../pages/admin.page";

export interface AuthFlowResult {
    success: boolean;
    error?: string;
}

export async function loginAs(
    page: Page,
    credentials: Credentials,
    baseURL: string,
    options?: { dismissConfig?: boolean },
): Promise<AuthFlowResult> {
    try {
        await loginAsHelper(page, credentials, baseURL, options);
        const adminPage = new AdminPage(page);
        const isLoggedIn = await adminPage.isLoggedIn();
        return { success: isLoggedIn };
    } catch (error) {
        return {
            success: false,
            error: error instanceof Error ? error.message : "Unknown error",
        };
    }
}

export async function logoutViaUI(page: Page): Promise<AuthFlowResult> {
    try {
        await logoutViaUIHelper(page);
        const sessionActive = await isSessionActive(page);
        return { success: !sessionActive };
    } catch (error) {
        return {
            success: false,
            error: error instanceof Error ? error.message : "Unknown error",
        };
    }
}

export async function verifySessionActive(page: Page): Promise<boolean> {
    return isSessionActive(page);
}

export async function verifyRedirectToLogin(
    page: Page,
    targetUrl: string,
): Promise<boolean> {
    await page.goto(targetUrl);
    const currentUrl = page.url();
    return (
        currentUrl.includes("login") || currentUrl.includes("authentication")
    );
}

export async function verifyAdminAccess(page: Page): Promise<boolean> {
    const adminPage = new AdminPage(page);
    return adminPage.isLoggedIn();
}

export async function loginAndVerifyFullFlow(
    page: Page,
    credentials: Credentials,
    baseURL: string,
): Promise<AuthFlowResult> {
    const loginResult = await loginAs(page, credentials, baseURL);
    if (!loginResult.success) return loginResult;

    const sessionActive = await verifySessionActive(page);
    if (!sessionActive) {
        return { success: false, error: "Session not active after login" };
    }

    const adminAccess = await verifyAdminAccess(page);
    if (!adminAccess) {
        return { success: false, error: "Admin access not verified" };
    }

    return { success: true };
}

export async function fullAuthLifecycle(
    page: Page,
    credentials: Credentials,
    baseURL: string,
): Promise<AuthFlowResult> {
    const loginResult = await loginAndVerifyFullFlow(
        page,
        credentials,
        baseURL,
    );
    if (!loginResult.success) return loginResult;

    const logoutResult = await logoutViaUI(page);
    if (!logoutResult.success) return logoutResult;

    const sessionAfterLogout = await verifySessionActive(page);
    if (sessionAfterLogout) {
        return { success: false, error: "Session still active after logout" };
    }

    return { success: true };
}
