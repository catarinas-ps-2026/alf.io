import { expect, test } from "../../../fixtures/auth";
import { loginAs } from "../../../flows/auth";
import { ConfigurationPage } from "../../../pages/configuration/ConfigurationPage";
import { EventsPage } from "../../../pages/events";
import { NavigationComponent } from "../../../pages/navigation/NavigationComponent";

test.describe("Authorization", () => {
    test("admin can open the system configuration section", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const nav = new NavigationComponent(page);
        await nav.gotoConfiguration();

        const configuration = new ConfigurationPage(page);
        expect(await configuration.isSystemConfigVisible()).toBe(true);
    });

    test("organization owner cannot access system configuration and sees an access-denied message", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            ownerCredentials,
            baseURL || "http://localhost:8080",
        );

        const configuration = new ConfigurationPage(page);
        await configuration.gotoSystemConfiguration();

        expect(await configuration.isAccessDeniedVisible()).toBe(true);
    });

    test("check-in supervisor cannot access system configuration either", async ({
        page,
        supervisorCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            supervisorCredentials,
            baseURL || "http://localhost:8080",
        );

        const configuration = new ConfigurationPage(page);
        await configuration.gotoSystemConfiguration();

        expect(await configuration.isAccessDeniedVisible()).toBe(true);
    });

    test("organization owner can start creating an event", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            ownerCredentials,
            baseURL || "http://localhost:8080",
        );

        const events = new EventsPage(page);
        await events.goto();
        expect(await events.isCreateEventLinkVisible()).toBe(true);

        await events.openCreateEventForm();
        expect(await events.isCreateEventFormVisible()).toBe(true);
    });

    test("check-in supervisor can view an event's details but cannot create events", async ({
        page,
        supervisorCredentials,
        event,
        baseURL,
    }) => {
        test.skip(!event, "Skipping test: Requires a test event.");
        if (!event) return;

        await loginAs(
            page,
            supervisorCredentials,
            baseURL || "http://localhost:8080",
        );

        const events = new EventsPage(page);
        await events.goto();
        expect(await events.isCreateEventLinkVisible()).toBe(false);

        await events.gotoEventDetail(event.slug);
        expect(await events.isEventDetailVisible()).toBe(true);
    });
});
