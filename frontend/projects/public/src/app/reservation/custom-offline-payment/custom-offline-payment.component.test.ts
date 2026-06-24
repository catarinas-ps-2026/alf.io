import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import {
    type ComponentFixture,
    TestBed,
    CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationInfo, CustomOfflinePayment } from '../../model/event';

vi.mock('../../shared/util', () => ({
    pollReservationStatus: vi.fn(),
}));

import { CustomOfflinePaymentComponent } from './custom-offline-payment.component';
import { ReservationService } from '../../shared/reservation.service';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { I18nService } from '../../shared/i18n.service';
import { AnalyticsService } from '../../shared/analytics.service';
import { pollReservationStatus } from '../../shared/util';

describe('CustomOfflinePaymentComponent', () => {
    let component: CustomOfflinePaymentComponent;
    let fixture: ComponentFixture<CustomOfflinePaymentComponent>;

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
            userCanDownloadReceiptOrInvoice: true,
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
        status: 'CUSTOM_OFFLINE_PAYMENT',
        validatedBookingInformation: true,
        formattedExpirationDate: {},
        invoiceNumber: 'INV-001',
        invoiceRequested: true,
        invoiceOrReceiptDocumentPresent: true,
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
    } as unknown as ReservationInfo;

    const mockCustomPaymentMethod: CustomOfflinePayment = {
        paymentMethodId: 'CUSTOM_BANK',
        paymentMethodName: 'Custom Bank Transfer',
        paymentProxy: 'CUSTOM_OFFLINE',
        localizations: {
            en: {
                paymentName: 'Custom Bank Transfer',
                instructions: 'Pay to account XYZ',
            },
            es: {
                paymentName: 'Transferencia Bancaria Custom',
                instructions: 'Pagar a cuenta XYZ',
            },
            it: {
                paymentName: 'Bonifico Bancario Custom',
                instructions: 'Pagare sul conto XYZ',
            },
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
        getSelectedCustomPaymentMethodDetails: vi.fn(() =>
            of(mockCustomPaymentMethod),
        ),
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

    beforeEach(async () => {
        vi.clearAllMocks();
        TestBed.configureTestingModule({
            declarations: [CustomOfflinePaymentComponent],
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

        fixture = TestBed.createComponent(CustomOfflinePaymentComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load purchase context, reservation and custom payment method', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith(
                'event',
                'test-event',
            );
            expect(
                mockReservationService.getReservationInfo,
            ).toHaveBeenCalledWith('res-123');
            expect(
                mockReservationService.getSelectedCustomPaymentMethodDetails,
            ).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith(
                'reservation-page-waiting.header.title',
                mockPurchaseContext,
            );
            expect(mockAnalyticsService.pageView).toHaveBeenCalled();
        });

        it('should set paymentReason with shortId', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.paymentReason).toContain('ABC123');
        });

        it('should store customPaymentMethodDetails', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.customPaymentMethodDetails).toEqual(
                mockCustomPaymentMethod,
            );
        });

        it('should set reservationFinalized to true when status is not OFFLINE_FINALIZING', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.reservationFinalized).toBe(true);
        });

        it('should set reservationFinalized to false and poll when status is OFFLINE_FINALIZING', async () => {
            const finalizingReservation = {
                ...mockReservationInfo,
                status: 'OFFLINE_FINALIZING',
            };
            mockReservationService.getReservationInfo.mockReturnValueOnce(
                of(finalizingReservation),
            );

            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.reservationFinalized).toBe(false);
            expect(pollReservationStatus).toHaveBeenCalledWith(
                'res-123',
                mockReservationService,
                expect.any(Function),
            );
        });

        it('should call callback and set reservationFinalized to true when poll returns COMPLETE', async () => {
            const finalizingReservation = {
                ...mockReservationInfo,
                status: 'OFFLINE_FINALIZING',
            };
            const completedReservation = {
                ...mockReservationInfo,
                status: 'COMPLETE',
            };
            mockReservationService.getReservationInfo.mockReturnValueOnce(
                of(finalizingReservation),
            );

            let capturedCallback: ((res: ReservationInfo) => void) | null =
                null;
            (
                pollReservationStatus as ReturnType<typeof vi.fn>
            ).mockImplementation((_, __, callback) => {
                capturedCallback = callback;
            });

            component.ngOnInit();
            await new Promise((resolve) => setTimeout(resolve, 100));

            expect(component.reservationFinalized).toBe(false);

            if (capturedCallback) {
                capturedCallback(completedReservation);
            }

            expect(component.reservationFinalized).toBe(true);
            expect(component.reservationInfo.status).toBe('COMPLETE');
        });
    });

    describe('invoiceAvailable', () => {
        it('should return true when all conditions are met', () => {
            component.reservationFinalized = true;
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = mockReservationInfo;
            expect(component.invoiceAvailable).toBe(true);
        });

        it('should return false when reservationFinalized is false', () => {
            component.reservationFinalized = false;
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = mockReservationInfo;
            expect(component.invoiceAvailable).toBe(false);
        });

        it('should return false when userCanDownloadReceiptOrInvoice is false', () => {
            component.reservationFinalized = true;
            component.purchaseContext = {
                ...mockPurchaseContext,
                invoicingConfiguration: {
                    ...mockPurchaseContext.invoicingConfiguration,
                    userCanDownloadReceiptOrInvoice: false,
                },
            } as PurchaseContext;
            component.reservationInfo = mockReservationInfo;
            expect(component.invoiceAvailable).toBe(false);
        });

        it('should return false when invoiceNumber is null', () => {
            component.reservationFinalized = true;
            component.purchaseContext = mockPurchaseContext;
            component.reservationInfo = {
                ...mockReservationInfo,
                invoiceNumber: null,
            };
            expect(component.invoiceAvailable).toBe(false);
        });
    });

    describe('translatedLocalization', () => {
        it('should return null when customPaymentMethodDetails is not set', () => {
            component.customPaymentMethodDetails = undefined;
            expect(component.translatedLocalization).toBeNull();
        });

        it('should return english localization when current lang is not available', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const result = component.translatedLocalization;
            expect(result).not.toBeNull();
            expect(result.paymentName).toBe('Custom Bank Transfer');
        });

        it('should return localized name when current lang is available', async () => {
            mockI18nService.getCurrentLang.mockReturnValue('es');
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const result = component.translatedLocalization;
            expect(result.paymentName).toBe('Transferencia Bancaria Custom');
        });

        it('should return first localization when current lang is not in localizations', async () => {
            mockI18nService.getCurrentLang.mockReturnValue('fr');
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const result = component.translatedLocalization;
            expect(result).not.toBeNull();
        });
    });
});
