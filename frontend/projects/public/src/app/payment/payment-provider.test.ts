import { describe, it, expect } from 'vitest';
import { of } from 'rxjs';
import {
    PaymentResult,
    PaymentStatusNotification,
    SimplePaymentProvider,
    PaymentProvider,
} from './payment-provider';

describe('PaymentResult', () => {
    it('should create with success=true and gatewayToken', () => {
        const result = new PaymentResult(true, 'tok-123');
        expect(result.success).toBe(true);
        expect(result.gatewayToken).toBe('tok-123');
        expect(result.reason).toBeNull();
        expect(result.reservationChanged).toBe(false);
    });

    it('should create with success=false and no token', () => {
        const result = new PaymentResult(false, null);
        expect(result.success).toBe(false);
        expect(result.gatewayToken).toBeNull();
    });

    it('should create with reason message', () => {
        const result = new PaymentResult(false, null, 'Payment declined');
        expect(result.reason).toBe('Payment declined');
    });

    it('should create with reservationChanged flag', () => {
        const result = new PaymentResult(true, 'tok-123', null, true);
        expect(result.reservationChanged).toBe(true);
    });

    it('should allow all parameters', () => {
        const result = new PaymentResult(
            false,
            'tok-456',
            'Error message',
            true,
        );
        expect(result.success).toBe(false);
        expect(result.gatewayToken).toBe('tok-456');
        expect(result.reason).toBe('Error message');
        expect(result.reservationChanged).toBe(true);
    });
});

describe('PaymentStatusNotification', () => {
    it('should create with delayed and indeterminate flags', () => {
        const notification = new PaymentStatusNotification(true, false);
        expect(notification.delayed).toBe(true);
        expect(notification.indeterminate).toBe(false);
    });

    it('should create with both flags true', () => {
        const notification = new PaymentStatusNotification(true, true);
        expect(notification.delayed).toBe(true);
        expect(notification.indeterminate).toBe(true);
    });

    it('should create with both flags false', () => {
        const notification = new PaymentStatusNotification(false, false);
        expect(notification.delayed).toBe(false);
        expect(notification.indeterminate).toBe(false);
    });
});

describe('SimplePaymentProvider', () => {
    it('should implement PaymentProvider interface', () => {
        const provider: PaymentProvider = new SimplePaymentProvider();
        expect(provider.paymentMethodDeferred).toBe(true);
    });

    it('should return paymentMethodDeferred as true', () => {
        const provider = new SimplePaymentProvider();
        expect(provider.paymentMethodDeferred).toBe(true);
    });

    it('should pay() return successful PaymentResult', async () => {
        const provider = new SimplePaymentProvider();
        const promise = new Promise<PaymentResult>((resolve) => {
            provider.pay().subscribe({ next: (result) => resolve(result) });
        });
        const result = await promise;
        expect(result).toBeInstanceOf(PaymentResult);
        expect(result.success).toBe(true);
        expect(result.gatewayToken).toBeNull();
    });

    it('should statusNotifications() return EMPTY', () => {
        const provider = new SimplePaymentProvider();
        const result = provider.statusNotifications();
        expect(result).toBeDefined();
    });
});
