import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SubscriptionService, getLocalizedContent } from './subscription.service';

describe('SubscriptionService', () => {
    let service: SubscriptionService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [SubscriptionService],
        });

        service = TestBed.inject(SubscriptionService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    describe('getSubscriptions', () => {
        it('should GET subscriptions without params', () => {
            const mockResponse = [{ id: 'sub-1' }, { id: 'sub-2' }];

            service.getSubscriptions().subscribe((result) => {
                expect(result).toEqual(mockResponse);
            });

            const req = httpMock.expectOne('/api/v2/public/subscriptions');
            expect(req.request.method).toBe('GET');
            req.flush(mockResponse);
        });

        it('should GET subscriptions with search params', () => {
            const mockResponse = [{ id: 'sub-1' }];
            const mockSearchParams = {
                toHttpParams: () => ({ subscriptionId: 'sub-1' }),
            } as any;

            service.getSubscriptions(mockSearchParams).subscribe((result) => {
                expect(result).toEqual(mockResponse);
            });

            const req = httpMock.expectOne((u) => u.url.includes('/api/v2/public/subscriptions'));
            expect(req.request.method).toBe('GET');
            req.flush(mockResponse);
        });
    });

    describe('getSubscriptionById', () => {
        it('should GET subscription by id', () => {
            const mockResponse = { id: 'sub-123', title: { en: 'Test' } };

            service.getSubscriptionById('sub-123').subscribe((result) => {
                expect(result).toEqual(mockResponse);
            });

            const req = httpMock.expectOne('/api/v2/public/subscription/sub-123');
            expect(req.request.method).toBe('GET');
            req.flush(mockResponse);
        });

        it('should cache subscription by id', () => {
            const mockResponse = { id: 'sub-123', title: { en: 'Test' } };

            service.getSubscriptionById('sub-123').subscribe();
            service.getSubscriptionById('sub-123').subscribe();

            const req = httpMock.expectOne('/api/v2/public/subscription/sub-123');
            expect(req.request.method).toBe('GET');
            req.flush(mockResponse);
        });
    });

    describe('reserve', () => {
        it('should POST to reserve subscription', () => {
            const mockResponse = { success: true, value: 'reserved-id' };

            service.reserve('sub-123').subscribe((result) => {
                expect(result).toEqual(mockResponse);
            });

            const req = httpMock.expectOne('/api/v2/public/subscription/sub-123');
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual({});
            req.flush(mockResponse);
        });
    });

    describe('getLocalizedContent', () => {
        it('should return content for current language', () => {
            const container = { en: 'English Content', it: 'Italian Content' };
            expect(getLocalizedContent(container, 'en')).toBe('English Content');
        });

        it('should return content for different language', () => {
            const container = { en: 'English Content', it: 'Italian Content' };
            expect(getLocalizedContent(container, 'it')).toBe('Italian Content');
        });

        it('should fallback to first language when current not found', () => {
            const container = { en: 'English Content', fr: 'French Content' };
            expect(getLocalizedContent(container, 'it')).toBe('English Content');
        });

        it('should return undefined when container is empty', () => {
            const container = {} as any;
            expect(getLocalizedContent(container, 'en')).toBeUndefined();
        });
    });
});