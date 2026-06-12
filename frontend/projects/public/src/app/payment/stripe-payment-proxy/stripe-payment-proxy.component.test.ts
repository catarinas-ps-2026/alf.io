import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { StripePaymentProxyComponent } from './stripe-payment-proxy.component';
import { ReservationService } from '../../shared/reservation.service';
import { mockReservationInfo, mockPurchaseContext } from '../test-data';

describe('StripePaymentProxyComponent', () => {
    let component: StripePaymentProxyComponent;
    let fixture: ComponentFixture<StripePaymentProxyComponent>;

    const mockTranslateService = {
        currentLang: 'en',
        instant: vi.fn(() => ''),
    };

    const mockReservationService = {
        initPayment: vi.fn(() => of({ success: true, clientSecret: 'secret123', reservationStatusChanged: false })),
        getPaymentStatus: vi.fn(() => of({ success: true })),
    };

    const createComponent = (method: string = 'CREDIT_CARD', proxy: string = 'STRIPE', parameters: { [key: string]: any } = {}) => {
        component.method = method as any;
        component.proxy = proxy as any;
        component.parameters = parameters;
        component.purchaseContext = mockPurchaseContext;
        component.reservation = mockReservationInfo;
        fixture.detectChanges();
    };

    beforeEach(async () => {
        vi.clearAllMocks();

        await TestBed.configureTestingModule({
            declarations: [StripePaymentProxyComponent],
            providers: [
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: ReservationService, useValue: mockReservationService },
            ],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(StripePaymentProxyComponent);
        component = fixture.componentInstance;
    });

    describe('matchProxyAndMethod', () => {
        it('should return true when proxy is STRIPE', () => {
            component.proxy = 'STRIPE';
            component.matchProxyAndMethod;
            expect(component.matchProxyAndMethod).toBe(true);
        });

        it('should return false when proxy is not STRIPE', () => {
            component.proxy = 'PAYPAL';
            expect(component.matchProxyAndMethod).toBe(false);
        });
    });

    describe('useSCA getter', () => {
        it('should return true when enableSCA is set in parameters', () => {
            component.parameters = { enableSCA: true };
            expect(component.useSCA).toBe(true);
        });

        it('should return undefined when enableSCA is not set', () => {
            component.parameters = {};
            expect(component.useSCA).toBeUndefined();
        });

        it('should return undefined when parameters is empty object', () => {
            component.parameters = {};
            expect(component.useSCA).toBeUndefined();
        });
    });

    describe('ngOnChanges', () => {
        it('should emit paymentProvider when method changes and proxy matches (non-SCA)', () => {
            const emittedProviders: any[] = [];
            component.paymentProvider.subscribe((p) => emittedProviders.push(p));

            component.method = 'CREDIT_CARD' as any;
            component.proxy = 'STRIPE' as any;
            component.parameters = { stripe_p_key: 'pk_test_123' };
            component.purchaseContext = mockPurchaseContext;
            component.reservation = mockReservationInfo;

            component.ngOnChanges({ method: { currentValue: 'CREDIT_CARD' } } as any);

            expect(emittedProviders.length).toBe(1);
        });

        it('should not emit when proxy does not match', () => {
            let emitCount = 0;
            component.paymentProvider.subscribe(() => emitCount++);

            component.method = 'PAYPAL' as any;
            component.proxy = 'PAYPAL' as any;
            component.parameters = {};
            component.purchaseContext = mockPurchaseContext;
            component.reservation = mockReservationInfo;

            component.ngOnChanges({ method: { currentValue: 'PAYPAL' } } as any);

            expect(emitCount).toBe(0);
        });

        it('should not emit when method has not changed', () => {
            let emitCount = 0;
            component.paymentProvider.subscribe(() => emitCount++);

            component.ngOnChanges({ other: {} } as any);

            expect(emitCount).toBe(0);
        });

        it('should call unloadAll when proxy does not match', () => {
            const unloadSpy = vi.spyOn(component as any, 'unloadAll');

            component.method = 'CREDIT_CARD' as any;
            component.proxy = 'PAYPAL' as any;
            component.parameters = {};
            component.purchaseContext = mockPurchaseContext;
            component.reservation = mockReservationInfo;

            component.ngOnChanges({ method: { currentValue: 'CREDIT_CARD' } } as any);

            expect(unloadSpy).toHaveBeenCalled();
        });
    });

    describe('unloadAll', () => {
        it('should be callable without errors', () => {
            expect(() => component.ngOnDestroy()).not.toThrow();
        });
    });
});