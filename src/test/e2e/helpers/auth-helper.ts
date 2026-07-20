import type { APIRequestContext, Page } from "@playwright/test";
import { AdminPage } from "../pages/admin.page";
import { LoginPage } from "../pages/login.page";

export interface UserStatus {
    authenticated: boolean;
    username: string | null;
    version: string;
    demoMode: boolean;
    devMode: boolean;
    prodMode: boolean;
}

export interface Credentials {
    username: string;
    password: string;
}

export interface CreatedUser {
    id: number;
    username: string;
    password: string;
}

export async function loginViaUI(
    page: Page,
    username: string,
    password: string,
): Promise<void> {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(username, password);
    await page.waitForURL(/.*(admin).*/);
}

export async function loginAs(
    page: Page,
    credentials: Credentials,
    baseURL: string,
    options?: { dismissConfig?: boolean },
): Promise<void> {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(credentials.username, credentials.password);
    await page.waitForURL(/.*(admin).*/);
    const status = await getCurrentUser(page);
    if (!status?.authenticated || status.username !== credentials.username) {
        throw new Error(
            `loginAs("${credentials.username}") did not produce an authenticated session for that user. ` +
                `authentication-status returned: ${JSON.stringify(status)}`,
        );
    }

    if (options?.dismissConfig !== false) {
        await completeBasicConfigIfVisible(page, baseURL);
    }
}

export async function logoutViaUI(page: Page): Promise<void> {
    const adminPage = new AdminPage(page);
    await adminPage.goto();
    if (await adminPage.isLoggedIn()) {
        await adminPage.logout();
    }
}

export async function isSessionActive(page: Page): Promise<boolean> {
    try {
        const statusResponse = await page.request.get("/authentication-status");
        if (!statusResponse.ok()) return false;
        const status = await statusResponse.json();
        return status.authenticated === true;
    } catch {
        return false;
    }
}

export async function getCurrentUser(page: Page): Promise<UserStatus | null> {
    try {
        const statusResponse = await page.request.get("/authentication-status");
        if (!statusResponse.ok()) return null;
        return await statusResponse.json();
    } catch {
        return null;
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
        await basicConfigHeader.waitFor({ state: "visible", timeout: 800 });
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

function getBaseURL(page: Page): string {
    const url = new URL(page.url());
    return url.origin;
}

// Organizations have no delete action in the admin UI, so tests that create
// one via the UI must clean up through the system API key instead.
export async function deleteOrganizationViaApi(
    page: Page,
    organizationId: number,
): Promise<void> {
    const baseURL = getBaseURL(page);
    const apiKey = process.env.E2E_SERVER_APIKEY || "e2e-test-api-key";
    await page.request.delete(
        `${baseURL}/api/v1/admin/system/organization/${organizationId}`,
        { headers: { Authorization: `ApiKey ${apiKey}` } },
    );
}

export async function getCurrentUserId(page: Page): Promise<number> {
    const baseURL = getBaseURL(page);
    const resp = await page.evaluate(async (url) => {
        const r = await fetch(`${url}/admin/api/users/current`);
        if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
        return r.json();
    }, baseURL);
    return resp.id;
}

export async function resetUserPassword(
    page: Page,
    userId: number,
): Promise<string> {
    const baseURL = getBaseURL(page);
    const resp = await page.evaluate(
        async ({ url, id }) => {
            function getCsrfToken(): string {
                const match = document.cookie.match(
                    /(?:^|; )XSRF-TOKEN=([^;]*)/,
                );
                return match ? decodeURIComponent(match[1]) : "";
            }
            const r = await fetch(
                `${url}/admin/api/users/${id}/reset-password?baseUrl=${encodeURIComponent(url)}`,
                {
                    method: "PUT",
                    headers: { "X-CSRF-TOKEN": getCsrfToken() },
                },
            );
            if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
            return r.json();
        },
        { url: baseURL, id: userId },
    );
    return resp.password;
}

export async function createUserViaPage(
    page: Page,
    user: {
        organizationId: number;
        username: string;
        firstName: string;
        lastName: string;
        emailAddress: string;
        role: string;
        type?: string;
    },
): Promise<CreatedUser> {
    const baseURL = getBaseURL(page);
    return page.evaluate(
        async ({ url, userData }) => {
            function getCsrfToken(): string {
                const match = document.cookie.match(
                    /(?:^|; )XSRF-TOKEN=([^;]*)/,
                );
                return match ? decodeURIComponent(match[1]) : "";
            }
            const r = await fetch(
                `${url}/admin/api/users/new?baseUrl=${encodeURIComponent(url)}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-TOKEN": getCsrfToken(),
                    },
                    body: JSON.stringify({
                        organizationId: userData.organizationId,
                        username: userData.username,
                        firstName: userData.firstName,
                        lastName: userData.lastName,
                        emailAddress: userData.emailAddress,
                        role: userData.role,
                        type: userData.type ?? "INTERNAL",
                    }),
                },
            );
            if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
            return r.json();
        },
        { url: baseURL, userData: user },
    );
}

export async function createOrResetUser(
    page: Page,
    user: {
        organizationId: number;
        username: string;
        firstName: string;
        lastName: string;
        emailAddress: string;
        role: string;
        type?: string;
    },
): Promise<string> {
    try {
        const created = await createUserViaPage(page, user);
        return created.password;
    } catch {
        const existing = await findUserByUsername(page, user.username);
        if (existing) {
            return resetUserPassword(page, existing.id);
        }
        throw new Error(
            `Could not create user "${user.username}" and no existing user with that name was found either.`,
        );
    }
}

export async function deleteUserViaPage(
    page: Page,
    userId: number,
): Promise<void> {
    const baseURL = getBaseURL(page);
    await page.evaluate(
        async ({ url, id }) => {
            function getCsrfToken(): string {
                const match = document.cookie.match(
                    /(?:^|; )XSRF-TOKEN=([^;]*)/,
                );
                return match ? decodeURIComponent(match[1]) : "";
            }
            const r = await fetch(`${url}/admin/api/users/${id}`, {
                method: "DELETE",
                headers: { "X-CSRF-TOKEN": getCsrfToken() },
            });
            if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
        },
        { url: baseURL, id: userId },
    );
}

export async function createOrganizationViaPage(
    page: Page,
    name: string,
): Promise<number> {
    const baseURL = getBaseURL(page);
    const resp = await page.evaluate(
        async ({ url, orgName }) => {
            function getCsrfToken(): string {
                const match = document.cookie.match(
                    /(?:^|; )XSRF-TOKEN=([^;]*)/,
                );
                return match ? decodeURIComponent(match[1]) : "";
            }
            const r = await fetch(`${url}/admin/api/organizations/new`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-TOKEN": getCsrfToken(),
                },
                body: JSON.stringify({
                    name: orgName,
                    email: `${orgName.toLowerCase().replace(/\s+/g, "-")}@e2e.test`,
                    description: `E2E test organization: ${orgName}`,
                }),
            });
            if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
            return r.json();
        },
        { url: baseURL, orgName: name },
    );
    return resp.id;
}

export async function getOrganizationIdViaPage(page: Page): Promise<number> {
    const baseURL = getBaseURL(page);
    const resp = await page.evaluate(async (url) => {
        const r = await fetch(`${url}/admin/api/organizations`);
        if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
        return r.json();
    }, baseURL);
    if (Array.isArray(resp) && resp.length > 0) {
        return resp[0].id;
    }
    throw new Error("No organizations found");
}

export async function findUserByUsername(
    page: Page,
    username: string,
): Promise<{ id: number; username: string } | null> {
    const baseURL = getBaseURL(page);
    const resp = await page.evaluate(
        async ({ url, name }) => {
            const r = await fetch(`${url}/admin/api/users`);
            if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
            const users = await r.json();
            return (
                users.find((u: { username: string }) => u.username === name) ||
                null
            );
        },
        { url: baseURL, name: username },
    );
    return resp;
}

const E2E_ORG_NAME = "E2E Org";

export async function ensureE2eOrgExists(
    request: APIRequestContext,
): Promise<void> {
    const baseURL =
        process.env.ALFIO_OVERRIDE_SYSTEM_SETTINGS_BASE_URL ||
        "http://localhost:8080";
    const apiKey = process.env.E2E_SERVER_APIKEY || "e2e-test-api-key";
    const listResp = await request.get(
        `${baseURL}/api/v1/admin/system/organization/list`,
        { headers: { Authorization: `ApiKey ${apiKey}` } },
    );
    if (listResp.ok()) {
        const orgs = await listResp.json();
        if (
            Array.isArray(orgs) &&
            orgs.some((o: { name: string }) => o.name === E2E_ORG_NAME)
        ) {
            return;
        }
    }
    await request.post(
        `${baseURL}/api/v1/admin/system/organization/create`,
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `ApiKey ${apiKey}`,
            },
            data: {
                name: E2E_ORG_NAME,
                email: "e2e@localhost",
                description: "E2E Org",
            },
        },
    );
}

export async function ensureOrganizationExists(page: Page): Promise<number> {
    const baseURL = getBaseURL(page);
    const orgName = `E2E Org ${Date.now()}`;
    await page.evaluate(
        async ({ url, name }) => {
            function getCsrfToken(): string {
                const match = document.cookie.match(
                    /(?:^|; )XSRF-TOKEN=([^;]*)/,
                );
                return match ? decodeURIComponent(match[1]) : "";
            }
            const r = await fetch(`${url}/admin/api/organizations/new`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-TOKEN": getCsrfToken(),
                },
                body: JSON.stringify({
                    name,
                    email: `${name.toLowerCase().replace(/\s+/g, "-")}@e2e.test`,
                    description: `E2E test organization: ${name}`,
                }),
            });
            if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
        },
        { url: baseURL, name: orgName },
    );
    const resp = await page.evaluate(async (url) => {
        const r = await fetch(`${url}/admin/api/organizations`);
        if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
        return r.json();
    }, baseURL);
    if (Array.isArray(resp) && resp.length > 0) {
        return resp[resp.length - 1].id;
    }
    throw new Error("No organizations found after creation");
}

export async function createDisposableUser(
    page: Page,
    role: string,
): Promise<CreatedUser> {
    const orgId = await getOrganizationIdViaPage(page);
    const username = `e2e-${role.toLowerCase()}-${Date.now()}`;
    return createUserViaPage(page, {
        organizationId: orgId,
        username,
        firstName: "Test",
        lastName: role.charAt(0) + role.slice(1).toLowerCase(),
        emailAddress: `${username}@e2e.test`,
        role,
    });
}

export async function withDisposableUser(
    page: Page,
    adminCredentials: Credentials,
    baseURL: string,
    role: string,
    run: (credentials: Credentials) => Promise<void>,
): Promise<void> {
    await loginAs(page, adminCredentials, baseURL);
    const created = await createDisposableUser(page, role);
    const credentials = {
        username: created.username,
        password: created.password,
    };
    try {
        await logoutViaUI(page);
        await loginAs(page, credentials, baseURL);
        await run(credentials);
    } finally {
        await logoutViaUI(page);
        await loginAs(page, adminCredentials, baseURL);
        await deleteUserViaPage(page, created.id);
    }
}
