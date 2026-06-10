import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { UntypedFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { OverviewComponent } from './overview.component';
import { ReservationExpiredComponent } from '../expired-notification/reservation-expired.component';
import { ReservationService } from '../../shared/reservation.service';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { AnalyticsService } from '../../shared/analytics.service';
import { FeedbackService } from '../../shared/feedback/feedback.service';
import { I18nService } from '../../shared/i18n.service';
import type { PurchaseContext } from '../../model/purchase-context';
import type { ReservationInfo } from '../../model/reservation-info';
import { SimplePaymentProvider } from '../../payment/payment-provider';

describe('OverviewComponent', () => {
    let component: OverviewComponent;
    let fixture: ComponentFixture<OverviewComponent>;

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

    const mockActivatedRoute = {
        data: of({ type: 'event', publicIdentifierParameter: 'eventShortName' }),
        params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
        queryParams: of({}),
        snapshot: {
            queryParamMap: { has: () => false, get: () => null },
            params: { eventShortName: 'test-event', reservationId: 'res-123' },
        },
    };

const mockRouter = {
        navigate: vi.fn(),
    };

    const mockReservationService = {
        getReservationInfo: vi.fn(() => of(mockReservationInfo)),
        getApplicableCustomPaymentMethodDetails: vi.fn(() => of([])),
        confirmOverview: vi.fn(() => of({ success: true, value: { redirect: false, redirectUrl: null } })),
        backToBooking: vi.fn(() => of(true)),
        cancelPendingReservation: vi.fn(() => of(true)),
        removePaymentToken: vi.fn(() => of(true)),
        forcePaymentStatusCheck: vi.fn(() => of({ success: false })),
        applySubscriptionCode: vi.fn(() => of({ success: true })),
        removeSubscription: vi.fn(() => of(true)),
    };

    const mockPurchaseContextService = {
        getContext: vi.fn(() => of(mockPurchaseContext)),
    };

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
            result: Promise.resolve(),
            componentInstance: {},
        })),
    };

    const mockFeedbackService = {
        showError: vi.fn(),
        showSuccess: vi.fn(),
        showInfo: vi.fn(),
    };

    const mockFormBuilder = new UntypedFormBuilder();

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [OverviewComponent],
            imports: [ReactiveFormsModule, NgbModule],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: Router, useValue: mockRouter },
                { provide: ReservationService, useValue: mockReservationService },
                { provide: PurchaseContextService, useValue: mockPurchaseContextService },
                { provide: UntypedFormBuilder, useValue: mockFormBuilder },
                { provide: I18nService, useValue: mockI18nService },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: AnalyticsService, useValue: mockAnalyticsService },
                { provide: NgbModal, useValue: mockModalService },
                { provide: FeedbackService, useValue: mockFeedbackService },
            ],
        });

        fixture = TestBed.createComponent(OverviewComponent);
        component = fixture.componentInstance;
        component.subscriptionInput = { nativeElement: { focus: vi.fn(), value: null } } as any;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load reservation and context', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith('event', 'test-event');
            expect(mockReservationService.getReservationInfo).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith('reservation-page.header.title', mockPurchaseContext);
        });

        it('should load custom payment methods', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.getApplicableCustomPaymentMethodDetails).toHaveBeenCalledWith('res-123');
        });

        it('should initialize subscription code form', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.subscriptionCodeForm).toBeDefined();
            expect(component.subscriptionCodeForm.get('subscriptionCode')).toBeDefined();
        });
    });

    describe('loadReservation', () => {
        it('should load reservation info and setup payment methods', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.loadReservation();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.getReservationInfo).toHaveBeenCalled();
            expect(component.reservationInfo).toEqual(mockReservationInfo);
        });

        it('should set free payment when order is free', async () => {
            const freeReservation = { ...mockReservationInfo, orderSummary: { ...mockReservationInfo.orderSummary, free: true } };
            mockReservationService.getReservationInfo.mockReturnValue(of(freeReservation));

            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.loadReservation();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.selectedPaymentProvider).toBeInstanceOf(SimplePaymentProvider);
        });
    });

    describe('paymentMethodsCount', () => {
        it('should return number of active payment methods', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.activePaymentMethods = {
                CREDIT_CARD: { paymentMethodId: 'CREDIT_CARD', paymentProxy: 'STRIPE' } as any,
                PAYPAL: { paymentMethodId: 'PAYPAL', paymentProxy: 'PAYPAL' } as any,
            };

            expect(component.paymentMethodsCount()).toBe(2);
        });
    });

    describe('getSinglePaymentMethod', () => {
        it('should return first payment method key when only one', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.activePaymentMethods = {
                CREDIT_CARD: { paymentMethodId: 'CREDIT_CARD', paymentProxy: 'STRIPE' } as any,
            };

            expect(component.getSinglePaymentMethod()).toBe('CREDIT_CARD');
        });
    });

    describe('back', () => {
        it('should navigate to booking page', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.publicIdentifier = 'test-event';
            component.reservationId = 'res-123';
            component.purchaseContextType = 'event';

            component.back();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.backToBooking).toHaveBeenCalledWith('res-123');
            expect(mockRouter.navigate).toHaveBeenCalledWith(['event', 'test-event', 'reservation', 'res-123', 'book'], {});
        });
    });

    describe('registerCurrentPaymentProvider', () => {
        it('should set selected payment provider', () => {
            const provider = new SimplePaymentProvider();
            component.registerCurrentPaymentProvider(provider);
            expect(component.selectedPaymentProvider).toBe(provider);
        });
    });

    describe('clearToken', () => {
        it('should call removePaymentToken and reload reservation', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.clearToken();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.removePaymentToken).toHaveBeenCalledWith('res-123');
        });
    });

    describe('handleRecaptchaResponse', () => {
        it('should set captcha value in form', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.overviewForm = mockFormBuilder.group({
                captcha: null,
            });

            component.handleRecaptchaResponse('test-captcha-token');

            expect(component.overviewForm.get('captcha').value).toBe('test-captcha-token');
        });
    });

    describe('applySubscription', () => {
        it('should apply subscription code and reload', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';
            component.reservationInfo = mockReservationInfo;
            component.subscriptionCodeForm = mockFormBuilder.group({
                subscriptionCode: 'SUB123',
            });

            component.applySubscription();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.applySubscriptionCode).toHaveBeenCalledWith('res-123', 'SUB123', 'john@example.com');
            expect(mockFeedbackService.showSuccess).toHaveBeenCalled();
        });

        it('should not apply if form is invalid', () => {
            component.reservationInfo = mockReservationInfo;
            component.reservationId = 'res-123';
            component.subscriptionCodeForm = mockFormBuilder.group({
                subscriptionCode: ['', Validators.required],
            });
            const control = component.subscriptionCodeForm.get('subscriptionCode');
            expect(control?.value).toBe('');
            expect(component.subscriptionCodeForm.valid).toBe(false);
            mockReservationService.applySubscriptionCode.mockClear();

            component.applySubscription();

            expect(mockReservationService.applySubscriptionCode).not.toHaveBeenCalled();
        });
    });

    describe('toggleSubscriptionFormVisible', () => {
        it('should toggle displaySubscriptionForm', () => {
            component.displaySubscriptionForm = false;

            component.toggleSubscriptionFormVisible();

            expect(component.displaySubscriptionForm).toBe(true);
        });

        it('should reset input when hiding form', () => {
            component.subscriptionInput = { nativeElement: { value: 'previous-value' } } as any;
            component.displaySubscriptionForm = true;

            component.toggleSubscriptionFormVisible();

            expect(component.displaySubscriptionForm).toBe(false);
        });
    });

    describe('acceptedPrivacyAndTermAndConditions', () => {
        it('should return true when privacy policy is not required', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.privacyPolicyUrl = null;
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: false,
            });

            expect(component.acceptedPrivacyAndTermAndConditions).toBe(true);
        });

        it('should return true when both are accepted', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.privacyPolicyUrl = 'https://example.com/privacy';
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: true,
            });

            expect(component.acceptedPrivacyAndTermAndConditions).toBe(true);
        });

        it('should return false when privacy is not accepted', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.privacyPolicyUrl = 'https://example.com/privacy';
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: false,
            });

            expect(component.acceptedPrivacyAndTermAndConditions).toBe(false);
        });
    });

    describe('enabledItalyEInvoicing', () => {
        it('should return true when enabled and italianEInvoicing present', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'PEC',
                fiscalCode: 'RSSMRA85T10A562K',
                addresseeCode: '',
                pec: 'test@example.it',
                reference: 'test@example.it',
                splitPayment: false,
            };

            expect(component.enabledItalyEInvoicing).toBe(true);
        });

        it('should return false when italianEInvoicing is not present', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = undefined;

            expect(component.enabledItalyEInvoicing).toBe(false);
        });
    });

    describe('hasTaxId', () => {
        it('should return true when invoice requested and taxId present', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.invoiceRequested = true;
            component.reservationInfo.skipVatNr = false;
            component.reservationInfo.billingDetails.taxId = '123456';

            expect(component.hasTaxId).toBe(true);
        });

        it('should return false when skipVatNr is true', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.invoiceRequested = true;
            component.reservationInfo.skipVatNr = true;
            component.reservationInfo.billingDetails.taxId = '123456';

            expect(component.hasTaxId).toBe(false);
        });
    });

    describe('paymentMethodDeferred', () => {
        it('should return false when token is acquired', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.tokenAcquired = true;

            expect(component.paymentMethodDeferred).toBe(false);
        });

        it('should check selectedPaymentProvider when token not acquired', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.tokenAcquired = false;
            component.selectedPaymentProvider = new SimplePaymentProvider();

            expect(component.paymentMethodDeferred).toBe(true);
        });
    });

    describe('appliedSubscription', () => {
        it('should return true when subscription is applied', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.orderSummary.summary = [
                { type: 'APPLIED_SUBSCRIPTION', name: 'Subscription', amount: 1, price: '0', subTotal: '0', taxPercentage: '0' },
            ];

            expect(component.appliedSubscription).toBe(true);
        });

        it('should return false when no subscription applied', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.orderSummary.summary = [
                { type: 'TICKET', name: 'Ticket', amount: 1, price: '100', subTotal: '100', taxPercentage: '22' },
            ];

            expect(component.appliedSubscription).toBe(false);
        });
    });

    describe('displayRemoveSubscription', () => {
        it('should return true for event purchase context', () => {
            component.purchaseContextType = 'event';
            expect(component.displayRemoveSubscription).toBe(true);
        });

        it('should return false for subscription purchase context', () => {
            component.purchaseContextType = 'subscription';
            expect(component.displayRemoveSubscription).toBe(false);
        });
    });

    describe('taxIdMessageKey', () => {
        it('should return fiscalCode for Italy', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.billingDetails.country = 'IT';
            expect(component.taxIdMessageKey).toBe('invoice-fields.fiscalCode');
        });

        it('should return tax-id for other countries', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationInfo.billingDetails.country = 'US';
            expect(component.taxIdMessageKey).toBe('invoice-fields.tax-id');
        });
    });

    describe('italyEInvoicingReference', () => {
        it('should return reference when italianEInvoicing is present', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'PEC',
                fiscalCode: 'RSSMRA85T10A562K',
                addresseeCode: '',
                pec: 'test@example.it',
                reference: 'test-reference',
                splitPayment: false,
            };

            expect(component.italyEInvoicingReference).toBe('test-reference');
        });

        it('should return empty string when not enabled', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = false;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'PEC',
                fiscalCode: 'RSSMRA85T10A562K',
                addresseeCode: '',
                pec: 'test@example.it',
                reference: 'test-reference',
                splitPayment: false,
            };

            expect(component.italyEInvoicingReference).toBe('');
        });

        it('should return empty string when italianEInvoicing is null', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = null;

            expect(component.italyEInvoicingReference).toBe('');
        });
    });

    describe('italyEInvoicingSelectedAddresseeKey', () => {
        it('should return addressee-code when referenceType is ADDRESSEE_CODE', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'ADDRESSEE_CODE',
                fiscalCode: '',
                addresseeCode: 'CODE123',
                pec: '',
                reference: '',
                splitPayment: false,
            };

            expect(component.italyEInvoicingSelectedAddresseeKey).toBe('invoice-fields.addressee-code');
        });

        it('should return pec when referenceType is not ADDRESSEE_CODE', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'PEC',
                fiscalCode: '',
                addresseeCode: '',
                pec: 'pec@example.it',
                reference: '',
                splitPayment: false,
            };

            expect(component.italyEInvoicingSelectedAddresseeKey).toBe('invoice-fields.pec');
        });

        it('should return empty string when not enabled', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = false;

            expect(component.italyEInvoicingSelectedAddresseeKey).toBe('');
        });
    });

    describe('italyEInvoicingFiscalCode', () => {
        it('should return fiscalCode when italianEInvoicing is present', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'PEC',
                fiscalCode: 'RSSMRA85T10A562K',
                addresseeCode: '',
                pec: 'test@example.it',
                reference: '',
                splitPayment: false,
            };

            expect(component.italyEInvoicingFiscalCode).toBe('RSSMRA85T10A562K');
        });

        it('should return empty string when italianEInvoicing is null', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = true;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = null;

            expect(component.italyEInvoicingFiscalCode).toBe('');
        });

        it('should return empty string when not enabled', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.purchaseContext.invoicingConfiguration.enabledItalyEInvoicing = false;
            component.reservationInfo.billingDetails.invoicingAdditionalInfo.italianEInvoicing = {
                referenceType: 'PEC',
                fiscalCode: 'RSSMRA85T10A562K',
                addresseeCode: '',
                pec: 'test@example.it',
                reference: '',
                splitPayment: false,
            };

            expect(component.italyEInvoicingFiscalCode).toBe('');
        });
    });

    describe('forceCheck', () => {
        it('should call forcePaymentStatusCheck service', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockReservationService.forcePaymentStatusCheck).toHaveBeenCalledWith('res-123');
        });
    });

    describe('removeSubscription', () => {
        it('should open modal and call removeSubscription service on confirm', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';

            mockModalService.open.mockReturnValue({
                result: Promise.resolve(true),
            });

            component.removeSubscription({ type: 'SUBSCRIPTION' } as any);

            await new Promise(resolve => setTimeout(resolve, 200));
            expect(mockModalService.open).toHaveBeenCalled();
            expect(mockReservationService.removeSubscription).toHaveBeenCalledWith('res-123');
            expect(mockFeedbackService.showInfo).toHaveBeenCalled();
        });
    });

    describe('getPaymentMethodMatchingProxy', () => {
        it('should return matching payment method id', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.activePaymentMethods = {
                CREDIT_CARD: { paymentMethodId: 'CREDIT_CARD', paymentProxy: 'STRIPE' } as any,
                PAYPAL: { paymentMethodId: 'PAYPAL', paymentProxy: 'PAYPAL' } as any,
            };

            const result = component['getPaymentMethodMatchingProxy']('STRIPE');
            expect(result).toBe('CREDIT_CARD');
        });

        it('should return null when no matching proxy', async () => {
            component.ngOnInit();

            await new Promise(resolve => setTimeout(resolve, 100));
            component.activePaymentMethods = {
                CREDIT_CARD: { paymentMethodId: 'CREDIT_CARD', paymentProxy: 'STRIPE' } as any,
            };

            const result = component['getPaymentMethodMatchingProxy']('UNKNOWN');
            expect(result).toBeNull();
        });
    });

    describe('confirm', () => {
        it('should return early if form is invalid', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: false,
            });
            component.selectedPaymentProvider = null;
            component.submitting = false;

            component.confirm();

            expect(component.submitting).toBe(false);
        });

        it('should return early if no payment provider selected', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                selectedPaymentMethod: null,
            });
            component.selectedPaymentProvider = null;
            component.submitting = false;

            component.confirm();

            expect(component.submitting).toBe(false);
        });

        it('should handle successful payment with redirect', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: true,
                selectedPaymentMethod: 'CREDIT_CARD',
                paymentProxy: 'STRIPE',
                gatewayToken: null,
                captcha: null,
            });
            component.reservationInfo = mockReservationInfo;
            component.purchaseContext = mockPurchaseContext;

            const mockPaymentProvider = {
                statusNotifications: vi.fn(() => of(null)),
                pay: vi.fn(() => of({ success: true, gatewayToken: 'token-123' })),
            } as any;

            component.selectedPaymentProvider = mockPaymentProvider;

            mockReservationService.confirmOverview.mockReturnValue(of({
                success: true,
                value: { redirect: true, redirectUrl: 'https://payment.example.com' },
            }));

            component.confirm();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockPaymentProvider.pay).toHaveBeenCalled();
            expect(mockReservationService.confirmOverview).toHaveBeenCalled();
        });

        it('should handle successful payment without redirect', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: true,
                selectedPaymentMethod: 'CREDIT_CARD',
                paymentProxy: 'STRIPE',
                gatewayToken: null,
                captcha: null,
            });
            component.reservationInfo = mockReservationInfo;
            component.purchaseContext = mockPurchaseContext;
            component.route.snapshot.queryParams = {};
            component.route.snapshot.params = {};

            const mockPaymentProvider = {
                statusNotifications: vi.fn(() => of(null)),
                pay: vi.fn(() => of({ success: true, gatewayToken: 'token-123' })),
            } as any;

            component.selectedPaymentProvider = mockPaymentProvider;

            mockReservationService.confirmOverview.mockReturnValue(of({
                success: true,
                value: { redirect: false, redirectUrl: null },
            }));

            component.confirm();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(mockRouter.navigate).toHaveBeenCalledWith(
                ['event', 'test-event', 'reservation', 'res-123', 'success'],
                expect.any(Object),
            );
        });

        it('should handle confirmOverview failure with validation errors', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: true,
                selectedPaymentMethod: 'CREDIT_CARD',
                paymentProxy: 'STRIPE',
                gatewayToken: null,
                captcha: null,
            });
            component.reservationInfo = mockReservationInfo;
            component.purchaseContext = mockPurchaseContext;

            const mockPaymentProvider = {
                statusNotifications: vi.fn(() => of(null)),
                pay: vi.fn(() => of({ success: true, gatewayToken: 'token-123' })),
            } as any;

            component.selectedPaymentProvider = mockPaymentProvider;

            mockReservationService.confirmOverview.mockReturnValue(of({
                success: false,
                validationErrors: [{ fieldName: 'email', code: 'error.invalid', arguments: {} }],
            }));

            component.confirm();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.submitting).toBe(false);
            expect(component.globalErrors).toBeDefined();
        });

        it('should handle payment error from pay()', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';
            component.overviewForm = mockFormBuilder.group({
                termAndConditionsAccepted: true,
                privacyPolicyAccepted: true,
                selectedPaymentMethod: 'CREDIT_CARD',
                paymentProxy: 'STRIPE',
                gatewayToken: null,
                captcha: null,
            });
            component.reservationInfo = mockReservationInfo;
            component.purchaseContext = mockPurchaseContext;

            const mockPaymentProvider = {
                statusNotifications: vi.fn(() => of(null)),
                pay: vi.fn(() => throwError(() => ({ message: 'Payment failed' }))),
            } as any;

            component.selectedPaymentProvider = mockPaymentProvider;

            component.confirm();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.submitting).toBe(false);
            expect(component.globalErrors).toBeDefined();
        });
    });

    describe('handleExpired', () => {
        it('should open modal and navigate when expired is true', async () => {
            component.purchaseContextType = 'event';
            component.publicIdentifier = 'test-event';

            mockModalService.open.mockClear();
            mockModalService.open.mockReturnValue({
                result: Promise.resolve(),
            });

            component.handleExpired(true);

            await new Promise(resolve => setTimeout(resolve, 50));
            expect(mockModalService.open).toHaveBeenCalledWith(
                ReservationExpiredComponent,
                expect.objectContaining({ centered: true, backdrop: 'static' }),
            );
        });

        it('should not open modal if already expired', async () => {
            component.expired = true;
            mockModalService.open.mockClear();

            component.handleExpired(true);

            expect(mockModalService.open).not.toHaveBeenCalled();
        });
    });

    describe('forceCheck error handling', () => {
        it('should handle forceCheck error', async () => {
            component.ngOnInit();
            await new Promise(resolve => setTimeout(resolve, 100));
            component.reservationId = 'res-123';
            component.reservationInfo = mockReservationInfo;
            component.purchaseContext = mockPurchaseContext;

            mockReservationService.forcePaymentStatusCheck.mockReturnValue(throwError(() => ({ message: 'Check failed' })));

            component.forceCheck();

            await new Promise(resolve => setTimeout(resolve, 100));
            expect(component.forceCheckInProgress).toBe(true);
        });
    });
});