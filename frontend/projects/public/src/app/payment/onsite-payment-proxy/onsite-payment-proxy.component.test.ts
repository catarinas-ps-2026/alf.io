import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
    type ComponentFixture,
    TestBed,
    CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { UntypedFormGroup } from '@angular/forms';
import { OnsitePaymentProxyComponent } from './onsite-payment-proxy.component';

describe('OnsitePaymentProxyComponent', () => {
    let component: OnsitePaymentProxyComponent;
    let fixture: ComponentFixture<OnsitePaymentProxyComponent>;

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
            declarations: [OnsitePaymentProxyComponent],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(OnsitePaymentProxyComponent);
        component = fixture.componentInstance;
        component.overviewForm = mockFormGroup as unknown as UntypedFormGroup;
    });

    describe('matchProxyAndMethod', () => {
        it('should return true when method is ON_SITE and proxy is ON_SITE', () => {
            component.method = 'ON_SITE';
            component.proxy = 'ON_SITE';

            expect(component.matchProxyAndMethod).toBe(true);
        });

        it('should return false when method is not ON_SITE', () => {
            component.method = 'CREDIT_CARD';
            component.proxy = 'ON_SITE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when proxy is not ON_SITE', () => {
            component.method = 'ON_SITE';
            component.proxy = 'STRIPE';

            expect(component.matchProxyAndMethod).toBe(false);
        });

        it('should return false when both do not match', () => {
            component.method = 'PAYPAL';
            component.proxy = 'PAYPAL';

            expect(component.matchProxyAndMethod).toBe(false);
        });
    });

    describe('ngOnChanges', () => {
        it('should emit paymentProvider when matchProxyAndMethod and method changes', () => {
            const emittedProviders: any[] = [];
            component.paymentProvider.subscribe((p) =>
                emittedProviders.push(p),
            );

            component.method = 'ON_SITE';
            component.proxy = 'ON_SITE';

            component.ngOnChanges({
                method: { currentValue: 'ON_SITE' },
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

        it('should not emit when method has not changed', () => {
            let emitCount = 0;
            component.paymentProvider.subscribe(() => emitCount++);

            component.ngOnChanges({ other: {} } as any);

            expect(emitCount).toBe(0);
        });
    });

    describe('handleRecaptchaResponse', () => {
        it('should set captcha form value', () => {
            const captchaControl = {
                setValue: vi.fn(),
            };
            mockFormGroup.get.mockReturnValue(captchaControl);

            component.handleRecaptchaResponse('onsite-recaptcha-token');

            expect(mockFormGroup.get).toHaveBeenCalledWith('captcha');
            expect(captchaControl.setValue).toHaveBeenCalledWith(
                'onsite-recaptcha-token',
            );
        });
    });
});
