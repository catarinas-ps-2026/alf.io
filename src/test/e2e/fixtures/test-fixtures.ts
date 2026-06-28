import { test as baseTest, type Page } from "@playwright/test";
import {
    createTestEvent,
    deleteTestEvent,
    type TestEvent,
} from "../helpers/api-helper";
import {
    loginViaUI,
    completeBasicConfigIfVisible,
} from "../helpers/auth-helper";

export interface Credentials {
    username: string;
    password: string;
}

export interface CustomFixtures {
    event: TestEvent | null;
    adminCredentials: Credentials | null;
    ownerCredentials: Credentials | null;
    supervisorCredentials: Credentials | null;
    authenticatedPage: Page | null;
}

export const test = baseTest.extend<CustomFixtures>({
    event: async ({ playwright }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";
        const apiKey = process.env.E2E_SERVER_APIKEY;

        if (!apiKey) {
            await use(null);
            return;
        }

        const requestContext = await playwright.request.newContext();
        const event = await createTestEvent(requestContext, baseURL, apiKey);

        await use(event);

        await deleteTestEvent(requestContext, baseURL, apiKey, event.slug);
        await requestContext.dispose();
    },

    // biome-ignore lint/correctness/noEmptyPattern: Playwright requires object destructuring pattern for fixtures
    adminCredentials: async ({}, use) => {
        const username = process.env.E2E_ADMIN_USERNAME;
        const password = process.env.E2E_ADMIN_PASSWORD;

        if (!username || !password) {
            await use(null);
        } else {
            await use({ username, password });
        }
    },

    // biome-ignore lint/correctness/noEmptyPattern: Playwright requires object destructuring pattern for fixtures
    ownerCredentials: async ({}, use) => {
        const username = process.env.E2E_OWNER_USERNAME;
        const password = process.env.E2E_OWNER_PASSWORD;

        if (!username || !password) {
            await use(null);
        } else {
            await use({ username, password });
        }
    },

    // biome-ignore lint/correctness/noEmptyPattern: Playwright requires object destructuring pattern for fixtures
    supervisorCredentials: async ({}, use) => {
        const username = process.env.E2E_SUPERVISOR_USERNAME;
        const password = process.env.E2E_SUPERVISOR_PASSWORD;

        if (!username || !password) {
            await use(null);
        } else {
            await use({ username, password });
        }
    },

    authenticatedPage: async ({ page, adminCredentials }, use) => {
        if (!adminCredentials) {
            await use(null);
            return;
        }

        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";

        // Login using administrative credentials
        await loginViaUI(
            page,
            adminCredentials.username,
            adminCredentials.password,
        );

        // Verify login succeeded by checking URL redirection or element
        await page.waitForURL(/.*(admin).*/);

        // Auto-complete basic configuration dialog if visible
        await completeBasicConfigIfVisible(page, baseURL);

        await use(page);

        // Standard cleanup (logout) is handled via clean session, but we can explicitly logout
        // or just let the test context dispose.
    },
});

export { expect } from "@playwright/test";
