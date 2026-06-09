import { vi } from 'vitest';

/**
 * Mockea global.fetch para retornar siempre el JSON especificado.
 * Útil para tests que hacen múltiples llamadas fetch.
 */
export function mockFetchJson(data: unknown): void {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
        ok: true,
        json: vi.fn().mockResolvedValue(data),
    }));
}

export function mockFetchJsonOnce(data: unknown): void {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValueOnce({
        ok: true,
        json: vi.fn().mockResolvedValue(data),
    }));
}

export function mockFetchResponse(options: { ok?: boolean; json?: unknown; status?: number } = {}): void {
    const { ok = true, json = {}, status = 200 } = options;
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
        ok,
        status,
        json: vi.fn().mockResolvedValue(json),
    }));
}

export function mockCsrfMeta(xsrfHeader = 'X-XSRF-TOKEN', xsrfValue = 'test-token-123'): void {
    const metaHeader = document.createElement('meta');
    metaHeader.setAttribute('name', '_csrf_header');
    metaHeader.setAttribute('content', xsrfHeader);

    const metaToken = document.createElement('meta');
    metaToken.setAttribute('name', '_csrf');
    metaToken.setAttribute('content', xsrfValue);

    document.head.appendChild(metaHeader);
    document.head.appendChild(metaToken);
}

export function clearCsrfMeta(): void {
    document.head.querySelectorAll('meta[name="_csrf_header"], meta[name="_csrf"]').forEach((el) => el.remove());
}

export function getFetchMock(): ReturnType<typeof vi.fn> {
    return globalThis.fetch as ReturnType<typeof vi.fn>;
}

export function resetFetchMock(): void {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
}
