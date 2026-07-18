import { expect, test } from "../../../fixtures/auth";
import { withDisposableUser } from "../../../helpers/auth-helper";
import { ProfilePage } from "../../../pages/profile/ProfilePage";

test.describe("Owner Profile - Change password", () => {
    test("user can change their password providing the correct current password", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        const url = baseURL || "http://localhost:8080";

        await withDisposableUser(
            page,
            adminCredentials,
            url,
            "OWNER",
            async (credentials) => {
                const profile = new ProfilePage(page);
                await profile.goto();

                await profile.changePassword(
                    credentials.password,
                    "Temp1234!x",
                );
                expect(await profile.isSuccessMessageVisible()).toBe(true);
            },
        );
    });

    test("shows an error when the current password is incorrect", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        const url = baseURL || "http://localhost:8080";

        await withDisposableUser(
            page,
            adminCredentials,
            url,
            "OWNER",
            async () => {
                const profile = new ProfilePage(page);
                await profile.goto();

                await profile.changePassword(
                    "definitely-wrong-password",
                    "NewPass123!",
                );
                expect(await profile.isOldPasswordErrorVisible()).toBe(true);
            },
        );
    });
});
