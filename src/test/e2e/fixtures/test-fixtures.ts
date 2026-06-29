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
        let clientApiKey = apiKey;

        try {
            // Check if the API key is the system API key by trying to list organizations.
            // If it succeeds, we are authenticated as SYSTEM_API_CLIENT and need to bootstrap.
            const orgListResp = await requestContext.get(
                `${baseURL}/api/v1/admin/system/organization/list`,
                {
                    headers: {
                        Authorization: `ApiKey ${apiKey}`,
                    },
                },
            );

            if (orgListResp.ok()) {
                const orgs = await orgListResp.json();
                let orgId: number;
                if (Array.isArray(orgs) && orgs.length > 0) {
                    orgId = orgs[0].id;
                } else {
                    const orgCreateResp = await requestContext.post(
                        `${baseURL}/api/v1/admin/system/organization/create`,
                        {
                            headers: {
                                "Content-Type": "application/json",
                                Authorization: `ApiKey ${apiKey}`,
                            },
                            data: {
                                name: "E2E Org",
                                email: "e2e@localhost",
                                description: "E2E Org",
                            },
                        },
                    );
                    if (!orgCreateResp.ok()) {
                        throw new Error(
                            `Failed to create E2E organization: ${orgCreateResp.status()} ${await orgCreateResp.text()}`,
                        );
                    }
                    const newOrg = await orgCreateResp.json();
                    orgId = newOrg.id;
                }

                // Generate a client API key for the organization
                const keyResp = await requestContext.put(
                    `${baseURL}/api/v1/admin/system/organization/${orgId}/api-key`,
                    {
                        headers: {
                            "Content-Type": "application/json",
                            Authorization: `ApiKey ${apiKey}`,
                        },
                        data: {
                            apiKeyType: "API_CLIENT",
                            description: "E2E Client Key",
                        },
                    },
                );
                if (!keyResp.ok()) {
                    throw new Error(
                        `Failed to create E2E client API key: ${keyResp.status()} ${await keyResp.text()}`,
                    );
                }
                const keyData = await keyResp.json();
                clientApiKey = keyData.apiKey;
            }
        } catch (e) {
            console.error(
                "Error checking or bootstrapping organization/API key, falling back to original API key:",
                e,
            );
        }

        const event = await createTestEvent(
            requestContext,
            baseURL,
            clientApiKey,
        );

        await use(event);

        try {
            await deleteTestEvent(
                requestContext,
                baseURL,
                clientApiKey,
                event.slug,
            );
        } catch (e) {
            console.error(`Failed to delete test event ${event.slug}:`, e);
        }
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
