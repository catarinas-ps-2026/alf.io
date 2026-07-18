import { test as baseTest, type Page } from "@playwright/test";
import {
    createTestEvent,
    deleteTestEvent,
    type TestEvent,
} from "../helpers/api-helper";
import { type Credentials, loginAs } from "../helpers/auth-helper";

export interface AuthFixtures {
    event: TestEvent | null;
    adminCredentials: Credentials;
    ownerCredentials: Credentials;
    supervisorCredentials: Credentials;
    authenticatedPage: Page;
    adminPage: Page;
    ownerPage: Page;
    supervisorPage: Page;
}

// Credenciales centralizadas (Priorizan variables de entorno, usan valores por defecto locales)
const ADMIN_USERNAME = process.env.E2E_ADMIN_USERNAME || "admin";
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || "Admin-1234";

const OWNER_USERNAME = process.env.E2E_OWNER_USERNAME || "owner-e2e";
const OWNER_PASSWORD = process.env.E2E_OWNER_PASSWORD || "abcd";

const SUPERVISOR_USERNAME =
    process.env.E2E_SUPERVISOR_USERNAME || "supervisor-e2e";
const SUPERVISOR_PASSWORD = process.env.E2E_SUPERVISOR_PASSWORD || "abcd";

const DEFAULT_SERVER_API_KEY = "e2e-test-api-key";

export const test = baseTest.extend<AuthFixtures>({
    // Fixture para inyectar un Evento creado vía API y eliminarlo automáticamente al finalizar el test
    event: async ({ playwright }, use) => {
        const baseURL =
            baseTest.info().project.use.baseURL || "http://localhost:8080";
        const apiKey = process.env.E2E_SERVER_APIKEY || DEFAULT_SERVER_API_KEY;

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
                if (!keyResp.ok()) {
                    throw new Error(
                        `Failed to create E2E client API key: ${keyResp.status()}`,
                    );
                }
                const keyData = await keyResp.json();
                clientApiKey = keyData.apiKey;
            }
        } catch (e) {
            console.error(
                "Error bootstrapping organization/API key, falling back to original API key:",
                e,
            );
        }

        // Crear el evento antes de que inicie la prueba
        const event = await createTestEvent(
            requestContext,
            baseURL,
            clientApiKey,
        );

        // Entregar el objeto "event" al archivo de pruebas
        await use(event);

        // Limpieza automática (Teardown): se ejecuta justo después de que termine tu test
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

    // Fixtures de Credenciales puras
    adminCredentials: async ({}, use) => {
        await use({ username: ADMIN_USERNAME, password: ADMIN_PASSWORD });
    },

    ownerCredentials: async ({}, use) => {
        await use({ username: OWNER_USERNAME, password: OWNER_PASSWORD });
    },

    supervisorCredentials: async ({}, use) => {
        await use({
            username: SUPERVISOR_USERNAME,
            password: SUPERVISOR_PASSWORD,
        });
    },

    // Fixtures que devuelven el objeto 'page' con la sesión ya iniciada en el navegador
    authenticatedPage: async ({ page, adminCredentials, baseURL }, use) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );
        await use(page);
    },

    adminPage: async ({ page, adminCredentials, baseURL }, use) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );
        await use(page);
    },

    ownerPage: async ({ page, ownerCredentials, baseURL }, use) => {
        await loginAs(
            page,
            ownerCredentials,
            baseURL || "http://localhost:8080",
        );
        await use(page);
    },

    supervisorPage: async ({ page, supervisorCredentials, baseURL }, use) => {
        await loginAs(
            page,
            supervisorCredentials,
            baseURL || "http://localhost:8080",
        );
        await use(page);
    },
});

export { expect } from "@playwright/test";
