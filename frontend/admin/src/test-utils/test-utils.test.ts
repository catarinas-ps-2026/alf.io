import { afterEach, describe, expect, it } from 'vitest';
import {
    createAdditionalField,
    createAdditionalItem,
    createAlfioEvent,
    createContentLanguage,
    createDateTimeModification,
    createEventWithOrganization,
    createOrganization,
    createPurchaseContext,
    createValidatedResponse,
} from './factories.ts';
import {
    clearCsrfMeta,
    getFetchMock,
    mockCsrfMeta,
    mockFetchJson,
    resetFetchMock,
} from './mocks.ts';

describe('test-utils/factories', () => {
    // Técnicas: Contract testing + Valores límite
    // - Verificar que cada factory retorna un objeto con la estructura correcta
    // - Verificar que overrides funcionan correctamente
    it('createPurchaseContext returns valid default object', () => {
        const ctx = createPurchaseContext();
        expect(ctx.type).toBe('event');
        expect(ctx.publicIdentifier).toBe('test-event-123');
        expect(ctx.contentLanguages).toHaveLength(1);
    });

    it('createAdditionalField returns valid default object', () => {
        const field = createAdditionalField();
        expect(field.name).toBe('attendee-name');
        expect(field.type).toBe('input:text');
        expect(field.required).toBe(true);
    });

    it('createAdditionalItem returns valid default object', () => {
        const item = createAdditionalItem();
        expect(item.id).toBe(1);
        expect(item.price).toBe(1000);
        expect(item.type).toBe('SUPPLEMENT');
    });

    it('createAlfioEvent returns valid default object', () => {
        const event = createAlfioEvent();
        expect(event.id).toBe(1);
        expect(event.displayName).toBe('Test Event');
        expect(event.currency).toBe('EUR');
    });

    it('createOrganization returns valid default object', () => {
        const org = createOrganization();
        expect(org.id).toBe(1);
        expect(org.name).toBe('Test Org');
    });

    it('createEventWithOrganization returns valid default object', () => {
        const ewo = createEventWithOrganization();
        expect(ewo.event).toBeDefined();
        expect(ewo.organization).toBeDefined();
    });

    it('createValidatedResponse returns valid default object', () => {
        const res = createValidatedResponse({ name: 'test' });
        expect(res.success).toBe(true);
        expect(res.value.name).toBe('test');
        expect(res.validationErrors).toHaveLength(0);
    });

    it('createContentLanguage returns valid default object', () => {
        const lang = createContentLanguage();
        expect(lang.locale).toBe('en');
        expect(lang.language).toBe('English');
    });

    it('createDateTimeModification returns valid default object', () => {
        const dt = createDateTimeModification();
        expect(dt.date).toBe('2025-01-15');
        expect(dt.time).toBe('10:00');
    });

    it('factories accept overrides', () => {
        const ctx = createPurchaseContext({
            type: 'subscription',
            publicIdentifier: 'sub-1',
        });
        expect(ctx.type).toBe('subscription');
        expect(ctx.publicIdentifier).toBe('sub-1');

        const field = createAdditionalField({
            type: 'checkbox',
            required: false,
        });
        expect(field.type).toBe('checkbox');
        expect(field.required).toBe(false);
    });
});

describe('test-utils/mocks', () => {
    // Técnicas: Mocking con verificación
    // - Verificar que los helpers de mocking crean mocks correctos
    // - Verificar que limpieza funciona correctamente
    afterEach(() => {
        resetFetchMock();
    });

    it('mockFetchJson mocks global.fetch and returns data', async () => {
        const data = { id: 1, name: 'test' };
        mockFetchJson(data);

        const result = await fetch('/api/test');
        const json = await (result as Response).json();
        expect(json).toEqual(data);
    });

    it('mockCsrfMeta adds meta tags to document head', () => {
        mockCsrfMeta('X-Custom-Token', 'custom-value-123');

        const header = document.querySelector('meta[name="_csrf_header"]');
        const token = document.querySelector('meta[name="_csrf"]');
        expect(header?.getAttribute('content')).toBe('X-Custom-Token');
        expect(token?.getAttribute('content')).toBe('custom-value-123');

        clearCsrfMeta();
        expect(document.querySelector('meta[name="_csrf_header"]')).toBeNull();
    });

    it('getFetchMock returns the mocked fetch', () => {
        mockFetchJson({});
        const mock = getFetchMock();
        expect(mock).toBeDefined();
        expect(typeof mock).toBe('function');
    });
});
