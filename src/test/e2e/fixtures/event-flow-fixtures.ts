import { test as baseTest, type Browser, type Page } from "@playwright/test";
import {
    createTestEvent,
    deleteTestEvent,
    type TestEvent,
} from "../helpers/api-helper";
import { loginViaUI, completeBasicConfigIfVisible } from "../helpers/auth-helper";

export interface CustomFixtures {
    event: TestEvent;
    adminCredentials: { username: string; password: string };
    authenticatedPage: Page;
}

const ADMIN_USERNAME = process.env.E2E_ADMIN_USERNAME || "admin";
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || "abcd";

export const test = baseTest.extend<CustomFixtures>({
    event: [
        async ({ playwright }, use) => {
            const baseURL =
                baseTest.info().project.use.baseURL || "http://localhost:8080";
            const apiKey = process.env.E2E_SERVER_APIKEY;

            if (!apiKey) {
                throw new Error(
                    "E2E_SERVER_APIKEY is required for event-flow tests",
                );
            }

            const requestContext = await playwright.request.newContext();

            let clientApiKey = apiKey;
            try {
                const orgListResp = await requestContext.get(
                    `${baseURL}/api/v1/admin/system/organization/list`,
                    {
                        headers: { Authorization: `ApiKey ${apiKey}` },
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
                                `Failed to create E2E organization: ${orgCreateResp.status()}`,
                            );
                        }
                        const newOrg = await orgCreateResp.json();
                        orgId = newOrg.id;
                    }

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
                    if (keyResp.ok()) {
                        const keyData = await keyResp.json();
                        clientApiKey = keyData.apiKey;
                    }
                }
            } catch (e) {
                console.error(
                    "Error bootstrapping organization/API key, falling back to original:",
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
        { scope: "worker" },
    ],

    // biome-ignore lint/correctness/noEmptyPattern: Playwright fixture API requires this signature
    adminCredentials: async ({}, use) => {
        await use({
            username: ADMIN_USERNAME,
            password: ADMIN_PASSWORD,
        });
    },

    authenticatedPage: async ({ page, adminCredentials }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";

        await loginViaUI(
            page,
            adminCredentials.username,
            adminCredentials.password,
        );
        await page.waitForURL(/.*(admin).*/);
        await completeBasicConfigIfVisible(page, baseURL);

        await use(page);
    },
});

export { expect } from "@playwright/test";
