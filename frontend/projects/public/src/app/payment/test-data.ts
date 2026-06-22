import type { PurchaseContext } from '../model/purchase-context';
import type { ReservationInfo } from '../model/reservation-info';
import type { Event } from '../model/event';
import type { PaymentMethodId, PaymentProxy } from '../model/event';

export const mockReservationInfo: ReservationInfo = {
    id: 'res-123',
    shortId: 'ABC123',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    validity: 900,
    ticketsByCategory: [],
    orderSummary: {
        summary: [],
        totalPrice: '100.00',
        free: false,
        displayVat: true,
        priceInCents: 10000,
        descriptionForPayment: 'Test reservation',
        totalVAT: '22.00',
        vatPercentage: '22%',
    },
    status: 'PENDING',
    validatedBookingInformation: true,
    formattedExpirationDate: {},
    invoiceNumber: null,
    invoiceRequested: false,
    invoiceOrReceiptDocumentPresent: false,
    paid: false,
    tokenAcquired: false,
    paymentProxy: null,
    addCompanyBillingDetails: false,
    customerReference: '',
    skipVatNr: false,
    billingAddress: '',
    billingDetails: {
        companyName: '',
        addressLine1: '',
        addressLine2: '',
        zip: '',
        city: '',
        state: '',
        country: 'US',
        taxId: '',
        invoicingAdditionalInfo: {},
    },
    containsCategoriesLinkedToGroups: false,
    activePaymentMethods: {},
    metadata: {
        hideContactData: false,
        lockEmailEdit: false,
        hideConfirmationButtons: false,
        readyForConfirmation: false,
        finalized: false,
    },
};

export const mockPurchaseContext: PurchaseContext = {
    id: 1,
    type: 'event',
    publicIdentifier: 'test-event',
    title: { en: 'Test Event' },
    description: {},
    shortDescription: {},
    imageUrl: null,
    format: 'IN_PERSON',
    startDate: '',
    endDate: '',
    enabled: true,
    analyticsConfiguration: null,
    embeddingConfiguration: { enabled: false, notificationOrigin: '' },
    additionalCategories: [],
    ticketCategories: [],
    localization: {},
    privacyPolicyUrl: null,
    invoicingConfiguration: {
        enabled: false,
        onlyInvoice: false,
        vatIncluded: false,
        userCanDownloadReceiptOrInvoice: true,
        enabledItalyEInvoicing: false,
    },
    assignmentConfiguration: { enableAttendeeAutocomplete: true },
} as unknown as PurchaseContext;

export const mockEvent: Event = {
    id: 1,
    shortName: 'test-event',
    title: { en: 'Test Event' },
    description: {},
    format: 'IN_PERSON',
    startDate: '2024-01-01T10:00:00Z',
    endDate: '2024-01-01T18:00:00Z',
    enabled: true,
    embeddingConfiguration: { enabled: false, notificationOrigin: '' },
    invoicingConfiguration: {
        enabled: false,
        onlyInvoice: false,
        vatIncluded: false,
        userCanDownloadReceiptOrInvoice: true,
        enabledItalyEInvoicing: false,
    },
    localization: {},
    analyticsConfiguration: null,
} as unknown as Event;

export interface MockFormGroup {
    get: (path: string) => { setValue: (value: string) => void };
}

export const createMockFormGroup = (): MockFormGroup => ({
    get: vi.fn(() => ({
        setValue: vi.fn(),
    })),
});

export interface CustomOfflinePayment {
    paymentMethodId: PaymentMethodId;
    localizations: {
        [lang: string]: { paymentDescription: string };
    };
}

export const mockCustomOfflinePayments: CustomOfflinePayment[] = [
    {
        paymentMethodId: 'CUSTOM_OFFLINE_1',
        localizations: {
            en: { paymentDescription: 'Bank Transfer - English' },
            es: { paymentDescription: 'Trasferencia Bancaria - Español' },
        },
    },
    {
        paymentMethodId: 'CUSTOM_OFFLINE_2',
        localizations: {
            en: { paymentDescription: 'Credit Card - English' },
            es: { paymentDescription: 'Tarjeta de Crédito - Español' },
        },
    },
];
