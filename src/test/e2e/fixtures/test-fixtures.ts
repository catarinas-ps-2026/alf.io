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
        const event = await createTestEvent(requestContext, baseURL, apiKey);

        await use(event);

        await deleteTestEvent(requestContext, baseURL, apiKey, event.slug);
        await requestContext.dispose();
    },
});

export { expect } from "@playwright/test";
