import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { SuccessSubscriptionComponent } from './success-subscription.component';
import { ReservationService } from '../../shared/reservation.service';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { I18nService } from '../../shared/i18n.service';
import { AnalyticsService } from '../../shared/analytics.service';
import { FeedbackService } from '../../shared/feedback/feedback.service';
import { EventService } from '../../shared/event.service';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationInfo, ReservationSubscriptionInfo } from '../../model/reservation-info';
import type { BasicEventInfo } from '../../model/basic-event-info';

describe('SuccessSubscriptionComponent', () => {
    let component: SuccessSubscriptionComponent;
    let fixture: ComponentFixture<SuccessSubscriptionComponent>;

    const mockSubscriptionInfo: ReservationSubscriptionInfo = {
        id: 'sub-123',
        pin: '1234',
        configuration: { displayPin: true },
        fieldConfigurationBeforeStandard: [],
        fieldConfigurationAfterStandard: [],
        additionalFields: [],
    };

    const mockPurchaseContext: PurchaseContext = {
        id: 1,
        type: 'subscription',
        publicIdentifier: 'test-subscription',
        title: { en: 'Test Subscription' },
        description: {},
        shortDescription: {},
        imageUrl: null,
        format: 'ONLINE',
        startDate: '',
        endDate: '',
        enabled: true,
        analyticsConfiguration: null,
        embeddingConfiguration: { enabled: false, notificationOrigin: '' },
        additionalCategories: [],
        ticketCategories: [],
        localization: {},
        privacyPolicyUrl: null,
        invoicingConfiguration: { enabled: false, onlyInvoice: false, vatIncluded: false, userCanDownloadReceiptOrInvoice: true, enabledItalyEInvoicing: false },
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
            totalPrice: '0.00',
            free: true,
            displayVat: false,
            priceInCents: 0,
            descriptionForPayment: 'Subscription',
            totalVAT: '0.00',
            vatPercentage: '0%',
        },
        status: 'COMPLETE',
        validatedBookingInformation: true,
        formattedExpirationDate: {},
        invoiceNumber: 'INV-001',
        invoiceRequested: true,
        invoiceOrReceiptDocumentPresent: true,
        paid: true,
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
            readyForConfirmation: true,
            finalized: true,
        },
        subscriptionInfos: [mockSubscriptionInfo],
    };

    const mockCompatibleEvents: BasicEventInfo[] = [
        { id: 1, shortName: 'event-1', title: { en: 'Event 1' }, startDate: '', endDate: '', format: 'IN_PERSON', enabled: true },
        { id: 2, shortName: 'event-2', title: { en: 'Event 2' }, startDate: '', endDate: '', format: 'ONLINE', enabled: true },
    ];

    const mockActivatedRoute = {
        data: of({ type: 'subscription', publicIdentifierParameter: 'subscriptionId' }),
        params: of({ subscriptionId: 'test-subscription', reservationId: 'res-123' }),
    };

    const mockReservationService = {
        getReservationInfo: vi.fn(() => of(mockReservationInfo)),
        reSendReservationEmail: vi.fn(() => of(undefined)),
    };

    const mockPurchaseContextService = {
        getContext: vi.fn(() => of(mockPurchaseContext)),
    };

    const mockI18nService = {
        setPageTitle: vi.fn(),
        getCurrentLang: vi.fn(() => 'en'),
    };

    const mockAnalyticsService = {
        pageView: vi.fn(),
    };

    const mockTranslateService = {
        currentLang: 'en',
    };

    const mockFeedbackService = {
        showSuccess: vi.fn(),
    };

    const mockEventService = {
        getEvents: vi.fn(() => of(mockCompatibleEvents)),
    };

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [SuccessSubscriptionComponent],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: ReservationService, useValue: mockReservationService },
                { provide: PurchaseContextService, useValue: mockPurchaseContextService },
                { provide: I18nService, useValue: mockI18nService },
                { provide: AnalyticsService, useValue: mockAnalyticsService },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: FeedbackService, useValue: mockFeedbackService },
                { provide: EventService, useValue: mockEventService },
            ],
        });

        fixture = TestBed.createComponent(SuccessSubscriptionComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load purchase context and reservation', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith('subscription', 'test-subscription');
            expect(mockReservationService.getReservationInfo).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith('reservation-page.header.title', mockPurchaseContext);
        });

        it('should store route params', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.publicIdentifier).toBe('test-subscription');
            expect(component.reservationId).toBe('res-123');
            expect(component.purchaseContextType).toBe('subscription');
        });
    });

    describe('processReservationInfo', () => {
        it('should set finalized based on status', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            const finalizingReservation = { ...mockReservationInfo, status: 'FINALIZING' };
            component.processReservationInfo(finalizingReservation);

            expect(component.reservationFinalized).toBe(false);
        });

        it('should load compatible events when finalized', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            component.processReservationInfo(mockReservationInfo);

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockEventService.getEvents).toHaveBeenCalled();
        });

        it('should load events when embedding is not enabled', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 200));
            const initialCalls = mockEventService.getEvents.mock.calls.length;
            const nonEmbeddedContext = { ...mockPurchaseContext, embeddingConfiguration: { enabled: false, notificationOrigin: '' } };
            component.purchaseContext = nonEmbeddedContext as PurchaseContext;
            component.processReservationInfo(mockReservationInfo);

            await new Promise(resolve => setTimeout(resolve, 200));
            expect(mockEventService.getEvents.mock.calls.length).toBeGreaterThan(initialCalls);
        });
    });

    describe('purchaseContextTitle', () => {
        it('should return title in current language', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            expect(component.purchaseContextTitle).toBe('Test Subscription');
        });
    });

    describe('downloadBillingDocumentVisible', () => {
        it('should return true when all conditions met', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = mockReservationInfo;
            expect(component.downloadBillingDocumentVisible).toBe(true);
        });

        it('should return false when not paid', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = { ...mockReservationInfo, paid: false };
            expect(component.downloadBillingDocumentVisible).toBe(false);
        });
    });

    describe('subscriptionInfo', () => {
        it('should return first subscription info', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo = mockReservationInfo;
            expect(component.subscriptionInfo).toEqual(mockSubscriptionInfo);
        });
    });

    describe('displayPin', () => {
        it('should return true when finalized and configuration displayPin is true', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationFinalized = true;
            component.reservationInfo = {
                ...mockReservationInfo,
                subscriptionInfos: [{ ...mockSubscriptionInfo, configuration: { displayPin: true } }],
            };
            expect(component.displayPin).toBe(true);
        });

        it('should return false when finalized is false', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationFinalized = false;
            component.reservationInfo = {
                ...mockReservationInfo,
                subscriptionInfos: [mockSubscriptionInfo],
            };
            expect(component.displayPin).toBe(false);
        });

        it('should return true by default when no configuration', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationFinalized = true;
            component.reservationInfo = {
                ...mockReservationInfo,
                subscriptionInfos: [{ ...mockSubscriptionInfo, configuration: undefined }],
            };
            expect(component.displayPin).toBe(true);
        });
    });

    describe('reSendReservationEmail', () => {
        it('should call reservationService.reSendReservationEmail', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.publicIdentifier = 'test-subscription';
            component.reservationId = 'res-123';
            component.reSendReservationEmail();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.reSendReservationEmail).toHaveBeenCalledWith('subscription', 'test-subscription', 'res-123', 'en');
            expect(mockFeedbackService.showSuccess).toHaveBeenCalledWith('email.confirmation-email-sent');
        });
    });

    describe('copied', () => {
        it('should show success feedback', () => {
            component.copied('1234');
            expect(mockFeedbackService.showSuccess).toHaveBeenCalledWith('reservation-page-complete.subscription.copy.success');
        });
    });

    describe('showReservationButtons', () => {
        it('should return true when finalized and not embedded', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = mockReservationInfo;
            component.reservationFinalized = true;
            expect(component.showReservationButtons).toBe(true);
        });

        it('should return false when not finalized', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = mockReservationInfo;
            component.reservationFinalized = false;
            expect(component.showReservationButtons).toBe(false);
        });

        it('should return false when hideConfirmationButtons is true', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = { ...mockReservationInfo, metadata: { ...mockReservationInfo.metadata, hideConfirmationButtons: true } };
            component.reservationFinalized = true;
            expect(component.showReservationButtons).toBe(false);
        });
    });
});