import { fetchJson } from './helpers.ts';

export class UtilService {
    /**
     * Renderiza texto CommonMark (markdown) a HTML en el servidor.
     * El texto se codifica como parámetro de consulta para seguridad.
     *
     * @param text - Texto en formato CommonMark
     * @returns HTML renderizado
     * @example UtilService.renderMarkdown('**negrita** → '<strong>negrita</strong>')
     */
    static renderMarkdown(text: string): Promise<string> {
        return fetchJson(
            `/admin/api/utils/render-commonmark?text=${encodeURIComponent(text)}`,
        );
    }
}
