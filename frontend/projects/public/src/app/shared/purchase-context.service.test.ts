import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PurchaseContextService, PurchaseContextType } from './purchase-context.service';
import { EventService } from './event.service';
import { SubscriptionService } from './subscription.service';
import type { PurchaseContext } from '../model/purchase-context';
import type { Event } from '../model/event';
import type { SubscriptionInfo } from '../model/subscription';

describe('PurchaseContextService', () => {
    let service: PurchaseContextService;
    let mockEventService: any;
    let mockSubscriptionService: any;

    const mockEvent: Event = {
        id: 1,
        shortName: 'test-event',
        title: { en: 'Test Event' },
        description: {},
        format: 'IN_PERSON',
        startDate: '2024-01-01T10:00:00Z',
        endDate: '2024-01-01T18:00:00Z',
        enabled: true,
        embeddingConfiguration: { enabled: false, notificationOrigin: '' },
        invoicingConfiguration: { enabled: false, onlyInvoice: false, vatIncluded: false, userCanDownloadReceiptOrInvoice: true, enabledItalyEInvoicing: false },
        localization: {},
        analyticsConfiguration: null,
    } as unknown as Event;

    const mockSubscription: SubscriptionInfo = {
        id: 'sub-123',
        title: { en: 'Test Subscription' },
        description: {},
        shortDescription: {},
        imageUrl: null,
        format: 'BADGE',
        startDate: '2024-01-01',
        endDate: '2024-12-31',
        enabled: true,
        analyticsConfiguration: null,
        embeddingConfiguration: { enabled: false, notificationOrigin: '' },
        additionalCategories: [],
        ticketCategories: [],
        localization: {},
        privacyPolicyUrl: null,
        invoicingConfiguration: { enabled: false, onlyInvoice: false, vatIncluded: false, userCanDownloadReceiptOrInvoice: true, enabledItalyEInvoicing: false },
        assignmentConfiguration: { enableAttendeeAutocomplete: true },
        contentLanguages: [{ locale: 'en', name: 'English' }],
        currency: 'USD',
    } as unknown as SubscriptionInfo;

    beforeEach(() => {
        mockEventService = {
            getEvent: vi.fn(),
        };

        mockSubscriptionService = {
            getSubscriptionById: vi.fn(),
        };

        TestBed.configureTestingModule({
            providers: [
                PurchaseContextService,
                { provide: EventService, useValue: mockEventService },
                { provide: SubscriptionService, useValue: mockSubscriptionService },
            ],
        });

        service = TestBed.inject(PurchaseContextService);
    });

    describe('getContext', () => {
        it('should return event when type is event', async () => {
            mockEventService.getEvent.mockReturnValue(of(mockEvent));

            const promise = new Promise<PurchaseContext>((resolve) => {
                service.getContext('event', 'test-event').subscribe({ next: (result) => resolve(result) });
            });

            const result = await promise;
            expect(result).toEqual(mockEvent);
            expect(mockEventService.getEvent).toHaveBeenCalledWith('test-event');
        });

        it('should return subscription when type is subscription', async () => {
            mockSubscriptionService.getSubscriptionById.mockReturnValue(of(mockSubscription));

            const promise = new Promise<PurchaseContext>((resolve) => {
                service.getContext('subscription', 'sub-123').subscribe({ next: (result) => resolve(result) });
            });

            const result = await promise;
            expect(result).toEqual(mockSubscription);
            expect(mockSubscriptionService.getSubscriptionById).toHaveBeenCalledWith('sub-123');
        });

        it('should throw error when type is unknown', () => {
            expect(() => service.getContext('unknown' as PurchaseContextType, 'id')).toThrow();
        });
    });
});