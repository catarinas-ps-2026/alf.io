import { expect, test } from "../../fixtures/auth";
import { deleteOrganizationViaApi } from "../../helpers/auth-helper";
import { loginAs } from "../../flows/auth";
import { OrganizationsPage } from "../../pages/organizations/OrganizationsPage";
import { randomString } from "../../helpers/random";

test.describe("Organizations", () => {
    test("admin can create a new organization and it appears in the list", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const organizations = new OrganizationsPage(page);
        await organizations.goto();

        const name = `ZZZ E2E Test Create Org ${randomString(6)}`;
        let organizationId: number | undefined;
        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name,
                email: "e2e-create-org@e2e.test",
                description: "Created by the Organizations E2E suite",
            });
            await organizations.save();

            expect(await organizations.isOrganizationVisible(name)).toBe(true);
            organizationId = await organizations.getOrganizationIdFor(name);
        } finally {
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });

    test("admin can edit an organization and the changes are saved", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const organizations = new OrganizationsPage(page);
        await organizations.goto();

        const name = `ZZZ E2E Test Edit Org ${randomString(6)}`;
        let organizationId: number | undefined;
        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name,
                email: "e2e-edit-org@e2e.test",
                description: "Original description",
            });
            await organizations.save();
            organizationId = await organizations.getOrganizationIdFor(name);

            await organizations.clickEditFor(name);
            const updatedDescription = "Updated description after edit";
            await organizations.descriptionInput.fill(updatedDescription);
            await organizations.save();

            await organizations.waitForDescription(name, updatedDescription);
            expect(await organizations.getDescriptionFor(name)).toBe(
                updatedDescription,
            );
        } finally {
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });

    test("admin can cancel editing an organization without persisting changes", async ({
        page,
        adminCredentials,
        baseURL,
    }) => {
        await loginAs(
            page,
            adminCredentials,
            baseURL || "http://localhost:8080",
        );

        const organizations = new OrganizationsPage(page);
        await organizations.goto();

        const name = `ZZZ E2E Test Cancel Org ${randomString(6)}`;
        const originalDescription = "Description that must survive the cancel";
        let organizationId: number | undefined;
        try {
            await organizations.openCreateForm();
            await organizations.fillForm({
                name,
                email: "e2e-cancel-org@e2e.test",
                description: originalDescription,
            });
            await organizations.save();
            organizationId = await organizations.getOrganizationIdFor(name);

            await organizations.clickEditFor(name);
            await organizations.descriptionInput.fill(
                "This should never be saved",
            );
            await organizations.cancel();

            expect(await organizations.getDescriptionFor(name)).toBe(
                originalDescription,
            );
        } finally {
            if (organizationId) {
                await deleteOrganizationViaApi(page, organizationId);
            }
        }
    });
});
