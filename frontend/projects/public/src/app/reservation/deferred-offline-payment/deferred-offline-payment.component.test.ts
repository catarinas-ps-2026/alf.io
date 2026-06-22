import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
    type ComponentFixture,
    TestBed,
    CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { DeferredOfflinePaymentComponent } from './deferred-offline-payment.component';
import { ReservationService } from '../../shared/reservation.service';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { I18nService } from '../../shared/i18n.service';
import { AnalyticsService } from '../../shared/analytics.service';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationInfo } from '../../model/reservation-info';

describe('DeferredOfflinePaymentComponent', () => {
    let component: DeferredOfflinePaymentComponent;
    let fixture: ComponentFixture<DeferredOfflinePaymentComponent>;

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
        invoicingConfiguration: {
            enabled: false,
            onlyInvoice: false,
            vatIncluded: false,
            userCanDownloadReceiptOrInvoice: false,
            enabledItalyEInvoicing: false,
        },
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
        status: 'DEFERRED_OFFLINE_PAYMENT',
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
        data: of({
            type: 'event',
            publicIdentifierParameter: 'eventShortName',
        }),
        params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
    };

    const mockTranslateService = {
        instant: vi.fn(() => ''),
    };

    const mockReservationService = {
        getReservationInfo: vi.fn(() => of(mockReservationInfo)),
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
            declarations: [DeferredOfflinePaymentComponent],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: TranslateService, useValue: mockTranslateService },
                {
                    provide: ReservationService,
                    useValue: mockReservationService,
                },
                {
                    provide: PurchaseContextService,
                    useValue: mockPurchaseContextService,
                },
                { provide: I18nService, useValue: mockI18nService },
                { provide: AnalyticsService, useValue: mockAnalyticsService },
            ],
        });

        fixture = TestBed.createComponent(DeferredOfflinePaymentComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load purchase context and reservation', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith(
                'event',
                'test-event',
            );
            expect(
                mockReservationService.getReservationInfo,
            ).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith(
                'reservation-page-waiting.header.title',
                mockPurchaseContext,
            );
            expect(mockAnalyticsService.pageView).toHaveBeenCalled();
        });

        it('should store purchaseContext and reservationInfo', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.purchaseContext).toEqual(mockPurchaseContext);
            expect(component.reservationInfo).toEqual(mockReservationInfo);
            expect(component.reservationId).toBe('res-123');
            expect(component.publicIdentifier).toBe('test-event');
            expect(component.purchaseContextType).toBe('event');
        });
    });
});
