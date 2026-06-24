import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
    type ComponentFixture,
    TestBed,
    CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { UntypedFormGroup } from '@angular/forms';
import { OfflinePaymentProxyComponent } from './offline-payment-proxy.component';

describe('OfflinePaymentProxyComponent', () => {
    let component: OfflinePaymentProxyComponent;
    let fixture: ComponentFixture<OfflinePaymentProxyComponent>;

    let mockFormGroup: {
        get: ReturnType<typeof vi.fn>;
    };

    beforeEach(async () => {
        vi.clearAllMocks();

        const captchaControl = {
            setValue: vi.fn(),
        };

        mockFormGroup = {
            get: vi.fn(() => captchaControl),
        };

        await TestBed.configureTestingModule({
            declarations: [OfflinePaymentProxyComponent],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(OfflinePaymentProxyComponent);
        component = fixture.componentInstance;
        component.overviewForm = mockFormGroup as unknown as UntypedFormGroup;
    });

    describe('matchProxyAndMethod', () => {
        it('should return true when method is BANK_TRANSFER and proxy is OFFLINE', () => {
            component.method = 'BANK_TRANSFER';
            component.proxy = 'OFFLINE';

            expect(component.matchProxyAndMethod).toBe(true);
        });

        it('should return false when method is not BANK_TRANSFER', () => {
            component.method = 'CREDIT_CARD';
            component.proxy = 'OFFLINE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when proxy is not OFFLINE', () => {
            component.method = 'BANK_TRANSFER';
            component.proxy = 'STRIPE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when both do not match', () => {
            component.method = 'PAYPAL';
            component.proxy = 'PAYPAL';

            expect(component.matchProxyAndMethod).toBe(false);
        });
    });

    describe('deferred getter', () => {
        it('should return true when parameters.deferred is true', () => {
            component.parameters = { deferred: true };

            expect(component.deferred).toBe(true);
        });

        it('should return falsy when parameters.deferred is false', () => {
            component.parameters = { deferred: false };

            expect(component.deferred).toBeFalsy();
        });

        it('should return undefined when parameters.deferred is undefined', () => {
            component.parameters = {};

            expect(component.deferred).toBeUndefined();
        });

        it('should throw when parameters is null', () => {
            component.parameters = null as any;

            expect(() => component.deferred).toThrow();
        });
    });

    describe('ngOnChanges', () => {
        it('should emit paymentProvider when matchProxyAndMethod and method changes', () => {
            const emittedProviders: any[] = [];
            component.paymentProvider.subscribe((p) =>
                emittedProviders.push(p),
            );

            component.method = 'BANK_TRANSFER';
            component.proxy = 'OFFLINE';

            component.ngOnChanges({
                method: { currentValue: 'BANK_TRANSFER' },
            } as any);

            expect(emittedProviders.length).toBe(1);
            expect(emittedProviders[0]).toBeDefined();
            expect(emittedProviders[0].paymentMethodDeferred).toBe(true);
        });

        it('should not emit when proxy does not match', () => {
            let emitCount = 0;
            component.paymentProvider.subscribe(() => emitCount++);

            component.method = 'CREDIT_CARD';
            component.proxy = 'STRIPE';

            component.ngOnChanges({
                method: { currentValue: 'CREDIT_CARD' },
            } as any);

            expect(emitCount).toBe(0);
        });
    });

    describe('handleRecaptchaResponse', () => {
        it('should set captcha form value', () => {
            const captchaControl = {
                setValue: vi.fn(),
            };
            mockFormGroup.get.mockReturnValue(captchaControl);

            component.handleRecaptchaResponse('recaptcha-token-123');

            expect(mockFormGroup.get).toHaveBeenCalledWith('captcha');
            expect(captchaControl.setValue).toHaveBeenCalledWith(
                'recaptcha-token-123',
            );
        });

        it('should handle empty recaptcha value', () => {
            const captchaControl = {
                setValue: vi.fn(),
            };
            mockFormGroup.get.mockReturnValue(captchaControl);

            component.handleRecaptchaResponse('');

            expect(captchaControl.setValue).toHaveBeenCalledWith('');
        });
    });
});
