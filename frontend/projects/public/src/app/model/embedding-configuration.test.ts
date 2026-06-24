import { describe, it, expect } from 'vitest';
import {
    ReservationStatusChanged,
    EmbeddingConfiguration,
} from './embedding-configuration';

describe('embedding-configuration.ts', () => {
    describe('ReservationStatusChanged', () => {
        it('should create instance with status and id', () => {
            const statusChanged = new ReservationStatusChanged(
                'COMPLETED',
                'res-123',
            );
            expect(statusChanged.status).toBe('COMPLETED');
            expect(statusChanged.id).toBe('res-123');
            expect(statusChanged.error).toBeUndefined();
        });

        it('should create instance with status, id and error', () => {
            const statusChanged = new ReservationStatusChanged(
                'FAILED',
                'res-456',
                'Payment failed',
            );
            expect(statusChanged.status).toBe('FAILED');
            expect(statusChanged.id).toBe('res-456');
            expect(statusChanged.error).toBe('Payment failed');
        });

        it('should accept different reservation statuses', () => {
            const statuses: ReservationStatus[] = [
                'PENDING',
                'COMPLETED',
                'FAILED',
                'CANCELLED',
                'EXPIRED',
            ];

            statuses.forEach((status) => {
                const statusChanged = new ReservationStatusChanged(
                    status,
                    'test-id',
                );
                expect(statusChanged.status).toBe(status);
            });
        });

        it('should allow undefined error', () => {
            const statusChanged = new ReservationStatusChanged(
                'COMPLETED',
                'res-789',
            );
            expect(statusChanged.error).toBeUndefined();
        });

        it('should allow empty string error', () => {
            const statusChanged = new ReservationStatusChanged(
                'COMPLETED',
                'res-789',
                '',
            );
            expect(statusChanged.error).toBe('');
        });
    });

    describe('EmbeddingConfiguration', () => {
        it('should define enabled and notificationOrigin properties', () => {
            const config: EmbeddingConfiguration = {
                enabled: true,
                notificationOrigin: 'https://example.com',
            };

            expect(config.enabled).toBe(true);
            expect(config.notificationOrigin).toBe('https://example.com');
        });

        it('should allow enabled to be false', () => {
            const config: EmbeddingConfiguration = {
                enabled: false,
                notificationOrigin: 'https://example.com',
            };

            expect(config.enabled).toBe(false);
        });

        it('should allow empty notificationOrigin', () => {
            const config: EmbeddingConfiguration = {
                enabled: true,
                notificationOrigin: '',
            };

            expect(config.notificationOrigin).toBe('');
        });
    });
});
