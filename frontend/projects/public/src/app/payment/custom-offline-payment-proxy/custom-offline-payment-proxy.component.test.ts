import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { CustomOfflinePaymentProxyComponent } from './custom-offline-payment-proxy.component';
import { I18nService } from '../../shared/i18n.service';
import { mockReservationInfo, mockCustomOfflinePayments } from '../test-data';

describe('CustomOfflinePaymentProxyComponent', () => {
    let component: CustomOfflinePaymentProxyComponent;
    let fixture: ComponentFixture<CustomOfflinePaymentProxyComponent>;

    const mockI18nService = {
        getCurrentLang: vi.fn(() => 'en'),
    };

    beforeEach(async () => {
        vi.clearAllMocks();

        await TestBed.configureTestingModule({
            declarations: [CustomOfflinePaymentProxyComponent],
            providers: [
                { provide: I18nService, useValue: mockI18nService },
            ],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(CustomOfflinePaymentProxyComponent);
        component = fixture.componentInstance;
    });

    describe('matchProxyAndMethod', () => {
        it('should return false when method is undefined', () => {
            component.method = undefined;
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'CUSTOM_OFFLINE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when availableMethods is undefined', () => {
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = undefined;
            component.proxy = 'CUSTOM_OFFLINE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when availableMethods is empty', () => {
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = [];
            component.proxy = 'CUSTOM_OFFLINE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when proxy is not CUSTOM_OFFLINE', () => {
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'STRIPE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return true when method and proxy match', () => {
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'CUSTOM_OFFLINE';

            expect(component.matchProxyAndMethod).toBe(true);
        });

        it('should return false when method does not match any available method', () => {
            component.method = 'UNKNOWN_METHOD';
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'CUSTOM_OFFLINE';

            expect(component.matchProxyAndMethod).toBe(false);
        });
    });

    describe('ngOnChanges', () => {
        it('should emit paymentProvider when matchProxyAndMethod and method changes', () => {
            const emittedProviders: any[] = [];
            component.paymentProvider.subscribe((p) => emittedProviders.push(p));

            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'CUSTOM_OFFLINE';

            component.ngOnChanges({ method: { currentValue: 'CUSTOM_OFFLINE_1' } } as any);

            expect(emittedProviders.length).toBe(1);
            expect(emittedProviders[0]).toBeDefined();
        });

        it('should emit paymentProvider when availableMethods changes', () => {
            const emittedProviders: any[] = [];
            component.paymentProvider.subscribe((p) => emittedProviders.push(p));

            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'CUSTOM_OFFLINE';

            component.ngOnChanges({ availableMethods: { currentValue: mockCustomOfflinePayments } } as any);

            expect(emittedProviders.length).toBe(1);
        });

        it('should not emit when proxy does not match', () => {
            let emitCount = 0;
            component.paymentProvider.subscribe(() => emitCount++);

            component.method = 'CREDIT_CARD';
            component.availableMethods = mockCustomOfflinePayments;
            component.proxy = 'STRIPE';

            component.ngOnChanges({ method: { currentValue: 'CREDIT_CARD' } } as any);

            expect(emitCount).toBe(0);
        });
    });

    describe('selectedPaymentMethodDescription', () => {
        it('should return English localization when currentLang matches', () => {
            mockI18nService.getCurrentLang.mockReturnValue('en');
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;

            const description = component.selectedPaymentMethodDescription;

            expect(description).toBe('Bank Transfer - English');
        });

        it('should return first localization when currentLang does not match', () => {
            mockI18nService.getCurrentLang.mockReturnValue('fr');
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;

            const description = component.selectedPaymentMethodDescription;

            expect(description).toBe('Bank Transfer - English');
        });

        it('should return empty string when method is not found', () => {
            mockI18nService.getCurrentLang.mockReturnValue('en');
            component.method = 'UNKNOWN_METHOD';
            component.availableMethods = mockCustomOfflinePayments;

            const description = component.selectedPaymentMethodDescription;

            expect(description).toBe('');
        });

        it('should return empty string when availableMethods is empty', () => {
            mockI18nService.getCurrentLang.mockReturnValue('en');
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = [];

            const description = component.selectedPaymentMethodDescription;

            expect(description).toBe('');
        });

        it('should return localized description for matching language', () => {
            mockI18nService.getCurrentLang.mockReturnValue('es');
            component.method = 'CUSTOM_OFFLINE_1';
            component.availableMethods = mockCustomOfflinePayments;

            const description = component.selectedPaymentMethodDescription;

            expect(description).toBe('Trasferencia Bancaria - Español');
        });
    });
});