import type { Event } from '../model/event';
import type { PurchaseContext } from '../model/purchase-context';
import type { ReservationInfo } from '../model/reservation-info';
import type { User } from '../model/user';
import type { Ticket } from '../model/ticket';
import type { AdditionalServiceWithData } from '../model/reservation-info';

export const mockEvent: Event = {
    id: 1,
    shortName: 'test-event',
    title: { en: 'Test Event', it: 'Evento Test' },
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
    contentLanguages: [{ locale: 'en', name: 'English' }],
    currency: 'USD',
    i18nOverride: {},
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
    contentLanguages: [{ locale: 'en', name: 'English' }],
    currency: 'USD',
} as unknown as PurchaseContext;

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

export const mockUser: User = {
    firstName: 'John',
    lastName: 'Doe',
    emailAddress: 'john@example.com',
    profile: {
        fullName: 'John Doe',
        additionalData: {},
    },
};

export const mockTicket: Ticket = {
    uuid: 'ticket-123',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    locked: false,
    assigned: true,
    userLanguage: 'en',
    ticketFieldConfigurationBeforeStandard: [],
    ticketFieldConfigurationAfterStandard: [],
};

export const mockAdditionalService: AdditionalServiceWithData = {
    serviceId: 1,
    itemId: 'addon-1',
    title: 'Additional Service',
    ticketUUID: 'ticket-123',
    ticketFieldConfiguration: [],
    count: 1,
    price: 10.0,
};
