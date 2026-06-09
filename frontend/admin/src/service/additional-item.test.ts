import { describe, it, expect, afterEach, vi } from 'vitest';
import { AdditionalItemService } from './additional-item.ts';

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

describe('AdditionalItemService', () => {
    // Técnicas: Mocking con verificación (mockear helpers.ts)
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('loadAll', () => {
        // Técnicas: Mocking con verificación
        it('construye URL con eventId', async () => {
            mockFetchJson.mockResolvedValue([]);

            await AdditionalItemService.loadAll({ eventId: 10 });

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/event/10/additional-services');
        });
    });

    describe('useCount', () => {
        // Técnicas: Mocking con verificación
        it('construye URL de conteo', async () => {
            mockFetchJson.mockResolvedValue({});

            await AdditionalItemService.useCount(10);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/event/10/additional-services/count');
        });
    });

    describe('validateAdditionalItem', () => {
        // Técnicas: Mocking con verificación
        it('envía POST a endpoint de validación', async () => {
            const item = { price: 1000 };
            const mockResponse = { success: true, value: item };
            mockPostJson.mockResolvedValue({
                json: vi.fn().mockResolvedValue(mockResponse),
            } as unknown as Response);

            const result = await AdditionalItemService.validateAdditionalItem(item);

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/additional-services/validate',
                item,
            );
            expect(result).toEqual(mockResponse);
        });
    });

    describe('updateAdditionalItem', () => {
        // Técnicas: Particiones de equivalencia + Mocking
        // - PE1: id presente (incluye 0) → PUT
        // - PE2: id undefined/null → POST
        it.each([
            ['PUT cuando id está presente', { id: 1, price: 1000 }, '/admin/api/event/10/additional-services/1', 'putJson'],
            ['PUT cuando id es 0', { id: 0, price: 1000 }, '/admin/api/event/10/additional-services/0', 'putJson'],
        ])('%s', async (_label, item, expectedUrl, expectedMethod) => {
            mockPutJson.mockResolvedValue({} as Response);
            mockPostJson.mockResolvedValue({} as Response);

            await AdditionalItemService.updateAdditionalItem(item, 10);

            if (expectedMethod === 'putJson') {
                expect(mockPutJson).toHaveBeenCalledWith(expectedUrl, item);
                expect(mockPostJson).not.toHaveBeenCalled();
            } else {
                expect(mockPostJson).toHaveBeenCalledWith(expectedUrl, item);
                expect(mockPutJson).not.toHaveBeenCalled();
            }
        });

        it('POST cuando id es undefined', async () => {
            mockPostJson.mockResolvedValue({} as Response);
            const item = { price: 1000 };

            await AdditionalItemService.updateAdditionalItem(item, 10);

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/10/additional-services',
                item,
            );
            expect(mockPutJson).not.toHaveBeenCalled();
        });
    });

    describe('deleteAdditionalItem', () => {
        // Técnicas: Mocking con verificación
        it('construye URL DELETE con ids', async () => {
            mockCallDelete.mockResolvedValue({} as Response);

            await AdditionalItemService.deleteAdditionalItem(5, 10);

            expect(mockCallDelete).toHaveBeenCalledWith('/admin/api/event/10/additional-services/5');
        });
    });

    describe('swapItems', () => {
        // Técnicas: Mocking con verificación
        it('envía ids como body JSON', async () => {
            mockPostJson.mockResolvedValue({} as Response);

            await AdditionalItemService.swapItems(10, 1, 2);

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/10/additional-services/swap-position',
                { id1: 1, id2: 2 },
            );
        });
    });
});
