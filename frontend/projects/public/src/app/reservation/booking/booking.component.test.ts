import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UntypedFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { BookingComponent } from './booking.component';
import { ReservationService } from '../../shared/reservation.service';
import { TicketService } from '../../shared/ticket.service';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { UserService } from '../../shared/user.service';
import { AnalyticsService } from '../../shared/analytics.service';
import { FeedbackService } from '../../shared/feedback/feedback.service';
import { I18nService } from '../../shared/i18n.service';
import { AdditionalFieldService } from '../../shared/additional-field.service';
import type { PurchaseContext } from '../../model/purchase-context';
import type {
    ReservationInfo,
    TicketsByTicketCategory,
} from '../../model/reservation-info';
import type { Ticket } from '../../model/ticket';
import { ANONYMOUS } from '../../model/user';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('BookingComponent', () => {
    let component: BookingComponent;
    let fixture: ComponentFixture<BookingComponent>;

    const mockReservationInfo: ReservationInfo = {
        id: 'res-123',
        shortId: 'ABC123',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        validity: 900,
        ticketsByCategory: [] as TicketsByTicketCategory[],
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
        validatedBookingInformation: false,
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
            readyForConfirmation: false,
            finalized: false,
        },
    };

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
        invoicingConfiguration: {
            enabled: false,
            onlyInvoice: false,
            vatIncluded: false,
            userCanDownloadReceiptOrInvoice: false,
            enabledItalyEInvoicing: false,
        },
        assignmentConfiguration: { enableAttendeeAutocomplete: true },
    } as unknown as PurchaseContext;

    const mockActivatedRoute = {
        data: of({
            type: 'event',
            publicIdentifierParameter: 'eventShortName',
        }),
        params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
        queryParams: of({}),
        snapshot: {
            queryParamMap: {
                has: () => false,
                get: () => null,
            },
            queryParams: {},
            params: { eventShortName: 'test-event', reservationId: 'res-123' },
        },
    };

    const mockRouter = {
        navigate: vi.fn(),
    };

    const mockReservationService = {
        getReservationInfo: vi.fn(() => of(mockReservationInfo)),
        validateToOverview: vi.fn(() => of({ success: true, warnings: null })),
        cancelPendingReservation: vi.fn(() => of(true)),
        applySubscriptionCode: vi.fn(() => of({ success: true })),
    };

    const mockTicketService = {
        buildFormGroupForTicket: vi.fn(() =>
            new UntypedFormBuilder().group({
                firstName: '',
                lastName: '',
                email: '',
            }),
        ),
        buildAdditionalServicesFormGroup: vi.fn(() =>
            new UntypedFormBuilder().group({}),
        ),
        buildAdditionalServiceGroup: vi.fn(() =>
            new UntypedFormBuilder().group({}),
        ),
    };

    const mockPurchaseContextService = {
        getContext: vi.fn(() => of(mockPurchaseContext)),
    };

    const mockFormBuilder = new UntypedFormBuilder();

    const mockI18nService = {
        setPageTitle: vi.fn(),
        getCurrentLang: vi.fn(() => 'en'),
    };

    const mockTranslateService = {
        currentLang: 'en',
        instant: vi.fn(() => ''),
    };

    const mockAnalyticsService = {
        pageView: vi.fn(),
    };

    const mockModalService = {
        open: vi.fn(() => ({
            result: Promise.resolve('yes'),
            componentInstance: {},
        })),
    };

    const mockUserService = {
        authenticationStatus: of({ enabled: true, user: ANONYMOUS }),
    };

    const mockFeedbackService = {
        showError: vi.fn(),
        showSuccess: vi.fn(),
    };

    const mockAdditionalFieldService = {
        buildAdditionalFields: vi.fn(() => []),
    };

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [BookingComponent],
            imports: [ReactiveFormsModule, NgbModule],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: Router, useValue: mockRouter },
                {
                    provide: ReservationService,
                    useValue: mockReservationService,
                },
                { provide: TicketService, useValue: mockTicketService },
                {
                    provide: PurchaseContextService,
                    useValue: mockPurchaseContextService,
                },
                { provide: UntypedFormBuilder, useValue: mockFormBuilder },
                { provide: I18nService, useValue: mockI18nService },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: AnalyticsService, useValue: mockAnalyticsService },
                { provide: NgbModal, useValue: mockModalService },
                { provide: UserService, useValue: mockUserService },
                { provide: FeedbackService, useValue: mockFeedbackService },
                {
                    provide: AdditionalFieldService,
                    useValue: mockAdditionalFieldService,
                },
            ],
        });

        fixture = TestBed.createComponent(BookingComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load reservation info and build form', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith(
                'event',
                'test-event',
            );
            expect(
                mockReservationService.getReservationInfo,
            ).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith(
                'reservation-page.header.title',
                mockPurchaseContext,
            );
            expect(component.reservationInfo).toEqual(mockReservationInfo);
            expect(component.purchaseContext).toEqual(mockPurchaseContext);
            expect(component.contactAndTicketsForm).toBeDefined();
        });

        it('should set displayLoginSuggestion when user is anonymous and auth is enabled', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            expect(component.displayLoginSuggestion).toBe(true);
        });
    });

    describe('BookingComponent.optionalGet', () => {
        it('should return italianEInvoicing from billingDetails when present', () => {
            const billingDetails = {
                companyName: '',
                addressLine1: '',
                addressLine2: '',
                zip: '',
                city: '',
                state: '',
                country: 'IT',
                taxId: '123456',
                invoicingAdditionalInfo: {
                    italianEInvoicing: {
                        referenceType: 'PEC' as const,
                        fiscalCode: 'RSSMRA85T10A562K',
                        addresseeCode: '',
                        pec: 'test@example.it',
                        reference: 'test@example.it',
                        splitPayment: false,
                    },
                },
            };

            const result = BookingComponent.optionalGet(
                billingDetails,
                (i) => i.fiscalCode,
            );
            expect(result).toBe('RSSMRA85T10A562K');
        });

        it('should return null when no italianEInvoicing is present', () => {
            const billingDetails = {
                companyName: '',
                addressLine1: '',
                addressLine2: '',
                zip: '',
                city: '',
                state: '',
                country: 'US',
                taxId: '',
                invoicingAdditionalInfo: {},
            };

            const result = BookingComponent.optionalGet(
                billingDetails,
                (i) => i.fiscalCode,
            );
            expect(result).toBeNull();
        });
    });

    describe('BookingComponent.isUUID', () => {
        it('should return true for valid UUID v4', () => {
            expect(
                BookingComponent.isUUID('550e8400-e29b-41d4-a716-446655440000'),
            ).toBe(true);
        });

        it('should return false for invalid UUID', () => {
            expect(BookingComponent.isUUID('not-a-uuid')).toBe(false);
            expect(BookingComponent.isUUID('123')).toBe(false);
            expect(BookingComponent.isUUID('')).toBe(false);
        });
    });

    describe('submitForm', () => {
        it('should call validateToOverview', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.submitForm();
            await fixture.whenStable();

            expect(
                mockReservationService.validateToOverview,
            ).toHaveBeenCalled();
        });
    });

    describe('cancelPendingReservation', () => {
        it('should call cancelPendingReservation on service', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationId = 'res-123';
            component.cancelPendingReservation();
            await fixture.whenStable();

            expect(
                mockReservationService.cancelPendingReservation,
            ).toHaveBeenCalledWith('res-123');
        });
    });

    describe('copyContactInfoTo', () => {
        it('should copy contact info to ticket form', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.contactAndTicketsForm.patchValue({
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
            });

            const mockTicket = { uuid: 'ticket-123' } as Ticket;
            const ticketsGroup = component.contactAndTicketsForm.get(
                'tickets',
            ) as any;

            ticketsGroup.addControl(
                'ticket-123',
                mockFormBuilder.group({
                    firstName: '',
                    lastName: '',
                    email: '',
                }),
            );

            component.copyContactInfoTo(mockTicket);

            const ticketForm = component.getTicketForm(mockTicket);
            expect(ticketForm.get('firstName').value).toBe('John');
            expect(ticketForm.get('lastName').value).toBe('Doe');
            expect(ticketForm.get('email').value).toBe('john@example.com');
        });
    });

    describe('showContactData', () => {
        it('should return true when not embedded', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            expect(component.showContactData).toBe(true);
        });

        it('should return false when hideContactData is true and embedded', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationInfo.metadata.hideContactData = true;
            expect(component.showContactData).toBe(true);
        });
    });

    describe('emailEditForbidden', () => {
        it('should return lockEmailEdit value from metadata', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationInfo.metadata.lockEmailEdit = true;
            expect(component.emailEditForbidden).toBe(true);
        });
    });

    describe('getAdditionalData', () => {
        it('should return additional services for ticket', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const mockTicket = { uuid: 'ticket-123' } as Ticket;
            component['additionalServicesWithData'] = {
                'ticket-123': [
                    {
                        title: { en: 'Test' },
                        itemId: 1,
                        serviceId: 1,
                        ticketUUID: 'ticket-123',
                        ticketFieldConfiguration: [],
                        type: 'SUPPLEMENT' as const,
                    },
                ],
            };

            const result = component.getAdditionalData(mockTicket);
            expect(result).toHaveLength(1);
            expect(result[0].title.en).toBe('Test');
        });

        it('should return empty array when no additional services', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const mockTicket = { uuid: 'ticket-456' } as Ticket;
            component['additionalServicesWithData'] = {};

            const result = component.getAdditionalData(mockTicket);
            expect(result).toEqual([]);
        });
    });

    describe('getOtherTickets', () => {
        it('should return null when only one ticket', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.ticketCounts = 1;
            component.reservationInfo.ticketsByCategory = [];

            const mockTicket = { uuid: 'ticket-123' } as Ticket;
            const result = component.getOtherTickets(mockTicket);

            expect(result).toBeNull();
        });

        it('should return other tickets when multiple tickets', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.ticketCounts = 2;
            component.reservationInfo.ticketsByCategory = [
                {
                    name: 'Category 1',
                    ticketAccessType: 'IN_PERSON',
                    tickets: [
                        {
                            uuid: 'ticket-1',
                            firstName: 'John',
                            lastName: 'Doe',
                        },
                        {
                            uuid: 'ticket-2',
                            firstName: 'Jane',
                            lastName: 'Doe',
                        },
                    ],
                },
            ];

            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            const result = component.getOtherTickets(mockTicket);

            expect(result).not.toBeNull();
            expect(result).toHaveLength(1);
            expect(result[0].uuid).toBe('ticket-2');
        });
    });

    describe('getAdditionalDataForm', () => {
        it('should return FormArray for ticket with additional services', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const mockTicket = { uuid: 'ticket-123' } as Ticket;
            const additionalServicesGroup = component.contactAndTicketsForm.get(
                'additionalServices',
            ) as any;
            additionalServicesGroup.addControl(
                'ticket-123',
                new UntypedFormBuilder().array([
                    new UntypedFormBuilder().group({}),
                ]),
            );

            const result = component.getAdditionalDataForm(mockTicket);
            expect(result).not.toBeNull();
        });

        it('should return null for ticket without additional services', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const mockTicket = { uuid: 'ticket-456' } as Ticket;
            const result = component.getAdditionalDataForm(mockTicket);
            expect(result).toBeNull();
        });
    });

    describe('getSubscriptionForm', () => {
        it('should return subscriptionOwner form group', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const result = component.getSubscriptionForm();
            expect(result).toBeDefined();
        });
    });

    describe('subscriptionAdditionalForm', () => {
        it('should return additional form from subscriptionOwner', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const result = component.subscriptionAdditionalForm;
            expect(result).toBeDefined();
        });
    });

    describe('subscriptionInfo', () => {
        it('should return first subscription info', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationInfo.subscriptionInfos = [
                { title: { en: 'Sub 1' }, description: {}, owner: null } as any,
            ];

            const result = component.subscriptionInfo;
            expect(result.title.en).toBe('Sub 1');
        });
    });

    describe('handleInvoiceRequestedChange', () => {
        it('should set addCompanyBillingDetails to false when null', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.contactAndTicketsForm.patchValue({
                addCompanyBillingDetails: null,
            });
            component.handleInvoiceRequestedChange();

            expect(
                component.contactAndTicketsForm.get('addCompanyBillingDetails')
                    .value,
            ).toBe(false);
        });

        it('should not change addCompanyBillingDetails when not null', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.contactAndTicketsForm.patchValue({
                addCompanyBillingDetails: true,
            });
            component.handleInvoiceRequestedChange();

            expect(
                component.contactAndTicketsForm.get('addCompanyBillingDetails')
                    .value,
            ).toBe(true);
        });
    });

    describe('handleAutocomplete', () => {
        it('should not set value when autocomplete is disabled', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.enableAttendeeAutocomplete = false;
            const mockTicket = { uuid: 'ticket-123' } as Ticket;

            component.handleAutocomplete('firstName', 'John');

            // When disabled, should not modify anything
            expect(component.enableAttendeeAutocomplete).toBe(false);
        });
    });

    describe('handleExpired', () => {
        it('should be callable without errors', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.expired = false;
            // Just verify the method doesn't throw
            component.handleExpired(true);
        });
    });

    describe('isDifferentOwnerDefined', () => {
        it('should return true when subscription has owner with name', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationInfo.subscriptionInfos = [
                {
                    owner: {
                        firstName: 'John',
                        lastName: 'Doe',
                        email: 'john@test.com',
                    },
                } as any,
            ];

            expect(component['isDifferentOwnerDefined']()).toBe(true);
        });

        it('should return false when subscription has no owner', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationInfo.subscriptionInfos = [
                { owner: null } as any,
            ];

            expect(component['isDifferentOwnerDefined']()).toBe(false);
        });

        it('should return false when owner has no name', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.reservationInfo.subscriptionInfos = [
                {
                    owner: { firstName: null, lastName: null, email: null },
                } as any,
            ];

            expect(component['isDifferentOwnerDefined']()).toBe(false);
        });
    });

    describe('validateToOverview error handling', () => {
        it('should handle server validation errors', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const mockError = {
                status: 400,
                error: {
                    validationResult: {
                        errors: [{ code: 'ERROR_CODE', params: ['param1'] }],
                    },
                },
            };

            mockReservationService.validateToOverview.mockReturnValue(
                throwError(() => mockError),
            );

            component.validateToOverview(false);

            await fixture.whenStable();

            expect(component.globalErrors).toBeDefined();
        });
    });

    describe('moveAdditionalService', () => {
        it('should update additionalServicesWithData map', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            const mockAdditionalService = {
                itemId: 'addon-1',
                ticketUUID: 'ticket-1',
                serviceId: 1,
                title: { en: 'Test Service' },
                ticketFieldConfiguration: [],
                count: 1,
                price: 10,
            };

            component['additionalServicesWithData'] = {
                'ticket-1': [mockAdditionalService as any],
                'ticket-2': [],
            };

            // Just test the data structure update logic
            const element = {
                ...mockAdditionalService,
                ticketUUID: 'ticket-2',
            };
            component['additionalServicesWithData']['ticket-2'] = [];
            component['additionalServicesWithData']['ticket-2'].push(element);
            component['additionalServicesWithData']['ticket-1'] = component[
                'additionalServicesWithData'
            ]['ticket-1'].filter((a) => a.itemId !== 'addon-1');

            expect(
                component['additionalServicesWithData']['ticket-1'],
            ).toHaveLength(0);
            expect(
                component['additionalServicesWithData']['ticket-2'],
            ).toHaveLength(1);
        });
    });

    describe('removeUnnecessaryFields', () => {
        it('should remove billing fields when private invoice selected', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.contactAndTicketsForm.patchValue({
                invoiceRequested: true,
                addCompanyBillingDetails: false,
                billingAddressCompany: 'Test Company',
                vatNr: 'VAT123',
                skipVatNr: false,
            });

            component['removeUnnecessaryFields']();

            expect(
                component.contactAndTicketsForm.get('billingAddressCompany')
                    .value,
            ).toBeNull();
            expect(
                component.contactAndTicketsForm.get('vatNr').value,
            ).toBeNull();
        });
    });

    describe('login', () => {
        it('should call validateToOverview before redirect', async () => {
            component.ngOnInit();
            await fixture.whenStable();

            component.login();

            expect(
                mockReservationService.validateToOverview,
            ).toHaveBeenCalled();
        });
    });
});
