import { randomEmail, randomName, randomString } from "../helpers/random";

export interface UserFactoryData {
    organizationId: number;
    username: string;
    firstName: string;
    lastName: string;
    emailAddress: string;
    role: string;
    type?: string;
}

export interface OrganizationFactoryData {
    name: string;
    email: string;
    description: string;
}

export interface EventFactoryData {
    slug: string;
    name: string;
    location: string;
    startDateTime: string;
    endDateTime: string;
    timeZone: string;
}

export function createUserData(
    organizationId: number,
    role: string,
    overrides?: Partial<UserFactoryData>,
): UserFactoryData {
    const username = `e2e-${role.toLowerCase()}-${randomString(6)}`;
    return {
        organizationId,
        username,
        firstName: "Test",
        lastName: role.charAt(0) + role.slice(1).toLowerCase(),
        emailAddress: `${username}@e2e.test`,
        role,
        type: "INTERNAL",
        ...overrides,
    };
}

export function createOrganizationData(
    overrides?: Partial<OrganizationFactoryData>,
): OrganizationFactoryData {
    const name = `E2E Org ${randomString(6)}`;
    return {
        name,
        email: randomEmail(),
        description: `E2E test organization: ${name}`,
        ...overrides,
    };
}
