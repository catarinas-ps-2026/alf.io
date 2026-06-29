import { test as baseTest } from "@playwright/test";
import {
    createTestEvent,
    deleteTestEvent,
    type TestEvent,
} from "../helpers/api-helper";

export interface CustomFixtures {
    event: TestEvent | null;
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
});

export { expect } from "@playwright/test";
