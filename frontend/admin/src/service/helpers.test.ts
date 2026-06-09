import { describe, it, expect, afterEach, vi, beforeEach } from 'vitest';
import {
    asString,
    asNumber,
    toDateTimeModification,
    extractDateTime,
    escapeHtml,
    supportedLanguages,
    notifyChange,
    postJson,
    putJson,
    callDelete,
    fetchJson,
} from './helpers.ts';

describe('asString', () => {
    // Técnicas: Particiones de equivalencia (tipos de input)
    // - PE1: null → sin conversión
    // - PE2: undefined → sin conversión
    // - PE3: number → conversión a string
    // - PE4: string → sin cambios
    // - PE5: boolean → conversión a string
    it('retorna null para null', () => {
        expect(asString(null)).toBe(null);
    });

    it('retorna undefined para undefined', () => {
        expect(asString(undefined)).toBe(undefined);
    });

    it.each([
        ['number positivo', 42, '42'],
        ['number cero', 0, '0'],
        ['number negativo', -1, '-1'],
        ['string vacío', '', ''],
        ['string no vacío', 'hello', 'hello'],
        ['boolean true', true, 'true'],
        ['boolean false', false, 'false'],
    ])('convierte %s (%j) a "%s"', (_label, input, expected) => {
        expect(asString(input)).toBe(expected);
    });
});

describe('asNumber', () => {
    // Técnicas: Particiones de equivalencia + Valores límite
    // - PE1: null/undefined → null
    // - PE2: Enteros válidos → número parseado
    // - PE3: Decimales → truncados a entero
    // - PE4: No numéricos → NaN
    // - VL: 0, '', espacios en blanco
    it('retorna null para undefined', () => {
        expect(asNumber(undefined)).toBe(null);
    });

    it('retorna null para null', () => {
        expect(asNumber(null as unknown as string)).toBe(null);
    });

    it.each([
        ['entero positivo', '42', 42],
        ['entero cero', '0', 0],
        ['entero negativo', '-5', -5],
        ['número grande', '999999999', 999999999],
    ])('parsea %s: "%s" → %d', (_label, input, expected) => {
        expect(asNumber(input)).toBe(expected);
    });

    it.each([
        ['decimal', '3.14', 3],
        ['notación científica', '1e3', 1],
        ['hexadecimal', '0x1A', 0],
        ['con espacios', '  42  ', 42],
    ])('trunca %s: "%s" → %d', (_label, input, expected) => {
        expect(asNumber(input)).toBe(expected);
    });

    it.each([
        ['no numérico', 'abc'],
        ['string vacío', ''],
    ])('retorna NaN para %s ("%s")', (_label, input) => {
        expect(asNumber(input)).toBeNaN();
    });
});

describe('toDateTimeModification', () => {
    // Técnicas: Particiones de equivalencia + Valores límite
    // - PE1: ISO datetime completo (≥16 chars)
    // - PE2: ISO date solo (10 chars)
    // - PE3: String vacío (0 chars)
    // - VL: Longitudes exactas 0, 10, 16
    it.each([
        ['ISO datetime completo', '2025-01-15T10:00:00', { date: '2025-01-15', time: '10:00' }],
        ['ISO con timezone', '2025-12-31T23:59:00+02:00', { date: '2025-12-31', time: '23:59' }],
        ['fecha mínima', '2000-01-01T00:00:00', { date: '2000-01-01', time: '00:00' }],
        ['solo fecha (10 chars)', '2025-01-15', { date: '2025-01-15', time: '' }],
        ['string vacío', '', { date: '', time: '' }],
        ['exactamente 16 chars', '2025-01-15T10:00', { date: '2025-01-15', time: '10:00' }],
    ])('parsea %s: "%s"', (_label, input, expected) => {
        expect(toDateTimeModification(input)).toEqual(expected);
    });
});

describe('extractDateTime', () => {
    // Técnicas: Particiones de equivalencia + Valores límite
    // - PE1: null/undefined → ''
    // - PE2: String válido ≥16 chars → primeros 16
    // - PE3: String corto <16 chars → string completo
    // - VL: 0, 11, 16 chars
    it.each([
        ['undefined', undefined, ''],
        ['null', null, ''],
        ['string vacío', '', ''],
    ])('retorna vacío para %s', (_label, input, expected) => {
        expect(extractDateTime(input as unknown as string)).toBe(expected);
    });

    it.each([
        ['ISO válido (19 chars)', '2025-01-15T10:00:00', '2025-01-15T10:00'],
        ['string corto (11 chars)', '2025-01-15T', '2025-01-15T'],
    ])('extrae de %s: "%s" → "%s"', (_label, input, expected) => {
        expect(extractDateTime(input)).toBe(expected);
    });
});

describe('escapeHtml', () => {
    // Técnicas: Particiones de equivalencia
    // - PE1: Sin caracteres especiales → sin cambios
    // - PE2: & → &amp;
    // - PE3: < → &lt;
    // - PE4: > → &gt;
    // - PE5: String vacío → vacío
    it('string sin especiales sin cambios', () => {
        expect(escapeHtml('hello')).toBe('hello');
    });

    it('string vacío retorna vacío', () => {
        expect(escapeHtml('')).toBe('');
    });

    it.each([
        ['ampersand', '&', '&amp;'],
        ['less-than', '<', '&lt;'],
        ['greater-than', '>', '&gt;'],
    ])('escapa %s: "%s" → "%s"', (_label, input, expected) => {
        expect(escapeHtml(input)).toBe(expected);
    });

    it('escapa script injection', () => {
        expect(escapeHtml('<script>alert(1)</script>')).toBe(
            '&lt;script&gt;alert(1)&lt;/script&gt;',
        );
    });

    it('múltiples caracteres especiales', () => {
        const result = escapeHtml('Tom & Jerry "say" <hi>');
        expect(result).toContain('&amp;');
        expect(result).toContain('&lt;');
        expect(result).toContain('&gt;');
    });
});

describe('supportedLanguages', () => {
    // Técnicas: Particiones de equivalencia
    // - PE1: undefined/no existe → []
    // - PE2: null → []
    // - PE3: JSON válido array vacío → []
    // - PE4: JSON válido con datos → array
    // - PE5: JSON inválido → SyntaxError
    afterEach(() => {
        delete (window as any).SUPPORTED_LANGUAGES;
    });

    it('retorna array vacío cuando no está definido', () => {
        expect(supportedLanguages()).toEqual([]);
    });

    it('retorna array vacío cuando es null', () => {
        (window as any).SUPPORTED_LANGUAGES = null;
        expect(supportedLanguages()).toEqual([]);
    });

    it('retorna array vacío para JSON []', () => {
        (window as any).SUPPORTED_LANGUAGES = '[]';
        expect(supportedLanguages()).toEqual([]);
    });

    it('retorna array con datos', () => {
        const data = [{ locale: 'en', value: 1, language: 'English', displayLanguage: 'English' }];
        (window as any).SUPPORTED_LANGUAGES = JSON.stringify(data);
        expect(supportedLanguages()).toEqual(data);
    });

    it('lanza SyntaxError con JSON inválido', () => {
        (window as any).SUPPORTED_LANGUAGES = 'not-json';
        expect(() => supportedLanguages()).toThrow(SyntaxError);
    });
});

describe('notifyChange', () => {
    // Técnicas: Particiones de equivalencia
    // - PE1: target null → sin llamada a handleChange
    // - PE2: target válido → handleChange(value)
    // - PE3: transformer custom → handleChange(transform(value))
    it('no llama handleChange si target es null', () => {
        const field = { handleChange: vi.fn() };
        const event = { currentTarget: null } as unknown as InputEvent;
        notifyChange(event, field);
        expect(field.handleChange).not.toHaveBeenCalled();
    });

    it('llama handleChange con valor del input', () => {
        const field = { handleChange: vi.fn() };
        const event = {
            currentTarget: { value: 'hello' },
        } as unknown as InputEvent;
        notifyChange(event, field);
        expect(field.handleChange).toHaveBeenCalledWith('hello');
    });

    it('llama handleChange con transformer custom', () => {
        const field = { handleChange: vi.fn() };
        const event = {
            currentTarget: { value: '123' },
        } as unknown as InputEvent;
        notifyChange(event, field, Number);
        expect(field.handleChange).toHaveBeenCalledWith(123);
    });

    it('llama handleChange con valor vacío', () => {
        const field = { handleChange: vi.fn() };
        const event = {
            currentTarget: { value: '' },
        } as unknown as InputEvent;
        notifyChange(event, field);
        expect(field.handleChange).toHaveBeenCalledWith('');
    });
});

describe('performRequest (via postJson/putJson/callDelete)', () => {
    let fetchMock: ReturnType<typeof vi.fn>;

    beforeEach(() => {
        fetchMock = vi.fn().mockResolvedValue({ ok: true, json: async () => ({}) } as Response);
        vi.stubGlobal('fetch', fetchMock);
        const metaHeader = document.createElement('meta');
        metaHeader.setAttribute('name', '_csrf_header');
        metaHeader.setAttribute('content', 'X-XSRF-TOKEN');
        document.head.appendChild(metaHeader);

        const metaToken = document.createElement('meta');
        metaToken.setAttribute('name', '_csrf');
        metaToken.setAttribute('content', 'test-token-123');
        document.head.appendChild(metaToken);
    });

    afterEach(() => {
        document.head.querySelectorAll('meta[name="_csrf_header"], meta[name="_csrf"]').forEach(el => el.remove());
        vi.restoreAllMocks();
    });

    it('CP-HTTP-01: postJson envía URLSearchParams con content-type correcto', async () => {
        const payload = new URLSearchParams({ key: 'val' });

        await postJson('https://example.com/api/test', payload);

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        const headers = callArgs[1].headers;
        expect(headers['Content-Type']).toBe('application/x-www-form-urlencoded');
    });

    it('CP-HTTP-02: postJson envía objeto JSON con content-type correcto', async () => {
        const payload = { foo: 'bar' };

        await postJson('https://example.com/api/test', payload);

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        expect(callArgs[0]).toBe('https://example.com/api/test');
        expect(callArgs[1].method).toBe('POST');
        expect(callArgs[1].body).toBe(JSON.stringify(payload));
        expect(callArgs[1].headers['Content-Type']).toBe('application/json');
    });

    it('CP-HTTP-03: callDelete envía DELETE con null body', async () => {
        await callDelete('https://example.com/api/test');

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        expect(callArgs[1].method).toBe('DELETE');
        expect(callArgs[1].body).toBeNull();
    });

    it('CP-HTTP-04: postJson con undefined envía null body', async () => {
        await postJson('https://example.com/api/test', undefined);

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        expect(callArgs[1].body).toBeNull();
    });

    it('CP-HTTP-05: incluye headers CSRF', async () => {
        await postJson('https://example.com/api/test', {});

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        const headers = callArgs[1].headers;
        expect(headers['X-XSRF-TOKEN']).toBe('test-token-123');
    });

    it('CP-HTTP-06: putJson envía method PUT', async () => {
        await putJson('https://example.com/api/test', { x: 1 });

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        expect(callArgs[1].method).toBe('PUT');
    });

    it('CP-HTTP-07: putJson con URLSearchParams', async () => {
        await putJson('https://example.com/api/test', new URLSearchParams());

        expect(fetchMock).toHaveBeenCalled();
        const callArgs = fetchMock.mock.calls[0];
        expect(callArgs[1].method).toBe('PUT');
        expect(callArgs[1].headers['Content-Type']).toBe('application/x-www-form-urlencoded');
    });
});

describe('fetchJson', () => {
    let fetchMock: ReturnType<typeof vi.fn>;

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('CP-FJ-01: retorna JSON parseado', async () => {
        const data = { data: 1 };
        fetchMock = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => data,
        } as Response);
        vi.stubGlobal('fetch', fetchMock);

        const result = await fetchJson('https://example.com/api/test');
        expect(result).toEqual(data);
    });

    it('CP-FJ-02: usa GET method y credentials', async () => {
        fetchMock = vi.fn().mockResolvedValue({
            ok: true,
            json: async () => ({}),
        } as Response);
        vi.stubGlobal('fetch', fetchMock);

        await fetchJson('https://example.com/api/test');

        expect(fetchMock).toHaveBeenCalledWith('https://example.com/api/test', {
            method: 'GET',
            credentials: 'include',
        });
    });
});
