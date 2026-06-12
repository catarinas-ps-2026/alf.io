import { describe, it, expect } from 'vitest';
import {
    Event,
    InvoicingConfiguration,
    Language,
    PaymentProxyWithParameters,
    EventFormat,
    PaymentMethod,
    CustomOfflinePayment,
    StaticPaymentMethodNames,
    PaymentProxy,
    PaymentMethodDetails,
    staticPaymentMethodDetails,
    CaptchaConfiguration,
    AssignmentConfiguration,
    PromotionsConfiguration,
    CurrencyDescriptor,
} from './event';

describe('event.ts', () => {
    describe('Event class', () => {
        it('should create Event instance', () => {
            const event = new Event();
            expect(event).toBeDefined();
        });

        it('should allow setting all properties', () => {
            const event = new Event();
            event.shortName = 'test-event';
            event.title = { en: 'Test Event' };
            event.format = 'IN_PERSON';
            event.currency = 'USD';
            event.free = false;
            event.availableTicketsCount = 100;
            event.canApplySubscriptions = true;
            event.offlinePaymentConfiguration = { showOnlyBasicInstructions: true };

            expect(event.shortName).toBe('test-event');
            expect(event.title.en).toBe('Test Event');
            expect(event.format).toBe('IN_PERSON');
            expect(event.currency).toBe('USD');
            expect(event.free).toBe(false);
            expect(event.availableTicketsCount).toBe(100);
            expect(event.canApplySubscriptions).toBe(true);
            expect(event.offlinePaymentConfiguration.showOnlyBasicInstructions).toBe(true);
        });
    });

    describe('InvoicingConfiguration', () => {
        it('should create instance with all properties', () => {
            const config = new InvoicingConfiguration();
            config.userCanDownloadReceiptOrInvoice = true;
            config.euVatCheckingEnabled = true;
            config.invoiceAllowed = true;
            config.onlyInvoice = false;
            config.customerReferenceEnabled = true;
            config.enabledItalyEInvoicing = true;
            config.vatNumberStrictlyRequired = true;

            expect(config.userCanDownloadReceiptOrInvoice).toBe(true);
            expect(config.euVatCheckingEnabled).toBe(true);
            expect(config.invoiceAllowed).toBe(true);
            expect(config.onlyInvoice).toBe(false);
            expect(config.customerReferenceEnabled).toBe(true);
            expect(config.enabledItalyEInvoicing).toBe(true);
            expect(config.vatNumberStrictlyRequired).toBe(true);
        });
    });

    describe('Language', () => {
        it('should create instance with locale and displayLanguage', () => {
            const lang: Language = { locale: 'en', displayLanguage: 'English' };
            expect(lang.locale).toBe('en');
            expect(lang.displayLanguage).toBe('English');
        });
    });

    describe('PaymentProxyWithParameters', () => {
        it('should create instance with paymentProxy and parameters', () => {
            const pp: PaymentProxyWithParameters = {
                paymentProxy: 'STRIPE',
                parameters: { key: 'value' },
            };
            expect(pp.paymentProxy).toBe('STRIPE');
            expect(pp.parameters.key).toBe('value');
        });
    });

    describe('EventFormat type', () => {
        it('should accept IN_PERSON format', () => {
            const format: EventFormat = 'IN_PERSON';
            expect(format).toBe('IN_PERSON');
        });

        it('should accept ONLINE format', () => {
            const format: EventFormat = 'ONLINE';
            expect(format).toBe('ONLINE');
        });

        it('should accept HYBRID format', () => {
            const format: EventFormat = 'HYBRID';
            expect(format).toBe('HYBRID');
        });
    });

    describe('PaymentMethod interface', () => {
        it('should create instance with paymentMethodId and paymentMethodName', () => {
            const method: PaymentMethod = {
                paymentMethodId: 'CREDIT_CARD',
                paymentMethodName: 'Credit Card',
            };
            expect(method.paymentMethodId).toBe('CREDIT_CARD');
            expect(method.paymentMethodName).toBe('Credit Card');
        });
    });

    describe('CustomOfflinePayment interface', () => {
        it('should extend PaymentMethod with localizations', () => {
            const customPayment: CustomOfflinePayment = {
                paymentMethodId: 'CUSTOM_OFFLINE',
                paymentMethodName: 'Custom Payment',
                localizations: {
                    en: {
                        paymentName: 'Custom Payment',
                        paymentDescription: 'Description',
                        paymentInstructions: 'Instructions',
                    },
                },
            };
            expect(customPayment.paymentMethodId).toBe('CUSTOM_OFFLINE');
            expect(customPayment.localizations.en.paymentName).toBe('Custom Payment');
        });
    });

    describe('StaticPaymentMethodNames type', () => {
        it('should accept all static payment method names', () => {
            const methods: StaticPaymentMethodNames[] = [
                'CREDIT_CARD', 'PAYPAL', 'IDEAL', 'BANK_TRANSFER', 'ON_SITE',
                'APPLE_PAY', 'BANCONTACT', 'ING_HOME_PAY', 'BELFIUS',
                'PRZELEWY_24', 'KBC', 'ETRANSFER', 'NONE',
            ];
            methods.forEach((method) => {
                expect(method).toBeDefined();
            });
        });
    });

    describe('PaymentProxy type', () => {
        it('should accept all payment proxy values', () => {
            const proxies: PaymentProxy[] = [
                'STRIPE', 'ON_SITE', 'OFFLINE', 'PAYPAL', 'MOLLIE', 'SAFERPAY', 'CUSTOM_OFFLINE',
            ];
            proxies.forEach((proxy) => {
                expect(proxy).toBeDefined();
            });
        });
    });

    describe('PaymentMethodDetails interface', () => {
        it('should create instance with labelKey and icon', () => {
            const details: PaymentMethodDetails = {
                labelKey: 'reservation-page.credit-card',
                icon: ['fas', 'credit-card'],
            };
            expect(details.labelKey).toBe('reservation-page.credit-card');
            expect(details.icon[0]).toBe('fas');
            expect(details.icon[1]).toBe('credit-card');
        });
    });

    describe('staticPaymentMethodDetails', () => {
        it('should have entries for all static payment methods', () => {
            const methods: StaticPaymentMethodNames[] = [
                'CREDIT_CARD', 'PAYPAL', 'IDEAL', 'BANK_TRANSFER', 'ON_SITE',
                'APPLE_PAY', 'BANCONTACT', 'ING_HOME_PAY', 'BELFIUS',
                'PRZELEWY_24', 'KBC', 'ETRANSFER', 'NONE',
            ];
            methods.forEach((method) => {
                expect(staticPaymentMethodDetails[method]).toBeDefined();
                expect(staticPaymentMethodDetails[method].labelKey).toBeDefined();
                expect(staticPaymentMethodDetails[method].icon).toBeDefined();
            });
        });

        it('should have CREDIT_CARD with correct icon', () => {
            const cc = staticPaymentMethodDetails['CREDIT_CARD'];
            expect(cc.labelKey).toBe('reservation-page.credit-card');
            expect(cc.icon).toEqual(['fas', 'credit-card']);
        });

        it('should have PAYPAL with icon', () => {
            const pp = staticPaymentMethodDetails['PAYPAL'];
            expect(pp.labelKey).toBe('reservation-page.paypal');
            expect(pp.icon).toEqual(['fab', 'paypal']);
        });

        it('should have NONE with null labelKey', () => {
            const none = staticPaymentMethodDetails['NONE'];
            expect(none.labelKey).toBeNull();
        });
    });

    describe('CaptchaConfiguration', () => {
        it('should create instance with all properties', () => {
            const config = new CaptchaConfiguration();
            config.captchaForTicketSelection = true;
            config.captchaForOfflinePaymentAndFree = false;
            config.recaptchaApiKey = 'api-key-123';

            expect(config.captchaForTicketSelection).toBe(true);
            expect(config.captchaForOfflinePaymentAndFree).toBe(false);
            expect(config.recaptchaApiKey).toBe('api-key-123');
        });
    });

    describe('AssignmentConfiguration', () => {
        it('should create instance with all properties', () => {
            const config = new AssignmentConfiguration();
            config.forceAssignment = false;
            config.enableAttendeeAutocomplete = true;
            config.enableTicketTransfer = true;

            expect(config.forceAssignment).toBe(false);
            expect(config.enableAttendeeAutocomplete).toBe(true);
            expect(config.enableTicketTransfer).toBe(true);
        });
    });

    describe('PromotionsConfiguration', () => {
        it('should create instance with all properties', () => {
            const config = new PromotionsConfiguration();
            config.hasAccessPromotions = true;
            config.usePartnerCode = false;

            expect(config.hasAccessPromotions).toBe(true);
            expect(config.usePartnerCode).toBe(false);
        });
    });

    describe('CurrencyDescriptor', () => {
        it('should create instance with all properties', () => {
            const descriptor: CurrencyDescriptor = {
                code: 'USD',
                name: 'US Dollar',
                symbol: '$',
                fractionDigits: 2,
            };

            expect(descriptor.code).toBe('USD');
            expect(descriptor.name).toBe('US Dollar');
            expect(descriptor.symbol).toBe('$');
            expect(descriptor.fractionDigits).toBe(2);
        });
    });
});