import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
    type ComponentFixture,
    TestBed,
    CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core/testing';
import { UntypedFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { PaymentMethodSelectorComponent } from './payment-method-selector.component';
import { ReservationService } from '../../shared/reservation.service';
import { I18nService } from '../../shared/i18n.service';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationInfo } from '../../model/reservation-info';
import type {
    CustomOfflinePayment,
    PaymentProxyWithParameters,
} from '../../model/event';

describe('PaymentMethodSelectorComponent', () => {
    let component: PaymentMethodSelectorComponent;
    let fixture: ComponentFixture<PaymentMethodSelectorComponent>;

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
        activePaymentMethods: {
            CREDIT_CARD: {
                paymentMethodId: 'CREDIT_CARD',
                paymentProxy: 'STRIPE',
            } as PaymentProxyWithParameters,
            PAYPAL: {
                paymentMethodId: 'PAYPAL',
                paymentProxy: 'PAYPAL',
            } as PaymentProxyWithParameters,
        },
        metadata: {
            hideContactData: false,
            lockEmailEdit: false,
            hideConfirmationButtons: false,
            readyForConfirmation: false,
            finalized: false,
        },
    };

    const mockReservationService = {
        getApplicableCustomPaymentMethodDetails: vi.fn(() => of([])),
    };

    const mockI18nService = {
        getCurrentLang: vi.fn(() => 'en'),
    };

    const formBuilder = new UntypedFormBuilder();

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [PaymentMethodSelectorComponent],
            imports: [ReactiveFormsModule],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {
                    provide: ReservationService,
                    useValue: mockReservationService,
                },
                { provide: I18nService, useValue: mockI18nService },
            ],
        });

        fixture = TestBed.createComponent(PaymentMethodSelectorComponent);
        component = fixture.componentInstance;

        component.purchaseContext = mockPurchaseContext;
        component.reservationInfo = mockReservationInfo;
        component.overviewForm = formBuilder.group({
            selectedPaymentMethod: 'CREDIT_CARD',
            paymentProxy: 'STRIPE',
        });
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load custom payment methods', () => {
            component.ngOnInit();
            expect(
                mockReservationService.getApplicableCustomPaymentMethodDetails,
            ).toHaveBeenCalledWith('res-123');
        });

        it('should subscribe to selectedPaymentMethod changes', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.overviewForm
                .get('selectedPaymentMethod')
                .setValue('PAYPAL');

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.overviewForm.get('paymentProxy').value).toBe(
                'PAYPAL',
            );
        });
    });

    describe('activePaymentMethods', () => {
        it('should return reservation info activePaymentMethods', () => {
            expect(component.activePaymentMethods).toEqual(
                mockReservationInfo.activePaymentMethods,
            );
        });
    });

    describe('sortedAvailablePaymentMethodIDs', () => {
        it('should return sorted keys of activePaymentMethods', () => {
            const result = component.sortedAvailablePaymentMethodIDs;
            expect(result).toEqual(['CREDIT_CARD', 'PAYPAL'].sort());
        });
    });

    describe('activePaymentsCount', () => {
        it('should return number of active payment methods', () => {
            expect(component.activePaymentsCount).toBe(2);
        });
    });

    describe('verticalLayout', () => {
        it('should return true when more than 3 payment methods', () => {
            component.reservationInfo = {
                ...mockReservationInfo,
                activePaymentMethods: {
                    METHOD_1: {
                        paymentMethodId: 'METHOD_1',
                        paymentProxy: 'P1',
                    } as PaymentProxyWithParameters,
                    METHOD_2: {
                        paymentMethodId: 'METHOD_2',
                        paymentProxy: 'P2',
                    } as PaymentProxyWithParameters,
                    METHOD_3: {
                        paymentMethodId: 'METHOD_3',
                        paymentProxy: 'P3',
                    } as PaymentProxyWithParameters,
                    METHOD_4: {
                        paymentMethodId: 'METHOD_4',
                        paymentProxy: 'P4',
                    } as PaymentProxyWithParameters,
                },
            };
            expect(component.verticalLayout).toBe(true);
        });

        it('should return false when 3 or fewer payment methods', () => {
            component.reservationInfo = {
                ...mockReservationInfo,
                activePaymentMethods: {
                    CREDIT_CARD: {
                        paymentMethodId: 'CREDIT_CARD',
                        paymentProxy: 'STRIPE',
                    } as PaymentProxyWithParameters,
                    PAYPAL: {
                        paymentMethodId: 'PAYPAL',
                        paymentProxy: 'PAYPAL',
                    } as PaymentProxyWithParameters,
                },
            };
            expect(component.verticalLayout).toBe(false);
        });
    });

    describe('selectedPaymentMethod', () => {
        it('should return current selected payment method id', () => {
            expect(component.selectedPaymentMethod).toBe('CREDIT_CARD');
        });
    });

    describe('selectedPaymentProxy', () => {
        it('should return current payment proxy', () => {
            expect(component.selectedPaymentProxy).toBe('STRIPE');
        });
    });

    describe('getPaymentMethodDetails', () => {
        it('should return details for known payment method', () => {
            const details = component.getPaymentMethodDetails('CREDIT_CARD');
            expect(details).toBeDefined();
            expect(details.labelKey).toBeDefined();
        });

        it('should return translated name for custom offline payment', async () => {
            const customMethod: CustomOfflinePayment = {
                paymentMethodId: 'CUSTOM_OFFLINE',
                paymentMethodName: 'Bank Transfer',
                paymentProxy: 'CUSTOM',
                localizations: {
                    en: { paymentName: 'Bank Transfer' },
                    es: { paymentName: 'Transferencia Bancaria' },
                },
            };
            mockReservationService.getApplicableCustomPaymentMethodDetails.mockReturnValue(
                of([customMethod]),
            );
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const details = component.getPaymentMethodDetails('CUSTOM_OFFLINE');
            expect(details.labelKey).toBe('Bank Transfer');
        });

        it('should return UNKNOWN for unrecognized payment method', () => {
            const details = component.getPaymentMethodDetails('UNKNOWN_METHOD');
            expect(details.labelKey).toBe('UNKNOWN PAYMENT UNKNOWN_METHOD');
        });
    });

    describe('registerCurrentPaymentProvider', () => {
        it('should emit selectedPaymentProvider event', () => {
            let emittedProvider: any = null;
            component.selectedPaymentProvider.subscribe(
                (p) => (emittedProvider = p),
            );

            const mockProvider = { pay: () => of({}) } as any;
            component.registerCurrentPaymentProvider(mockProvider);

            expect(emittedProvider).toBe(mockProvider);
        });
    });
});
