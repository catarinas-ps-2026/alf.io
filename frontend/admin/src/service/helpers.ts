import { html, nothing, type TemplateResult } from 'lit';
import { when } from 'lit/directives/when.js';
import type { DateTimeModification } from '../model/event.ts';
import type { ContentLanguage } from '../model/purchase-context.ts';

export function postJson(url: string, payload: any): Promise<Response> {
    return performRequest(url, 'POST', payload);
}

export function putJson(url: string, payload: any): Promise<Response> {
    return performRequest(url, 'PUT', payload);
}

export function callDelete(url: string): Promise<Response> {
    return performRequest(url, 'DELETE', null);
}

function performRequest(
    url: string,
    method: 'PUT' | 'POST' | 'DELETE',
    payload: any,
): Promise<Response> {
    const xsrfName = document
        .querySelector('meta[name=_csrf_header]')
        ?.getAttribute('content') as string;
    const xsrfValue = document
        .querySelector('meta[name=_csrf]')
        ?.getAttribute('content') as string;

    let body: URLSearchParams | string | null = null;

    if (payload instanceof URLSearchParams) {
        body = payload;
    } else if (payload != null) {
        body = JSON.stringify(payload);
    }

    return fetch(url, {
        method,
        credentials: 'include',
        headers: {
            Accept: 'application/json',
            'Content-Type':
                payload instanceof URLSearchParams
                    ? 'application/x-www-form-urlencoded'
                    : 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            [xsrfName]: xsrfValue,
        },
        body,
    });
}

export function fetchJson<T>(url: string): Promise<T> {
    return fetch(url, {
        method: 'GET',
        credentials: 'include',
    }).then((r) => r.json());
}

export function renderIf(
    predicate: () => boolean,
    template: () => TemplateResult,
): TemplateResult {
    return html`${when(predicate(), template, () => nothing)}`;
}

export function supportedLanguages(): ContentLanguage[] {
    if (window.SUPPORTED_LANGUAGES != null) {
        return JSON.parse(window.SUPPORTED_LANGUAGES);
    }
    return [];
}

/**
 * Extrae date y time de un string ISO datetime usando substring.
 * - date: primeros 10 caracteres (YYYY-MM-DD)
 * - time: caracteres 11-15 (HH:MM)
 * - Si el string es más corto, retorna strings vacíos
 *
 * @example
 * toDateTimeModification('2025-01-15T10:00:00') // { date: '2025-01-15', time: '10:00' }
 * toDateTimeModification('2025-01-15')           // { date: '2025-01-15', time: '' }
 */
export function toDateTimeModification(
    isoString: string,
): DateTimeModification {
    return {
        date: isoString.substring(0, 10),
        time: isoString.substring(11, 16),
    };
}

/**
 * Extrae los primeros 16 caracteres de un string (formato ISO datetime).
 * - Si isoString es null o undefined, retorna string vacío
 * - Si el string tiene menos de 16 caracteres, retorna el string completo
 *
 * @example
 * extractDateTime('2025-01-15T10:00:00') // '2025-01-15T10:00'
 * extractDateTime(undefined)             // ''
 */
export function extractDateTime(isoString?: string): string {
    if (isoString != null) {
        return isoString.substring(0, 16);
    }
    return '';
}

export function notifyChange(
    event: InputEvent,
    field: { handleChange: (m: any) => void },
    // helps with boolean / number values
    valueTransformer: (v: string) => any = (s) => s,
): void {
    const target = event.currentTarget as HTMLInputElement | null;
    if (target != null) {
        field.handleChange(valueTransformer(target.value));
    }
}

/**
 * Escapa caracteres especiales de HTML usando textContent/innerHTML.
 * Convierte &, <, >, ", ' a sus entidades HTML correspondientes.
 *
 * @example
 * escapeHtml('<script>')  // '&lt;script&gt;'
 * escapeHtml('Tom & Jerry') // 'Tom &amp; Jerry'
 */
export function escapeHtml(message: string): string {
    const div = document.createElement('div');
    div.textContent = message;
    return div.innerHTML;
}

/**
 * Convierte un valor a su representación en string.
 * - Si value es null o undefined, retorna el valor sin cambios
 * - Si value es un number/boolean, lo convierte a string
 * - Si value es un string, lo retorna sin cambios
 *
 * @example
 * asString(42)      // '42'
 * asString(null)    // null
 * asString('hello') // 'hello'
 */
export function asString(value: any): string | null {
    if (value != null) {
        return `${value}`;
    }
    return value;
}

/**
 * Parsea un string a número entero usando parseInt con radix 10.
 * - Si value es null o undefined, retorna null
 * - Si el string no es numérico, retorna NaN
 * - Decimales se truncan (ej: '3.14' → 3)
 * - Notación científica se trunca en 'e' (ej: '1e3' → 1)
 *
 * @example
 * asNumber('42')    // 42
 * asNumber('3.14')  // 3
 * asNumber('abc')   // NaN
 * asNumber()        // null
 */
export function asNumber(value?: string): number | null {
    if (value != null) {
        return Number.parseInt(value, 10);
    }
    return value ?? null;
}

declare global {
    interface Window {
        SUPPORTED_LANGUAGES: string | null;
    }
}
