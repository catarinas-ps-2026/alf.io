import { describe, it, expect, afterEach, vi } from 'vitest';
import { AdditionalFieldService } from './additional-field.ts';
import { createPurchaseContext } from '../test-utils/factories.ts';

vi.mock('./helpers.ts', () => ({
    fetchJson: vi.fn(),
    postJson: vi.fn(),
    callDelete: vi.fn(),
}));

import { fetchJson, postJson, callDelete } from './helpers.ts';

const mockFetchJson = vi.mocked(fetchJson);
const mockPostJson = vi.mocked(postJson);
const mockCallDelete = vi.mocked(callDelete);

describe('AdditionalFieldService', () => {
    // Técnicas: Mocking con verificación (mockear helpers.ts)
    // Cada método construye una URL y delega a helpers HTTP
    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('loadAllByPurchaseContext', () => {
        // Técnicas: Particiones de equivalencia (type: event vs subscription)
        it.each([
            ['event', 'evt-1', '/admin/api/event/evt-1/additional-field'],
            [
                'subscription',
                'sub-1',
                '/admin/api/subscription/sub-1/additional-field',
            ],
        ])('construye URL para type=%s', async (_type, identifier, expectedUrl) => {
            const ctx = createPurchaseContext({
                type: _type as 'event' | 'subscription',
                publicIdentifier: identifier,
            });
            mockFetchJson.mockResolvedValue([]);

            await AdditionalFieldService.loadAllByPurchaseContext(ctx);

            expect(mockFetchJson).toHaveBeenCalledWith(expectedUrl);
        });
    });

    describe('deleteField', () => {
        // Técnicas: Mocking con verificación
        it('construye URL DELETE con id', async () => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            mockCallDelete.mockResolvedValue({} as Response);

            await AdditionalFieldService.deleteField(ctx, 5);

            expect(mockCallDelete).toHaveBeenCalledWith(
                '/admin/api/event/evt-1/additional-field/5',
            );
        });
    });

    describe('swapFieldPosition', () => {
        // Técnicas: Mocking con verificación + Valores límite
        it.each([
            [
                'ids diferentes',
                1,
                2,
                '/admin/api/event/evt-1/additional-field/swap-position/1/2',
            ],
            [
                'ids iguales',
                1,
                1,
                '/admin/api/event/evt-1/additional-field/swap-position/1/1',
            ],
        ])('construye URL con %s', async (_label, id1, id2, expectedUrl) => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            mockPostJson.mockResolvedValue({} as Response);

            await AdditionalFieldService.swapFieldPosition(ctx, id1, id2);

            expect(mockPostJson).toHaveBeenCalledWith(expectedUrl, null);
        });
    });

    describe('moveField', () => {
        // Técnicas: Mocking con verificación + Valores límite
        it.each([
            ['position válida', 5],
            ['position cero', 0],
        ])('envía %s (%d) en body URLSearchParams', async (_label, position) => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            mockPostJson.mockResolvedValue({} as Response);

            await AdditionalFieldService.moveField(ctx, 1, position);

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/evt-1/additional-field/set-position/1',
                expect.any(URLSearchParams),
            );
            const body = mockPostJson.mock.calls[0][1] as URLSearchParams;
            expect(body.get('newPosition')).toBe(String(position));
        });
    });

    describe('createNewField', () => {
        // Técnicas: Mocking con verificación
        it('envía POST y retorna ValidatedResponse', async () => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            const mockResponse = { success: true, value: { id: 1 } };
            mockPostJson.mockResolvedValue({
                json: vi.fn().mockResolvedValue(mockResponse),
            } as unknown as Response);

            const result = await AdditionalFieldService.createNewField(ctx, {
                name: 'test',
                type: 'input:text',
                order: 0,
                required: true,
                readOnly: false,
                categoryIds: [],
                displayAtCheckIn: false,
                description: {},
                userDefinedOrder: false,
            });

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/evt-1/additional-field/new',
                expect.any(Object),
            );
            expect(result).toEqual(mockResponse);
        });
    });

    describe('saveField', () => {
        // Técnicas: Mocking con verificación
        it('usa field.id en URL', async () => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            mockPostJson.mockResolvedValue({} as Response);

            await AdditionalFieldService.saveField(ctx, {
                id: 5,
                name: 'test',
                type: 'input:text',
                order: 0,
                required: true,
                editable: true,
                context: 'ATTENDEE',
                displayAtCheckIn: false,
                description: {},
            });

            expect(mockPostJson).toHaveBeenCalledWith(
                '/admin/api/event/evt-1/additional-field/5',
                expect.any(Object),
            );
        });
    });

    describe('loadRestrictedValuesStats', () => {
        // Técnicas: Mocking con verificación
        it('construye URL con id de stats', async () => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            mockFetchJson.mockResolvedValue([]);

            await AdditionalFieldService.loadRestrictedValuesStats(ctx, 3);

            expect(mockFetchJson).toHaveBeenCalledWith(
                '/admin/api/event/evt-1/additional-field/3/stats',
            );
        });
    });

    describe('loadTemplates', () => {
        // Técnicas: Mocking con verificación
        it('construye URL de templates', async () => {
            const ctx = createPurchaseContext({
                type: 'event',
                publicIdentifier: 'evt-1',
            });
            mockFetchJson.mockResolvedValue([]);

            await AdditionalFieldService.loadTemplates(ctx);

            expect(mockFetchJson).toHaveBeenCalledWith(
                '/admin/api/event/evt-1/additional-field/templates',
            );
        });
    });
});
