import { describe, it, expect, beforeEach } from 'vitest';
import { FeedbackService } from './feedback/feedback.service';
import type { FeedbackContent } from '../model/feedback';

describe('FeedbackService', () => {
    let service: FeedbackService;

    beforeEach(() => {
        service = new FeedbackService();
    });

    describe('showSuccess', () => {
        it('should emit SUCCESS feedback', async () => {
            const promise = new Promise<void>((resolve) => {
                service
                    .displayNotification()
                    .subscribe((notification: FeedbackContent) => {
                        expect(notification.active).toBe(true);
                        expect(notification.message).toBe('success.message');
                        expect(notification.type).toBe('SUCCESS');
                        resolve();
                    });
            });

            service.showSuccess('success.message');
            await promise;
        });
    });

    describe('showError', () => {
        it('should emit ERROR feedback', async () => {
            const promise = new Promise<void>((resolve) => {
                service
                    .displayNotification()
                    .subscribe((notification: FeedbackContent) => {
                        expect(notification.active).toBe(true);
                        expect(notification.message).toBe('error.message');
                        expect(notification.type).toBe('ERROR');
                        resolve();
                    });
            });

            service.showError('error.message');
            await promise;
        });
    });

    describe('showInfo', () => {
        it('should emit INFO feedback', async () => {
            const promise = new Promise<void>((resolve) => {
                service
                    .displayNotification()
                    .subscribe((notification: FeedbackContent) => {
                        expect(notification.active).toBe(true);
                        expect(notification.message).toBe('info.message');
                        expect(notification.type).toBe('INFO');
                        resolve();
                    });
            });

            service.showInfo('info.message');
            await promise;
        });
    });

    describe('hide', () => {
        it('should emit inactive feedback', async () => {
            const promise = new Promise<void>((resolve) => {
                service
                    .displayNotification()
                    .subscribe((notification: FeedbackContent) => {
                        expect(notification.active).toBe(false);
                        resolve();
                    });
            });

            service.hide();
            await promise;
        });
    });

    describe('displayNotification', () => {
        it('should return an observable', () => {
            const observable = service.displayNotification();
            expect(observable).toBeDefined();
        });

        it('should emit multiple notifications in sequence', async () => {
            const emissions: FeedbackContent[] = [];

            const promise = new Promise<void>((resolve) => {
                service
                    .displayNotification()
                    .subscribe((notification: FeedbackContent) => {
                        emissions.push(notification);
                        if (emissions.length === 3) {
                            expect(emissions[0].type).toBe('SUCCESS');
                            expect(emissions[1].type).toBe('ERROR');
                            expect(emissions[2].type).toBe('INFO');
                            resolve();
                        }
                    });
            });

            service.showSuccess('msg1');
            service.showError('msg2');
            service.showInfo('msg3');
            await promise;
        });
    });
});
