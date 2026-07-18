import { type Browser, test as baseTest, type Page } from "@playwright/test";
import {
    createTestEvent,
    deleteTestEvent,
    type TestEvent,
} from "../helpers/api-helper";
import {
    type Credentials,
    completeBasicConfigIfVisible,
    createUserViaPage,
    ensureOrganizationExists,
    findUserByUsername,
    loginAs,
    loginViaUI,
    resetUserPassword,
} from "../helpers/auth-helper";

export type { Credentials };

export interface CustomFixtures {
    event: TestEvent | null;
    adminCredentials: Credentials;
    ownerCredentials: Credentials;
    supervisorCredentials: Credentials;
    authenticatedPage: Page | null;
    adminPage: Page | null;
    ownerPage: Page | null;
    supervisorPage: Page | null;
}

const ADMIN_USERNAME = process.env.E2E_ADMIN_USERNAME || "admin";
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || "Admin-1234";
const OWNER_USERNAME = process.env.E2E_OWNER_USERNAME || "owner-e2e";
const OWNER_PASSWORD = process.env.E2E_OWNER_PASSWORD || "abcd";
const SUPERVISOR_USERNAME =
    process.env.E2E_SUPERVISOR_USERNAME || "supervisor-e2e";
const SUPERVISOR_PASSWORD = process.env.E2E_SUPERVISOR_PASSWORD || "abcd";

let cachedOwnerPassword = OWNER_PASSWORD;
let cachedSupervisorPassword = SUPERVISOR_PASSWORD;
let seedingPromise: Promise<{
    ownerPass: string;
    supervisorPass: string;
}> | null = null;

async function performSeeding(browser: Browser, baseURL: string) {
    const context = await browser.newContext();
    const page = await context.newPage();
    try {
        await loginViaUI(page, ADMIN_USERNAME, ADMIN_PASSWORD);
        await page.waitForURL(/.*(admin).*/);
        await completeBasicConfigIfVisible(page, baseURL);
        const orgId = await ensureOrganizationExists(page);

        let ownerPass = OWNER_PASSWORD;
        try {
            const created = await createUserViaPage(page, {
                organizationId: orgId,
                username: OWNER_USERNAME,
                firstName: "Test",
                lastName: "Owner",
                emailAddress: `${OWNER_USERNAME}@e2e.test`,
                role: "OWNER",
            });
            ownerPass = created.password;
        } catch {
            const existing = await findUserByUsername(page, OWNER_USERNAME);
            if (existing) {
                ownerPass = await resetUserPassword(page, existing.id);
            }
        }

        let supervisorPass = SUPERVISOR_PASSWORD;
        try {
            const created = await createUserViaPage(page, {
                organizationId: orgId,
                username: SUPERVISOR_USERNAME,
                firstName: "Test",
                lastName: "Supervisor",
                emailAddress: `${SUPERVISOR_USERNAME}@e2e.test`,
                role: "SUPERVISOR",
            });
            supervisorPass = created.password;
        } catch {
            const existing = await findUserByUsername(
                page,
                SUPERVISOR_USERNAME,
            );
            if (existing) {
                supervisorPass = await resetUserPassword(page, existing.id);
            }
        }

        cachedOwnerPassword = ownerPass;
        cachedSupervisorPassword = supervisorPass;
    } finally {
        await page.close();
        await context.close();
    }
}

function seedUsersIfNeeded(browser: Browser, baseURL: string) {
    if (!seedingPromise) {
        seedingPromise = performSeeding(browser, baseURL).then(() => {
            return {
                ownerPass: cachedOwnerPassword,
                supervisorPass: cachedSupervisorPassword,
            };
        });
    }
    return seedingPromise;
}

export const test = baseTest.extend<CustomFixtures>({
    event: async ({ playwright }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";
        const apiKey =
            process.env.E2E_SERVER_APIKEY ||
            process.env.ALFIO_OVERRIDE_SYSTEM_SETTINGS_SYSTEM_API_KEY ||
            "e2e-test-api-key";

        const requestContext = await playwright.request.newContext();
        let clientApiKey = apiKey;

        try {
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

    // biome-ignore lint/correctness/noEmptyPattern: Playwright fixture API requires this signature
    adminCredentials: async ({}, use) => {
        await use({
            username: ADMIN_USERNAME,
            password: ADMIN_PASSWORD,
        });
    },

    ownerCredentials: async ({ browser }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";
        const { ownerPass } = await seedUsersIfNeeded(browser, baseURL);
        await use({
            username: OWNER_USERNAME,
            password: ownerPass,
        });
    },

    supervisorCredentials: async ({ browser }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";
        const { supervisorPass } = await seedUsersIfNeeded(browser, baseURL);
        await use({
            username: SUPERVISOR_USERNAME,
            password: supervisorPass,
        });
    },

    authenticatedPage: async ({ page, adminCredentials, browser }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";

        try {
            await seedUsersIfNeeded(browser, baseURL);

            await loginAs(page, adminCredentials, baseURL);

            await use(page);
        } finally {
        }
    },

    adminPage: async ({ page, adminCredentials, browser }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";

        try {
            await seedUsersIfNeeded(browser, baseURL);
            await loginAs(page, adminCredentials, baseURL);
            await use(page);
        } finally {
        }
    },

    ownerPage: async ({ page, ownerCredentials, browser }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";

        try {
            await seedUsersIfNeeded(browser, baseURL);
            await loginAs(page, ownerCredentials, baseURL);
            await use(page);
        } finally {
        }
    },

    supervisorPage: async ({ page, supervisorCredentials, browser }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";

        try {
            await seedUsersIfNeeded(browser, baseURL);
            await loginAs(page, supervisorCredentials, baseURL);
            await use(page);
        } finally {
        }
    },
});

export { expect } from "@playwright/test";
