import { vi } from 'vitest';

/**
 * Mockea global.fetch para retornar siempre el JSON especificado.
 * Útil para tests que hacen múltiples llamadas fetch.
 */
export function mockFetchJson(data: unknown): void {
    vi.stubGlobal(
        'fetch',
        vi.fn().mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue(data),
        }),
    );
}

/**
 * Mockea global.fetch para retornar el JSON especificado solo una vez.
 * Útil para tests con múltiples llamadas fetch secuenciales.
 */
export function mockFetchJsonOnce(data: unknown): void {
    vi.stubGlobal(
        'fetch',
        vi.fn().mockResolvedValueOnce({
            ok: true,
            json: vi.fn().mockResolvedValue(data),
        }),
    );
}

/**
 * Mockea global.fetch con una respuesta personalizada.
 * Permite configurar ok, status, y json.
 */
export function mockFetchResponse(
    options: { ok?: boolean; json?: unknown; status?: number } = {},
): void {
    const { ok = true, json = {}, status = 200 } = options;
    vi.stubGlobal(
        'fetch',
        vi.fn().mockResolvedValue({
            ok,
            status,
            json: vi.fn().mockResolvedValue(json),
        }),
    );
}

/**
 * Inyecta meta tags CSRF en el DOM para tests de servicios HTTP.
 * Los servicios leen estos valores para los headers X-XSRF-TOKEN.
 */
export function mockCsrfMeta(
    xsrfHeader = 'X-XSRF-TOKEN',
    xsrfValue = 'test-token-123',
): void {
    const metaHeader = document.createElement('meta');
    metaHeader.setAttribute('name', '_csrf_header');
    metaHeader.setAttribute('content', xsrfHeader);

    const metaToken = document.createElement('meta');
    metaToken.setAttribute('name', '_csrf');
    metaToken.setAttribute('content', xsrfValue);

    document.head.appendChild(metaHeader);
    document.head.appendChild(metaToken);
}

/**
 * Elimina las meta tags CSRF del DOM.
 * Usar en afterEach para limpiar después de cada test.
 */
export function clearCsrfMeta(): void {
    document.head
        .querySelectorAll('meta[name="_csrf_header"], meta[name="_csrf"]')
        .forEach((el) => el.remove());
}

/**
 * Retorna la referencia al mock de global.fetch.
 * Útil para verificar llamadas con expect(mock).toHaveBeenCalled().
 */
export function getFetchMock(): ReturnType<typeof vi.fn> {
    return globalThis.fetch as ReturnType<typeof vi.fn>;
}

/**
 * Restaura todos los mocks y globals stubbed.
 * Usar en afterEach para limpiar después de cada test.
 */
export function resetFetchMock(): void {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
}
