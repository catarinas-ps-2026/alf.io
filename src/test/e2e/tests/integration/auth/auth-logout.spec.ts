import { expect, test } from "../../../fixtures/auth";
import { loginAs, logoutViaUI } from "../../../flows/auth";

test.describe("Authentication - Logout", () => {
    test("logout redirects user to login page", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(page, adminCredentials, baseURL!);
        await logoutViaUI(page);
        await expect(page).toHaveURL(/authentication|login/);
    });
});
