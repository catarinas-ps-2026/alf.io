import { fetchJson } from './helpers';

export type LocalizationServiceLocale = {
    locale: string;
    value: number;
    language: string;
    displayLanguage: string;
};

export class LocalizationService {
    /**
     * Obtiene la lista de idiomas soportados para eventos.
     * Retorna un array de LocalizationServiceLocale con cada idioma disponible.
     */
    async getEventsSupportedLanguages() {
        const result = await fetchJson<LocalizationServiceLocale[]>(
            `/admin/api/events-supported-languages`,
        );
        return result;
    }
}
