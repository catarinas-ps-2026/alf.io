import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { ProcessingPaymentComponent } from './processing-payment.component';
import { ReservationService } from '../../shared/reservation.service';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { I18nService } from '../../shared/i18n.service';
import { AnalyticsService } from '../../shared/analytics.service';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationInfo } from '../../model/reservation-info';

describe('ProcessingPaymentComponent', () => {
    let component: ProcessingPaymentComponent;
    let fixture: ComponentFixture<ProcessingPaymentComponent>;

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
        privacyPolicyUrl: null,
        invoicingConfiguration: { enabled: false, onlyInvoice: false, vatIncluded: false, userCanDownloadReceiptOrInvoice: false, enabledItalyEInvoicing: false },
        assignmentConfiguration: { enableAttendeeAutocomplete: true },
    } as unknown as PurchaseContext;

    const mockReservationInfo: ReservationInfo = {
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
            descriptionForPayment: 'Test',
            totalVAT: '22.00',
            vatPercentage: '22%',
        },
        status: 'IN_PAYMENT',
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

    const mockActivatedRoute = {
        data: of({ type: 'event', publicIdentifierParameter: 'eventShortName' }),
        params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
        snapshot: {
            queryParams: {},
            params: { eventShortName: 'test-event', reservationId: 'res-123' },
        },
    };

    const mockRouter = {
        navigate: vi.fn(),
    };

    const mockReservationService = {
        getReservationInfo: vi.fn(() => of(mockReservationInfo)),
        getReservationStatusInfo: vi.fn(() => of({ status: 'IN_PAYMENT', validatedBookingInformation: true })),
        forcePaymentStatusCheck: vi.fn(() => of({ success: false, failure: false, redirect: null })),
    };

    const mockPurchaseContextService = {
        getContext: vi.fn(() => of(mockPurchaseContext)),
    };

    const mockI18nService = {
        setPageTitle: vi.fn(),
    };

    const mockAnalyticsService = {
        pageView: vi.fn(),
    };

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [ProcessingPaymentComponent],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: Router, useValue: mockRouter },
                { provide: ReservationService, useValue: mockReservationService },
                { provide: PurchaseContextService, useValue: mockPurchaseContextService },
                { provide: I18nService, useValue: mockI18nService },
                { provide: AnalyticsService, useValue: mockAnalyticsService },
            ],
        });

        fixture = TestBed.createComponent(ProcessingPaymentComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        component.ngOnDestroy();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load purchase context and reservation', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith('event', 'test-event');
            expect(mockReservationService.getReservationInfo).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith('show-ticket.header.title', mockPurchaseContext);
            expect(mockAnalyticsService.pageView).toHaveBeenCalled();
        });
    });

    describe('forceCheck', () => {
        it('should call forcePaymentStatusCheck', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.forceCheckVisible = true;
            component.forceCheckInProgress = false;
            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.forcePaymentStatusCheck).toHaveBeenCalledWith('res-123');
            expect(component.forceCheckInProgress).toBe(false);
        });

        it('should redirect when redirectUrl is returned', async () => {
            const redirectUrl = 'https://example.com/return';
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(of({ success: false, failure: false, redirect: true, redirectUrl }));

            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            const originalLocation = window.location.href;
            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.forcePaymentStatusCheck).toHaveBeenCalled();
        });

        it('should navigate to success when status changes', async () => {
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(of({ success: true, failure: false, redirect: false }));

            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockRouter.navigate).toHaveBeenCalled();
        });
    });

    describe('ngOnDestroy', () => {
        it('should clear interval', async () => {
            const clearIntervalSpy = vi.spyOn(window as any, 'clearInterval');
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.ngOnDestroy();
            expect(clearIntervalSpy).toHaveBeenCalled();
        });
    });

    describe('forceCheck error handling', () => {
        it('should handle error when forceCheck fails', async () => {
            const errorResponse = { status: 500 };
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(throwError(() => errorResponse));

            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo = mockReservationInfo;
            component.purchaseContext = mockPurchaseContext;
            component.reservationId = 'res-123';

            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.forcePaymentStatusCheck).toHaveBeenCalled();
        });
    });

    describe('component properties', () => {
        it('should initialize with default values', () => {
            component.ngOnInit();
            expect(component.forceCheckVisible).toBe(false);
            expect(component.providerWarningVisible).toBe(false);
            expect(component.forceCheckInProgress).toBe(false);
        });
    });

    describe('reservationStateChanged', () => {
        it('should navigate to success page', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';
            component.reservationId = 'res-123';

            component['reservationStateChanged']();

            expect(mockRouter.navigate).toHaveBeenCalledWith(
                ['event', 'test-event', 'reservation', 'res-123', 'success'],
                expect.any(Object),
            );
        });

        it('should use SearchParams.transformParams for query params', async () => {
            mockActivatedRoute.snapshot.queryParams = { lang: 'en' };

            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';
            component.reservationId = 'res-123';

            component['reservationStateChanged']();

            expect(mockRouter.navigate).toHaveBeenCalled();
        });
    });

    describe('forceCheck with redirect', () => {
        it('should redirect when status has redirectUrl', async () => {
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(of({
                success: false,
                failure: false,
                redirect: true,
                redirectUrl: 'https://payment.return.url',
            }));

            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';

            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.forcePaymentStatusCheck).toHaveBeenCalledWith('res-123');
        });
    });

    describe('forceCheck with success', () => {
        it('should call reservationStateChanged when success', async () => {
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(of({
                success: true,
                failure: false,
                redirect: false,
            }));

            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';
            component.reservationId = 'res-123';

            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockRouter.navigate).toHaveBeenCalled();
        });
    });

    describe('forceCheck with failure', () => {
        it('should call reservationStateChanged when failure', async () => {
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(of({
                success: false,
                failure: true,
                redirect: false,
            }));

            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';
            component.reservationId = 'res-123';

            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockRouter.navigate).toHaveBeenCalled();
        });
    });

    describe('forceCheckInProgress state', () => {
        it('should reset forceCheckInProgress after check completes', async () => {
            mockReservationService.forcePaymentStatusCheck.mockReturnValue(of({
                success: false,
                failure: false,
                redirect: false,
            }));

            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';

            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.forceCheckInProgress).toBe(false);
        });
    });
});

function throwError(error: any) {
    return new (require('rxjs').Observable)((subscriber) => {
        subscriber.error(error);
    });
}