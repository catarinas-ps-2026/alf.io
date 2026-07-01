import { expect, test } from "../../fixtures/auth";
import { loginAs } from "../../flows/auth";
import { NavigationComponent } from "../../pages/navigation/NavigationComponent";

test.describe("Admin Panel: Navigation", () => {
    test("should display limited navigation for owner user", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            ownerCredentials,
            baseURL || "http://localhost:8080",
        );

        const nav = new NavigationComponent(page);
        const modules = [
            { goto: () => nav.gotoEvents(), url: /.*admin#?.*/ },
            { goto: () => nav.gotoSubscriptions(), url: /.*subscriptions.*/ },
            { goto: () => nav.gotoOrganizations(), url: /.*organizations.*/ },
            { goto: () => nav.gotoUsers(), url: /.*users.*/ },
            { goto: () => nav.gotoApiKeys(), url: /.*api-keys.*/ },
            { goto: () => nav.gotoGroups(), url: /.*group.*/ },
            { goto: () => nav.gotoConfiguration(), url: /.*configuration.*/ },
        ];

        for (const module of modules) {
            await module.goto();
            await expect(page).toHaveURL(module.url);
        }
    });

    test("should navigate between all main admin modules using the navigation bar", async ({
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
        const modules = [
            { goto: () => nav.gotoEvents(), url: /.*admin#?.*/ },
            { goto: () => nav.gotoSubscriptions(), url: /.*subscriptions.*/ },
            { goto: () => nav.gotoOrganizations(), url: /.*organizations.*/ },
            { goto: () => nav.gotoUsers(), url: /.*users.*/ },
            { goto: () => nav.gotoApiKeys(), url: /.*api-keys.*/ },
            { goto: () => nav.gotoGroups(), url: /.*group.*/ },
            { goto: () => nav.gotoConfiguration(), url: /.*configuration.*/ },
            { goto: () => nav.gotoExtension(), url: /.*extension.*/ },
        ];

        for (const module of modules) {
            await module.goto();
            await expect(page).toHaveURL(module.url);
        }
    });
});
