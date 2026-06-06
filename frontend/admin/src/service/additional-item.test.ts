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
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('loadAll', () => {
        it('CP-AIL-01: construye URL con eventId', async () => {
            mockFetchJson.mockResolvedValue([]);

            await AdditionalItemService.loadAll({ eventId: 10 });

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/event/10/additional-services');
        });
    });

    describe('useCount', () => {
        it('CP-AIU-01: construye URL de conteo', async () => {
            mockFetchJson.mockResolvedValue({});

            await AdditionalItemService.useCount(10);

            expect(mockFetchJson).toHaveBeenCalledWith('/admin/api/event/10/additional-services/count');
        });
    });

    describe('validateAdditionalItem', () => {
        it('CP-AIV-01: envía POST a endpoint de validación', async () => {
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
        it('CP-AIU-01: PUT cuando id está presente', async () => {
            mockPutJson.mockResolvedValue({} as Response);
            const item = { id: 1, price: 1000 };

            await AdditionalItemService.updateAdditionalItem(item, 10);

            expect(mockPutJson).toHaveBeenCalledWith(
                '/admin/api/event/10/additional-services/1',
                item,
            );
            expect(mockPostJson).not.toHaveBeenCalled();
        });

        it('CP-AIU-02: POST cuando id es undefined', async () => {
            mockPostJson.mockResolvedValue({} as Response);
            const item = { price: 1000 };

            await AdditionalItemService.updateAdditionalItem(item, 10);

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/10/additional-services',
                item,
            );
            expect(mockPutJson).not.toHaveBeenCalled();
        });

        it('CP-AIU-03: PUT cuando id es 0', async () => {
            mockPutJson.mockResolvedValue({} as Response);
            const item = { id: 0, price: 1000 };

            await AdditionalItemService.updateAdditionalItem(item, 10);

            expect(mockPutJson).toHaveBeenCalledWith(
                '/admin/api/event/10/additional-services/0',
                item,
            );
        });
    });

    describe('deleteAdditionalItem', () => {
        it('CP-AID-01: construye URL DELETE con ids', async () => {
            mockCallDelete.mockResolvedValue({} as Response);

            await AdditionalItemService.deleteAdditionalItem(5, 10);

            expect(mockCallDelete).toHaveBeenCalledWith('/admin/api/event/10/additional-services/5');
        });
    });

    describe('swapItems', () => {
        it('CP-AIS-01: envía ids como body JSON', async () => {
            mockPostJson.mockResolvedValue({} as Response);

            await AdditionalItemService.swapItems(10, 1, 2);

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/10/additional-services/swap-position',
                { id1: 1, id2: 2 },
            );
        });
    });
});
