import { describe, it, expect, beforeEach } from 'vitest';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { UntypedFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TicketFormComponent } from './ticket-form.component';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationMetadata } from '../../model/reservation-info';
import type { Ticket } from '../../model/ticket';

describe('TicketFormComponent', () => {
    let component: TicketFormComponent;
    let fixture: ComponentFixture<TicketFormComponent>;

    const formBuilder = new UntypedFormBuilder();

    const mockPurchaseContext: PurchaseContext = {
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
        contentLanguages: [{ locale: 'en', name: 'English' }],
        invoicingConfiguration: {
            enabled: false,
            onlyInvoice: false,
            vatIncluded: false,
            userCanDownloadReceiptOrInvoice: false,
            enabledItalyEInvoicing: false,
        },
        assignmentConfiguration: { enableAttendeeAutocomplete: true },
    } as unknown as PurchaseContext;

    const mockReservationMetadata: ReservationMetadata = {
        hideContactData: false,
        lockEmailEdit: true,
        hideConfirmationButtons: false,
        readyForConfirmation: true,
        finalized: true,
    };

    const mockTicket: Ticket = {
        uuid: 'ticket-123',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        locked: false,
        assigned: true,
    } as unknown as Ticket;

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [TicketFormComponent],
            imports: [ReactiveFormsModule],
        });

        fixture = TestBed.createComponent(TicketFormComponent);
        component = fixture.componentInstance;

        component.form = formBuilder.group({
            firstName: 'John',
            lastName: 'Doe',
            email: 'john@example.com',
            userLanguage: null,
            additional: formBuilder.group({}),
        });
        component.ticket = mockTicket;
        component.purchaseContext = mockPurchaseContext;
        component.reservationMetadata = mockReservationMetadata;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should set userLanguage when only one language available', () => {
            component.ngOnInit();
            expect(component.form.get('userLanguage').value).toBe('en');
        });

        it('should not set userLanguage when multiple languages available', () => {
            component.purchaseContext = {
                ...mockPurchaseContext,
                contentLanguages: [
                    { locale: 'en', name: 'English' },
                    { locale: 'it', name: 'Italian' },
                ],
            } as PurchaseContext;

            component.ngOnInit();
            expect(component.form.get('userLanguage').value).toBeNull();
        });

        it('should not set userLanguage when no contentLanguages', () => {
            component.purchaseContext = {
                ...mockPurchaseContext,
                contentLanguages: null,
            } as PurchaseContext;

            component.ngOnInit();
            expect(component.form.get('userLanguage').value).toBeNull();
        });
    });

    describe('getAdditional', () => {
        it('should return additional form group', () => {
            const result = component.getAdditional(component.form);
            expect(result).toBeDefined();
        });
    });

    describe('emailEditForbidden', () => {
        it('should return true when ticket is locked', () => {
            component.ticket = { ...mockTicket, locked: true } as Ticket;
            component.reservationMetadata = mockReservationMetadata;

            expect(component.emailEditForbidden).toBe(true);
        });

        it('should return true when lockEmailEdit is true and ticket has email', () => {
            component.ticket = {
                ...mockTicket,
                locked: false,
                email: 'john@example.com',
            } as Ticket;
            component.reservationMetadata = {
                ...mockReservationMetadata,
                lockEmailEdit: true,
            };

            expect(component.emailEditForbidden).toBe(true);
        });

        it('should return false when ticket is not locked and lockEmailEdit is false', () => {
            component.ticket = { ...mockTicket, locked: false } as Ticket;
            component.reservationMetadata = {
                ...mockReservationMetadata,
                lockEmailEdit: false,
            };

            expect(component.emailEditForbidden).toBe(false);
        });

        it('should return false when lockEmailEdit is true but ticket has no email', () => {
            component.ticket = {
                ...mockTicket,
                locked: false,
                email: null,
            } as Ticket;
            component.reservationMetadata = {
                ...mockReservationMetadata,
                lockEmailEdit: true,
            };

            expect(component.emailEditForbidden).toBe(false);
        });
    });
});
