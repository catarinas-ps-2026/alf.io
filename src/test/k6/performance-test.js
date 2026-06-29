import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { check, sleep } from "k6";
import http from "k6/http";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const API_KEY = __ENV.API_KEY || "my-performance-test-api-key";

const IS_CI = __ENV.GITHUB_ACTIONS === "true" || __ENV.CI === "true";
const PROFILE = __ENV.PROFILE || (IS_CI ? "smoke" : "mid-large-event");

let stages;
let thresholds;

if (PROFILE === "mid-large-event") {
    stages = [
        { duration: "1m", target: 200 },
        { duration: "30s", target: 1600 },
        { duration: "2m", target: 1600 },
        { duration: "1m", target: 0 },
    ];

    thresholds = {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<1000"],
    };
} else {
    stages = [
        { duration: "10s", target: 10 },
        { duration: "20s", target: 10 },
        { duration: "10s", target: 0 },
    ];

    thresholds = {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<500"],
    };
}

export const options = {
    stages: stages,
    thresholds: thresholds,
};

export function setup() {
    const systemHeaders = {
        "Content-Type": "application/json",
        Authorization: `ApiKey ${API_KEY}`,
    };

    // 1. Get or create organization
    const orgsRes = http.get(
        `${BASE_URL}/api/v1/admin/system/organization/list`,
        { headers: systemHeaders },
    );

    check(orgsRes, {
        "organizations listed successfully": (res) => res.status === 200,
    });

    if (orgsRes.status !== 200) {
        throw new Error(
            `Failed to list organizations: ${orgsRes.status} ${orgsRes.body}`,
        );
    }

    const orgs = JSON.parse(orgsRes.body);
    let orgId;
    if (Array.isArray(orgs) && orgs.length > 0) {
        orgId = orgs[0].id;
    } else {
        console.log("No organization found. Creating one...");
        const orgPayload = {
            name: "K6 Org",
            email: "k6@localhost",
            description: "K6 Performance Org",
            externalId: "k6-org",
            slug: "k6-org",
        };
        const createOrgRes = http.post(
            `${BASE_URL}/api/v1/admin/system/organization/create`,
            JSON.stringify(orgPayload),
            { headers: systemHeaders },
        );
        check(createOrgRes, {
            "organization created successfully": (res) => res.status === 200,
        });
        if (createOrgRes.status !== 200) {
            throw new Error(
                `Failed to create organization: ${createOrgRes.status} ${createOrgRes.body}`,
            );
        }
        const newOrg = JSON.parse(createOrgRes.body);
        orgId = newOrg.id;
    }

    // 2. Generate a client API key for the organization
    const keyPayload = {
        apiKeyType: "API_CLIENT",
        description: "K6 Performance Client Key",
    };
    const keyRes = http.put(
        `${BASE_URL}/api/v1/admin/system/organization/${orgId}/api-key`,
        JSON.stringify(keyPayload),
        { headers: systemHeaders },
    );
    check(keyRes, {
        "client API key generated successfully": (res) => res.status === 200,
    });
    if (keyRes.status !== 200) {
        throw new Error(
            `Failed to generate client API key: ${keyRes.status} ${keyRes.body}`,
        );
    }
    const keyData = JSON.parse(keyRes.body);
    const clientApiKey = keyData.apiKey;

    // 3. Create the test event using the generated client API key
    const slug = `k6-perf-${Math.random().toString(36).substring(2, 9)}`;
    const now = new Date();

    const formatDate = (d) => d.toISOString().split(".")[0];
    const startSelling = formatDate(
        new Date(now.getTime() - 24 * 60 * 60 * 1000),
    );
    const eventStart = formatDate(
        new Date(now.getTime() + 2 * 24 * 60 * 60 * 1000),
    );
    const eventEnd = formatDate(
        new Date(now.getTime() + (2 * 24 * 60 + 120) * 60 * 1000),
    );

    const eventPayload = {
        title: "K6 Performance Event",
        slug: slug,
        description: [
            {
                lang: "en",
                body: "Performance testing description",
            },
        ],
        location: {
            fullAddress: "Pollegio 6742 Switzerland",
            coordinate: {
                latitude: "45.55",
                longitude: "9.00",
            },
        },
        timezone: "Europe/Zurich",
        startDate: eventStart,
        endDate: eventEnd,
        websiteUrl: "https://alf.io",
        termsAndConditionsUrl: "https://alf.io",
        imageUrl:
            "https://alf.io/img/getting-started/email/mailjet-email-options.PNG",
        tickets: {
            freeOfCharge: false,
            max: 10000,
            currency: "CHF",
            taxPercentage: 7.7,
            taxIncludedInPrice: true,
            paymentMethods: ["STRIPE", "ON_SITE", "OFFLINE"],
            categories: [
                {
                    name: "Standard",
                    description: [
                        {
                            lang: "en",
                            body: "Standard Ticket Description",
                        },
                    ],
                    maxTickets: null,
                    accessRestricted: false,
                    price: 10.0,
                    startSellingDate: startSelling,
                    endSellingDate: eventStart,
                },
            ],
        },
    };

    const clientHeaders = {
        "Content-Type": "application/json",
        Authorization: `ApiKey ${clientApiKey}`,
    };

    console.log(`Creating test event with slug: ${slug}`);
    const createRes = http.post(
        `${BASE_URL}/api/v1/admin/event/create`,
        JSON.stringify(eventPayload),
        { headers: clientHeaders },
    );

    check(createRes, {
        "event created successfully": (res) => res.status === 200,
    });

    if (createRes.status !== 200) {
        throw new Error(
            `Failed to create test event: ${createRes.status} ${createRes.body}`,
        );
    }

    const catRes = http.get(
        `${BASE_URL}/api/v2/public/event/${slug}/ticket-categories`,
    );
    check(catRes, {
        "categories fetched successfully": (res) => res.status === 200,
    });

    const categoriesResponse = JSON.parse(catRes.body);
    const ticketCategories = categoriesResponse.ticketCategories || [];
    const standardCategory = ticketCategories.find(
        (c) => c.name === "Standard",
    );
    if (!standardCategory) {
        throw new Error("Standard category not found in the created event.");
    }

    return {
        slug: slug,
        categoryId: standardCategory.id,
        clientApiKey: clientApiKey,
    };
}

export default function (data) {
    const { slug, categoryId } = data;

    const viewRes = http.get(`${BASE_URL}/api/v2/public/event/${slug}`);
    check(viewRes, {
        "view event status is 200": (res) => res.status === 200,
    });

    const csrfToken =
        viewRes.headers["Xsrf-Token"] ||
        viewRes.headers["xsrf-token"] ||
        viewRes.headers["XSRF-TOKEN"] ||
        viewRes.headers["xsrf-token"] ||
        "";
    const authToken =
        viewRes.headers["X-Auth-Token"] ||
        viewRes.headers["x-auth-token"] ||
        "";
    const headers = {
        "Content-Type": "application/json",
        "X-CSRF-TOKEN": csrfToken,
        "X-Auth-Token": authToken,
    };

    const catRes = http.get(
        `${BASE_URL}/api/v2/public/event/${slug}/ticket-categories`,
    );
    check(catRes, {
        "fetch categories status is 200": (res) => res.status === 200,
    });

    const reservePayload = {
        promoCode: "",
        reservation: [
            {
                ticketCategoryId: categoryId,
                quantity: 1,
            },
        ],
        additionalService: [],
        captcha: "",
    };

    const reserveRes = http.post(
        `${BASE_URL}/api/v2/public/event/${slug}/reserve-tickets?lang=en`,
        JSON.stringify(reservePayload),
        { headers },
    );

    check(reserveRes, {
        "reserve tickets status is 200": (res) => res.status === 200,
        "reserve tickets has value": (res) => {
            const body = JSON.parse(res.body);
            return body.value !== undefined;
        },
    });

    if (reserveRes.status !== 200) return;
    const reservationId = JSON.parse(reserveRes.body).value;

    const reservationRes = http.get(
        `${BASE_URL}/api/v2/public/reservation/${reservationId}`,
    );
    check(reservationRes, {
        "get reservation status is 200": (res) => res.status === 200,
    });

    if (reservationRes.status !== 200) return;
    const reservationDetails = JSON.parse(reservationRes.body);

    const ticketsByCategory = reservationDetails.ticketsByCategory;
    if (
        !ticketsByCategory ||
        ticketsByCategory.length === 0 ||
        ticketsByCategory[0].tickets.length === 0
    ) {
        console.error("No tickets found in reservation.");
        return;
    }
    const ticketUuid = ticketsByCategory[0].tickets[0].uuid;

    const validatePayload = {
        email: "k6-test-user@example.com",
        fullName: "K6 Test User",
        firstName: "K6",
        lastName: "Test User",
        billingAddress: "",
        customerReference: "",
        expressCheckoutRequested: false,
        postponeAssignment: false,
        vatCountryCode: "",
        vatNr: "",
        invoiceRequested: false,
        tickets: {
            [ticketUuid]: {
                email: "k6-attendee@example.com",
                fullName: "K6 Attendee",
                firstName: "K6",
                lastName: "Attendee",
                userLanguage: "en",
                additionalServices: {},
                additional: {},
            },
        },
        additionalServices: {},
    };

    const validateRes = http.post(
        `${BASE_URL}/api/v2/public/reservation/${reservationId}/validate-to-overview?lang=en`,
        JSON.stringify(validatePayload),
        { headers },
    );

    check(validateRes, {
        "validate-to-overview status is 200": (res) => res.status === 200,
    });

    if (validateRes.status !== 200) return;

    const confirmPayload = {
        gatewayToken: "",
        paymentProxy: "OFFLINE",
        selectedPaymentMethod: "BANK_TRANSFER",
        termAndConditionsAccepted: true,
        privacyPolicyAccepted: true,
        hmac: "",
        captcha: "",
    };

    const confirmRes = http.post(
        `${BASE_URL}/api/v2/public/reservation/${reservationId}?lang=en`,
        JSON.stringify(confirmPayload),
        { headers },
    );

    check(confirmRes, {
        "confirm reservation status is 200": (res) => res.status === 200,
    });

    if (confirmRes.status !== 200) return;

    const statusRes = http.get(
        `${BASE_URL}/api/v2/public/reservation/${reservationId}/status`,
    );
    check(statusRes, {
        "reservation status is 200": (res) => res.status === 200,
        "status is OFFLINE_PAYMENT": (res) => {
            const body = JSON.parse(res.body);
            return body.status === "OFFLINE_PAYMENT";
        },
    });

    sleep(1);
}

export function teardown(data) {
    const { slug, clientApiKey } = data;
    const headers = {
        "Content-Type": "application/json",
        Authorization: `ApiKey ${clientApiKey}`,
    };

    console.log(`Cleaning up test event with slug: ${slug}`);
    const deleteRes = http.del(`${BASE_URL}/api/v1/admin/event/${slug}`, null, {
        headers,
    });

    check(deleteRes, {
        "event deleted successfully": (res) => res.status === 200,
    });
}

export function handleSummary(data) {
    return {
        "k6-report.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}
