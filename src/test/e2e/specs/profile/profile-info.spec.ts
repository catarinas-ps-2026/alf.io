import { expect, test } from "../../fixtures/auth";
import { loginAs } from "../../flows/auth";
import { randomString } from "../../helpers/random";
import { ProfilePage } from "../../pages/profile/ProfilePage";

test.describe("User Profile - Personal information", () => {
    test("user can update first name, last name and email, and the changes are saved", async ({
        page,
        ownerCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            ownerCredentials,
            baseURL || "http://localhost:8080",
        );

        const profile = new ProfilePage(page);
        await profile.goto();

        const updatedFirstName = `Test-${randomString(6)}`;
        await profile.updatePersonalInfo({ firstName: updatedFirstName });

        expect(await profile.isSuccessMessageVisible()).toBe(true);

        await page.reload();
        await expect(profile.firstNameInput).toHaveValue(updatedFirstName);
    });
});
