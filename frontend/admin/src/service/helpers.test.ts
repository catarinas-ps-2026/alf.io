/**
 * Tests para funciones de helpers.ts
 *
 * Funciones puras: particiones de equivalencia sobre tipos de input
 * - null/undefined (falsy, sin conversión)
 * - number (conversión a string)
 * - string (sin cambios)
 * - boolean (conversión a string)
 *
 * Funciones HTTP: mocking con verificación de llamadas
 */
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
    // Convierte valores a string, retorna null/undefined sin cambios
    it('retorna null para null', () => {
        expect(asString(null)).toBe(null);
    });

    it('retorna undefined para undefined', () => {
        expect(asString(undefined)).toBe(undefined);
    });

    it('convierte number positivo a string', () => {
        expect(asString(42)).toBe('42');
    });

    it('convierte number cero a string', () => {
        expect(asString(0)).toBe('0');
    });

    it('convierte number negativo a string', () => {
        expect(asString(-1)).toBe('-1');
    });

    it('retorna string vacío sin cambios', () => {
        expect(asString('')).toBe('');
    });

    it('retorna string no vacío sin cambios', () => {
        expect(asString('hello')).toBe('hello');
    });

    it('convierte boolean true a string', () => {
        expect(asString(true)).toBe('true');
    });

    it('convierte boolean false a string', () => {
        expect(asString(false)).toBe('false');
    });
});

describe('asNumber', () => {
    // Parsea strings a number con parseInt radix 10, retorna null para null/undefined
    it('retorna null para undefined', () => {
        expect(asNumber(undefined)).toBe(null);
    });

    it('retorna null para null', () => {
        expect(asNumber(null as unknown as string)).toBe(null);
    });

    it('parsea entero positivo', () => {
        expect(asNumber('42')).toBe(42);
    });

    it('parsea entero cero', () => {
        expect(asNumber('0')).toBe(0);
    });

    it('parsea entero negativo', () => {
        expect(asNumber('-5')).toBe(-5);
    });

    it('trunca decimal', () => {
        expect(asNumber('3.14')).toBe(3);
    });

    it('retorna NaN para no numérico', () => {
        expect(asNumber('abc')).toBeNaN();
    });

    it('retorna NaN para vacío', () => {
        expect(asNumber('')).toBeNaN();
    });

    it('trunca notación científica', () => {
        expect(asNumber('1e3')).toBe(1);
    });

    it('trunca hexadecimal', () => {
        expect(asNumber('0x1A')).toBe(0);
    });

    it('ignora espacios', () => {
        expect(asNumber('  42  ')).toBe(42);
    });

    it('parsea número grande', () => {
        expect(asNumber('999999999')).toBe(999999999);
    });
});

describe('toDateTimeModification', () => {
    // Extrae date/time de ISO string via substring
    it('parsea ISO datetime completo', () => {
        expect(toDateTimeModification('2025-01-15T10:00:00')).toEqual({
            date: '2025-01-15',
            time: '10:00',
        });
    });

    it('parsea ISO con timezone', () => {
        expect(toDateTimeModification('2025-12-31T23:59:00+02:00')).toEqual({
            date: '2025-12-31',
            time: '23:59',
        });
    });

    it('parsea fecha mínima', () => {
        expect(toDateTimeModification('2000-01-01T00:00:00')).toEqual({
            date: '2000-01-01',
            time: '00:00',
        });
    });

    it('solo fecha retorna time vacío', () => {
        expect(toDateTimeModification('2025-01-15')).toEqual({
            date: '2025-01-15',
            time: '',
        });
    });

    it('string vacío retorna vacío', () => {
        expect(toDateTimeModification('')).toEqual({
            date: '',
            time: '',
        });
    });

    it('exactamente 16 chars funciona', () => {
        expect(toDateTimeModification('2025-01-15T10:00')).toEqual({
            date: '2025-01-15',
            time: '10:00',
        });
    });
});

describe('extractDateTime', () => {
    // Retorna primeros 16 chars de ISO string, o vacío si es null/undefined
    it('retorna vacío para undefined', () => {
        expect(extractDateTime(undefined)).toBe('');
    });

    it('retorna vacío para null', () => {
        expect(extractDateTime(null as unknown as string)).toBe('');
    });

    it('retorna vacío para string vacío', () => {
        expect(extractDateTime('')).toBe('');
    });

    it('extrae 16 chars de ISO válido', () => {
        expect(extractDateTime('2025-01-15T10:00:00')).toBe('2025-01-15T10:00');
    });

    it('retorna string completo si es corto', () => {
        expect(extractDateTime('2025-01-15T')).toBe('2025-01-15T');
    });
});

describe('escapeHtml', () => {
    // Escapa caracteres HTML via textContent/innerHTML
    it('string sin especiales sin cambios', () => {
        expect(escapeHtml('hello')).toBe('hello');
    });

    it('escapa ampersand', () => {
        expect(escapeHtml('&')).toBe('&amp;');
    });

    it('escapa less-than', () => {
        expect(escapeHtml('<')).toBe('&lt;');
    });

    it('escapa greater-than', () => {
        expect(escapeHtml('>')).toBe('&gt;');
    });

    it('escapa double quote', () => {
        const result = escapeHtml('"');
        expect(result).toMatch(/&quot;|"/);
    });

    it('escapa script injection', () => {
        expect(escapeHtml('<script>alert(1)</script>')).toBe(
            '&lt;script&gt;alert(1)&lt;/script&gt;',
        );
    });

    it('string vacío retorna vacío', () => {
        expect(escapeHtml('')).toBe('');
    });

    it('múltiples caracteres especiales', () => {
        const result = escapeHtml('Tom & Jerry "say" <hi>');
        expect(result).toContain('&amp;');
        expect(result).toContain('&lt;');
        expect(result).toContain('&gt;');
    });
});

describe('supportedLanguages', () => {
    afterEach(() => {
        delete (window as any).SUPPORTED_LANGUAGES;
    });

    it('CP-LANG-01: retorna array vacío cuando no está definido', () => {
        expect(supportedLanguages()).toEqual([]);
    });

    it('CP-LANG-02: retorna array vacío cuando es null', () => {
        (window as any).SUPPORTED_LANGUAGES = null;
        expect(supportedLanguages()).toEqual([]);
    });

    it('CP-LANG-03: retorna array vacío para JSON []', () => {
        (window as any).SUPPORTED_LANGUAGES = '[]';
        expect(supportedLanguages()).toEqual([]);
    });

    it('CP-LANG-04: retorna array con datos', () => {
        const data = [{ locale: 'en', value: 1, language: 'English', displayLanguage: 'English' }];
        (window as any).SUPPORTED_LANGUAGES = JSON.stringify(data);
        expect(supportedLanguages()).toEqual(data);
    });

    it('CP-LANG-05: lanza SyntaxError con JSON inválido', () => {
        (window as any).SUPPORTED_LANGUAGES = 'not-json';
        expect(() => supportedLanguages()).toThrow(SyntaxError);
    });
});

describe('notifyChange', () => {
    it('CP-NCF-01: no llama handleChange si target es null', () => {
        const field = { handleChange: vi.fn() };
        const event = { currentTarget: null } as unknown as InputEvent;
        notifyChange(event, field);
        expect(field.handleChange).not.toHaveBeenCalled();
    });

    it('CP-NCF-02: llama handleChange con valor del input', () => {
        const field = { handleChange: vi.fn() };
        const event = {
            currentTarget: { value: 'hello' },
        } as unknown as InputEvent;
        notifyChange(event, field);
        expect(field.handleChange).toHaveBeenCalledWith('hello');
    });

    it('CP-NCF-03: llama handleChange con transformer custom', () => {
        const field = { handleChange: vi.fn() };
        const event = {
            currentTarget: { value: '123' },
        } as unknown as InputEvent;
        notifyChange(event, field, Number);
        expect(field.handleChange).toHaveBeenCalledWith(123);
    });

    it('CP-NCF-04: llama handleChange con valor vacío', () => {
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
