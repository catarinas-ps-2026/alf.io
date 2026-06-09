import { describe, it, expect, afterEach, vi } from 'vitest';
import { EventService } from './event.ts';
import { SubscriptionDescriptorService } from './subscription-descriptor.ts';
import { ConfigurationService } from './configuration.ts';
import { UtilService } from './util.ts';
import { PurchaseContextService } from './purchase-context.ts';

vi.mock('./helpers.ts', () => ({
    fetchJson: vi.fn(),
    postJson: vi.fn(),
    putJson: vi.fn(),
    callDelete: vi.fn(),
}));

import { fetchJson, postJson, putJson, callDelete } from './helpers.ts';

const mockFetchJson = vi.mocked(fetchJson);
const mockPostJson = vi.mocked(postJson);
const mockPutJson = vi.mocked(putJson);
const mockCallDelete = vi.mocked(callDelete);

describe('EventService', () => {
    // Técnicas: Mocking con verificación
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('load', () => {
        it('construye URL con publicIdentifier', async () => {
            mockFetchJson.mockResolvedValue({ event: {}, organization: {} });

            await EventService.load('evt-1');

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events/evt-1');
        });
    });
});

describe('SubscriptionDescriptorService', () => {
    // Técnicas: Mocking con verificación
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('load', () => {
        it('construye URL con organizationId y publicIdentifier', async () => {
            mockFetchJson.mockResolvedValue({});

            await SubscriptionDescriptorService.load('sub-1', 1);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/organization/1/subscription/sub-1');
        });
    });
});

describe('ConfigurationService', () => {
    // Técnicas: Mocking con verificación
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('update', () => {
        it('envía POST con key/value', async () => {
            mockPostJson.mockResolvedValue({} as Response);
            const kv = { key: 'theme', value: 'dark' };

            await ConfigurationService.update(kv);

            expect(mockPostJson).toHaveBeenCalledWith('/admin/api/configuration/update', kv);
        });
    });
});

describe('UtilService', () => {
    // Técnicas: Mocking con verificación
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('renderMarkdown', () => {
        it('construye URL con text encoded', async () => {
            mockFetchJson.mockResolvedValue('<p>hello</p>');

            await UtilService.renderMarkdown('hello **world**');

            expect(mockFetchJson).toHaveBeenCalledWith(
                '/admin/api/utils/render-commonmark?text=hello%20**world**',
            );
        });
    });
});

describe('PurchaseContextService', () => {
    // Técnicas: Particiones de equivalencia + Mocking
    // - PE1: type='event' → delega a EventService
    // - PE2: type='subscription' → delega a SubscriptionDescriptorService
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('load', () => {
        it('delega a EventService para type=event', async () => {
            const mockEvent = { event: { id: 1 }, organization: { id: 1 } };
            mockFetchJson.mockResolvedValue(mockEvent);

            const result = await PurchaseContextService.load('evt-1', 'event', 1);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events/evt-1');
            expect(result).toEqual({ eventWithOrganization: mockEvent });
        });

        it('delega a SubscriptionDescriptorService para type=subscription', async () => {
            const mockSub = { id: 1, type: 'subscription' };
            mockFetchJson.mockResolvedValue(mockSub);

            const result = await PurchaseContextService.load('sub-1', 'subscription', 1);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/organization/1/subscription/sub-1');
            expect(result).toEqual({ subscriptionDescriptor: mockSub });
        });

        it('ignora organizationId para type=event', async () => {
            mockFetchJson.mockResolvedValue({});

            await PurchaseContextService.load('evt-1', 'event', 999);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events/evt-1');
        });
    });
});

describe('LocalizationService', () => {
    // Técnicas: Mocking con verificación
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('llama a endpoint de idiomas', async () => {
        const { LocalizationService } = await import('./localization.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new LocalizationService();
        await service.getEventsSupportedLanguages();

        expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/events-supported-languages');
    });
});

describe('CustomPaymentMethodsService', () => {
    // Técnicas: Mocking con verificación
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('construye URL con organizationId', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new CustomPaymentMethodsService();
        await service.getPaymentMethodsForOrganization(1);

        expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/configuration/organizations/1/payment-method');
    });

    it('envía POST con paymentMethod', async () => {
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

    it('envía POST con array de ids', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockPostJson.mockResolvedValue({} as Response);

        const service = new CustomPaymentMethodsService();
        await service.setPaymentMethodsForEvent(10, ['pm-1', 'pm-2']);

        expect(mockPostJson).toHaveBeenCalledWith(
            '/admin/api/configuration/event/10/payment-method',
            ['pm-1', 'pm-2'],
        );
    });

    it('construye URL con ids de evento y categoría', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new CustomPaymentMethodsService();
        await service.getDeniedPaymentMethodsForCategory(10, 1);

        expect(mockFetchJson).toHaveBeenCalledWith(
            '/admin/api/events/10/categories/1/denied-custom-payment-methods',
        );
    });

    it('envía PUT para actualizar método de pago', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockPutJson.mockResolvedValue({} as Response);

        const service = new CustomPaymentMethodsService();
        const method = { id: 'pm-1', name: 'Updated' } as any;
        await service.updatePaymentMethod(1, 'pm-1', method);

        expect(mockPutJson).toHaveBeenCalledWith(
            '/admin/api/configuration/organizations/1/payment-method/pm-1',
            method,
        );
    });

    it('envía DELETE para eliminar método de pago', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockCallDelete.mockResolvedValue({} as Response);

        const service = new CustomPaymentMethodsService();
        await service.deletePaymentMethod(1, 'pm-1');

        expect(mockCallDelete).toHaveBeenCalledWith(
            '/admin/api/configuration/organizations/1/payment-method/pm-1',
        );
    });

    it('construye URL de métodos permitidos para evento', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockFetchJson.mockResolvedValue([]);

        const service = new CustomPaymentMethodsService();
        await service.getAllowedPaymentMethodsForEvent(10);

        expect(mockFetchJson).toHaveBeenCalledWith(
            '/admin/api/configuration/event/10/payment-method',
        );
    });

    it('envía POST para establecer métodos denegados en categoría', async () => {
        const { CustomPaymentMethodsService } = await import('./custom-payment-methods.ts');
        mockPostJson.mockResolvedValue({} as Response);

        const service = new CustomPaymentMethodsService();
        await service.setDeniedPaymentMethodsForCategory(10, 1, ['pm-1']);

        expect(mockPostJson).toHaveBeenCalledWith(
            '/admin/api/events/10/categories/1/denied-custom-payment-methods',
            ['pm-1'],
        );
    });
});
