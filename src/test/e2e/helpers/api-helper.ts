import * as fs from "node:fs";
import * as path from "node:path";
import type { APIRequestContext } from "@playwright/test";

const TEMPLATE_PATH = path.resolve(
    __dirname,
    "../../resources/e2e/create-event-for-e2e.json",
);

export interface TestEvent {
    slug: string;
    url: string;
}

export async function createTestEvent(
    request: APIRequestContext,
    baseURL: string,
    apiKey: string,
): Promise<TestEvent> {
    const slug = `e2e-${Math.random().toString(36).substring(2, 11)}`;
    const now = new Date();

    const formatDate = (date: Date) => date.toISOString().split(".")[0];

    const startSelling = formatDate(
        new Date(now.getTime() - 24 * 60 * 60 * 1000),
    );
    const eventStart = formatDate(
        new Date(now.getTime() + 2 * 24 * 60 * 60 * 1000),
    );
    const eventEnd = formatDate(
        new Date(now.getTime() + (2 * 24 * 60 + 120) * 60 * 1000),
    );

    if (!fs.existsSync(TEMPLATE_PATH)) {
        throw new Error(`Event template JSON not found at ${TEMPLATE_PATH}`);
    }

    let templateContent = fs.readFileSync(TEMPLATE_PATH, "utf8");
    templateContent = templateContent
        .replace(/--SLUG--/g, slug)
        .replace(/--CATEGORY_START_SELLING--/g, startSelling)
        .replace(/--EVENT_START_DATE--/g, eventStart)
        .replace(/--EVENT_END_DATE--/g, eventEnd);

    const response = await request.post(
        `${baseURL}/api/v1/admin/event/create`,
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `ApiKey ${apiKey}`,
            },
            data: JSON.parse(templateContent),
        },
    );

    if (!response.ok()) {
        throw new Error(
            `Failed to create test event: ${response.status()} ${await response.text()}`,
        );
    }

    return {
        slug,
        url: `${baseURL}/event/${slug}`,
    };
}

export async function deleteTestEvent(
    request: APIRequestContext,
    baseURL: string,
    apiKey: string,
    slug: string,
): Promise<void> {
    const response = await request.delete(
        `${baseURL}/api/v1/admin/event/${slug}`,
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `ApiKey ${apiKey}`,
            },
        },
    );

    if (!response.ok()) {
        throw new Error(
            `Failed to delete test event ${slug}: ${response.status()} ${await response.text()}`,
        );
    }
}
