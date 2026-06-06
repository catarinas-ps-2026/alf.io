import { describe, it, expect, afterEach, vi } from 'vitest';
import { EventService } from './event.ts';
import { SubscriptionDescriptorService } from './subscription-descriptor.ts';
import { ConfigurationService } from './configuration.ts';
import { UtilService } from './util.ts';
import { PurchaseContextService } from './purchase-context.ts';

vi.mock('./helpers.ts', () => ({
    fetchJson: vi.fn(),
    postJson: vi.fn(),
}));

import { fetchJson, postJson } from './helpers.ts';

const mockFetchJson = vi.mocked(fetchJson);
const mockPostJson = vi.mocked(postJson);

describe('EventService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('load', () => {
        it('CP-EVT-01: construye URL con publicIdentifier', async () => {
            mockFetchJson.mockResolvedValue({ event: {}, organization: {} });

            await EventService.load('evt-1');

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events/evt-1');
        });
    });
});

describe('SubscriptionDescriptorService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('load', () => {
        it('CP-SUB-01: construye URL con organizationId y publicIdentifier', async () => {
            mockFetchJson.mockResolvedValue({});

            await SubscriptionDescriptorService.load('sub-1', 1);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/organization/1/subscription/sub-1');
        });
    });
});

describe('ConfigurationService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('update', () => {
        it('CP-CFG-01: envía POST con key/value', async () => {
            mockPostJson.mockResolvedValue({} as Response);
            const kv = { key: 'theme', value: 'dark' };

            await ConfigurationService.update(kv);

            expect(mockPostJson).toHaveBeenCalledWith('/admin/api/configuration/update', kv);
        });
    });
});

describe('UtilService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('renderMarkdown', () => {
        it('CP-UTL-01: construye URL con text encoded', async () => {
            mockFetchJson.mockResolvedValue('<p>hello</p>');

            await UtilService.renderMarkdown('hello **world**');

            expect(mockFetchJson).toHaveBeenCalledWith(
                '/admin/api/utils/render-commonmark?text=hello%20**world**',
            );
        });
    });
});

describe('PurchaseContextService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('load', () => {
        it('CP-CTX-01: delega a EventService para type=event', async () => {
            const mockEvent = { event: { id: 1 }, organization: { id: 1 } };
            mockFetchJson.mockResolvedValue(mockEvent);

            const result = await PurchaseContextService.load('evt-1', 'event', 1);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events/evt-1');
            expect(result).toEqual({ eventWithOrganization: mockEvent });
        });

        it('CP-CTX-02: delega a SubscriptionDescriptorService para type=subscription', async () => {
            const mockSub = { id: 1, type: 'subscription' };
            mockFetchJson.mockResolvedValue(mockSub);

            const result = await PurchaseContextService.load('sub-1', 'subscription', 1);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/organization/1/subscription/sub-1');
            expect(result).toEqual({ subscriptionDescriptor: mockSub });
        });

        it('CP-CTX-03: ignora organizationId para type=event', async () => {
            mockFetchJson.mockResolvedValue({});

            await PurchaseContextService.load('evt-1', 'event', 999);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events/evt-1');
        });
    });
});

describe('LocalizationService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('CP-LOC-01: llama a endpoint de idiomas', async () => {
        const { LocalizationService } = await import('./localization.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new LocalizationService();
        await service.getEventsSupportedLanguages();

        expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events-supported-languages');
    });
});

describe('CustomPaymentMethodsService', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('CP-CPM-01: construye URL con organizationId', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new CustomPaymentMethodsService();
        await service.getPaymentMethodsForOrganization(1);

        expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/configuration/organizations/1/payment-method');
    });

    it('CP-CPM-02: envía POST con paymentMethod', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockPostJson.mockResolvedValue({} as Response);

        const service = new CustomPaymentMethodsService();
        const method = { id: 'pm-1', name: 'Bank Transfer' } as any;
        await service.createPaymentMethod(1, method);

        expect(mockPostJson).toHaveBeenCalledWith(
            '/admin/api/configuration/organizations/1/payment-method',
            method,
        );
    });

    it('CP-CPM-05: envía POST con array de ids', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockPostJson.mockResolvedValue({} as Response);

        const service = new CustomPaymentMethodsService();
        await service.setPaymentMethodsForEvent(10, ['pm-1', 'pm-2']);

        expect(mockPostJson).toHaveBeenCalledWith(
            '/admin/api/configuration/event/10/payment-method',
            ['pm-1', 'pm-2'],
        );
    });

    it('CP-CPM-08: construye URL con ids de evento y categoría', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new CustomPaymentMethodsService();
        await service.getDeniedPaymentMethodsForCategory(10, 1);

        expect(mockFetchJson).toHaveBeenCalledWith(
            '/admin/api/events/10/categories/1/denied-custom-payment-methods',
        );
    });
});
