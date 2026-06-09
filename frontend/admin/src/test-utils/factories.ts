import type { AdditionalField } from '../model/additional-field.ts';
import type { AdditionalItem } from '../model/additional-item.ts';
import type {
    AlfioEvent,
    DateTimeModification,
    EventWithOrganization,
} from '../model/event.ts';
import type { Organization } from '../model/organization.ts';
import type {
    ContentLanguage,
    PurchaseContext,
} from '../model/purchase-context.ts';
import type { ValidatedResponse } from '../model/validation.ts';

/**
 * Crea un ContentLanguage con valores por defecto (en, English).
 * Soporta overrides parciales para personalización.
 */
export function createContentLanguage(
    overrides: Partial<ContentLanguage> = {},
): ContentLanguage {
    return {
        locale: 'en',
        value: 1,
        language: 'English',
        displayLanguage: 'English',
        ...overrides,
    };
}

/**
 * Crea un PurchaseContext con valores por defecto (type: 'event', publicIdentifier: 'test-event-123').
 * Incluye un ContentLanguage por defecto.
 */
export function createPurchaseContext(
    overrides: Partial<PurchaseContext> = {},
): PurchaseContext {
    const languages = [createContentLanguage()];
    return {
        type: 'event',
        publicIdentifier: 'test-event-123',
        contentLanguages: languages,
        firstContentLanguage: languages[0],
        ...overrides,
    };
}

/**
 * Crea un DateTimeModification con valores por defecto (2025-01-15, 10:00).
 */
export function createDateTimeModification(
    overrides: Partial<DateTimeModification> = {},
): DateTimeModification {
    return {
        date: '2025-01-15',
        time: '10:00',
        ...overrides,
    };
}

/**
 * Crea un AdditionalField con valores por defecto (input:text, required, ATTENDEE context).
 */
export function createAdditionalField(
    overrides: Partial<AdditionalField> = {},
): AdditionalField {
    return {
        name: 'attendee-name',
        order: 0,
        type: 'input:text',
        required: true,
        editable: true,
        context: 'ATTENDEE',
        displayAtCheckIn: false,
        description: {
            en: {
                locale: 'en',
                fieldName: 'attendee-name',
                description: {
                    label: 'Attendee Name',
                    placeholder: 'Enter name',
                },
            },
        },
        ...overrides,
    };
}

/**
 * Crea un AdditionalItem con valores por defecto (SUPPLEMENT, price: 1000, EUR).
 */
export function createAdditionalItem(
    overrides: Partial<AdditionalItem> = {},
): AdditionalItem {
    return {
        id: 1,
        price: 1000,
        fixPrice: true,
        ordinal: 0,
        inception: createDateTimeModification(),
        expiration: createDateTimeModification({ date: '2025-12-31' }),
        vat: null,
        vatType: 'INHERITED',
        title: [],
        description: [],
        type: 'SUPPLEMENT',
        supplementPolicy: 'OPTIONAL_UNLIMITED_AMOUNT',
        currencyCode: 'EUR',
        availableItems: null,
        minPrice: null,
        maxPrice: null,
        finalPrice: 1000,
        currency: 'EUR',
        ...overrides,
    };
}

export function createAlfioEvent(
    overrides: Partial<AlfioEvent> = {},
): AlfioEvent {
    const purchaseCtx = createPurchaseContext();
    return {
        ...purchaseCtx,
        id: 1,
        shortName: 'test-event',
        displayName: 'Test Event',
        ticketCategories: [],
        description: {},
        title: {},
        begin: '2025-01-15T10:00:00',
        format: 'IN_PERSON',
        currency: 'EUR',
        formattedBegin: 'January 15, 2025 10:00 AM',
        visibleForCurrentUser: true,
        displayStatistics: true,
        status: 'ACTIVE',
        expired: false,
        locales: 1,
        freeOfCharge: false,
        sameDay: true,
        end: '2025-01-15T18:00:00',
        online: false,
        organizationId: 1,
        regularPrice: 5000,
        termsAndConditionsUrl: '',
        privacyPolicyUrl: '',
        vatIncluded: true,
        vatPercentage: 22,
        beginTimeZoneOffset: 60,
        endTimeZoneOffset: 60,
        isOnline: false,
        supportsAdditionalItemsQuantity: false,
        supportsAdditionalServicesOrdinal: false,
        finalPrice: 5000,
        netPrice: 4098,
        taxablePrice: 4098,
        ...overrides,
    };
}

export function createOrganization(
    overrides: Partial<Organization> = {},
): Organization {
    return {
        id: 1,
        name: 'Test Org',
        email: 'org@test.com',
        externalId: null,
        slug: null,
        ...overrides,
    };
}

export function createEventWithOrganization(
    overrides: Partial<EventWithOrganization> = {},
): EventWithOrganization {
    return {
        event: createAlfioEvent(),
        organization: createOrganization(),
        ...overrides,
    };
}

export function createValidatedResponse<T>(
    value: T,
    overrides: Partial<ValidatedResponse<T>> = {},
): ValidatedResponse<T> {
    return {
        success: true,
        errorCount: 0,
        validationErrors: [],
        value,
        warnings: [],
        ...overrides,
    };
}
