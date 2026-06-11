import { describe, it, expect } from 'vitest';
import {
    mockEvent,
    mockPurchaseContext,
    mockReservationInfo,
    mockUser,
    mockTicket,
    mockAdditionalService,
} from './test-data';

describe('test-data.ts', () => {
    describe('mockEvent', () => {
        it('should have required properties', () => {
            expect(mockEvent.id).toBe(1);
            expect(mockEvent.shortName).toBe('test-event');
            expect(mockEvent.title).toEqual({ en: 'Test Event', it: 'Evento Test' });
            expect(mockEvent.format).toBe('IN_PERSON');
            expect(mockEvent.enabled).toBe(true);
            expect(mockEvent.currency).toBe('USD');
        });

        it('should have valid contentLanguages', () => {
            expect(mockEvent.contentLanguages).toHaveLength(1);
            expect(mockEvent.contentLanguages[0].locale).toBe('en');
        });

        it('should have i18nOverride empty object', () => {
            expect(mockEvent.i18nOverride).toEqual({});
        });

        it('should have valid dates', () => {
            expect(mockEvent.startDate).toBe('2024-01-01T10:00:00Z');
            expect(mockEvent.endDate).toBe('2024-01-01T18:00:00Z');
        });
    });

    describe('mockPurchaseContext', () => {
        it('should have required properties', () => {
            expect(mockPurchaseContext.id).toBe(1);
            expect(mockPurchaseContext.type).toBe('event');
            expect(mockPurchaseContext.publicIdentifier).toBe('test-event');
            expect(mockPurchaseContext.title).toEqual({ en: 'Test Event' });
            expect(mockPurchaseContext.enabled).toBe(true);
            expect(mockPurchaseContext.currency).toBe('USD');
        });

        it('should have valid invoicingConfiguration', () => {
            expect(mockPurchaseContext.invoicingConfiguration.enabled).toBe(false);
            expect(mockPurchaseContext.invoicingConfiguration.userCanDownloadReceiptOrInvoice).toBe(true);
        });

        it('should have assignmentConfiguration', () => {
            expect(mockPurchaseContext.assignmentConfiguration.enableAttendeeAutocomplete).toBe(true);
        });
    });

    describe('mockReservationInfo', () => {
        it('should have required properties', () => {
            expect(mockReservationInfo.id).toBe('res-123');
            expect(mockReservationInfo.shortId).toBe('ABC123');
            expect(mockReservationInfo.firstName).toBe('John');
            expect(mockReservationInfo.lastName).toBe('Doe');
            expect(mockReservationInfo.email).toBe('john@example.com');
            expect(mockReservationInfo.status).toBe('PENDING');
        });

        it('should have orderSummary with correct values', () => {
            expect(mockReservationInfo.orderSummary.totalPrice).toBe('100.00');
            expect(mockReservationInfo.orderSummary.displayVat).toBe(true);
            expect(mockReservationInfo.orderSummary.priceInCents).toBe(10000);
        });

        it('should have billingDetails', () => {
            expect(mockReservationInfo.billingDetails.country).toBe('US');
            expect(mockReservationInfo.billingDetails.zip).toBe('');
        });

        it('should have metadata flags', () => {
            expect(mockReservationInfo.metadata.hideContactData).toBe(false);
            expect(mockReservationInfo.metadata.finalized).toBe(false);
        });

        it('should have validity in seconds', () => {
            expect(mockReservationInfo.validity).toBe(900);
        });
    });

    describe('mockUser', () => {
        it('should have required properties', () => {
            expect(mockUser.firstName).toBe('John');
            expect(mockUser.lastName).toBe('Doe');
            expect(mockUser.emailAddress).toBe('john@example.com');
        });

        it('should have profile with additionalData', () => {
            expect(mockUser.profile.fullName).toBe('John Doe');
            expect(mockUser.profile.additionalData).toEqual({});
        });
    });

    describe('mockTicket', () => {
        it('should have required properties', () => {
            expect(mockTicket.uuid).toBe('ticket-123');
            expect(mockTicket.firstName).toBe('John');
            expect(mockTicket.lastName).toBe('Doe');
            expect(mockTicket.email).toBe('john@example.com');
            expect(mockTicket.userLanguage).toBe('en');
        });

        it('should have assigned=true and locked=false', () => {
            expect(mockTicket.assigned).toBe(true);
            expect(mockTicket.locked).toBe(false);
        });

        it('should have empty field configurations', () => {
            expect(mockTicket.ticketFieldConfigurationBeforeStandard).toEqual([]);
            expect(mockTicket.ticketFieldConfigurationAfterStandard).toEqual([]);
        });
    });

    describe('mockAdditionalService', () => {
        it('should have required properties', () => {
            expect(mockAdditionalService.serviceId).toBe(1);
            expect(mockAdditionalService.itemId).toBe('addon-1');
            expect(mockAdditionalService.title).toBe('Additional Service');
            expect(mockAdditionalService.ticketUUID).toBe('ticket-123');
        });

        it('should have count and price', () => {
            expect(mockAdditionalService.count).toBe(1);
            expect(mockAdditionalService.price).toBe(10.0);
        });

        it('should have empty ticketFieldConfiguration', () => {
            expect(mockAdditionalService.ticketFieldConfiguration).toEqual([]);
        });
    });
});