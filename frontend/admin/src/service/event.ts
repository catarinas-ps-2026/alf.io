import type { EventWithOrganization } from '../model/event.ts';
import { fetchJson } from './helpers.ts';

export class EventService {
    /**
     * Carga un evento por su identificador público.
     * Retorna el evento completo con datos de la organización.
     *
     * @param publicIdentifier - Identificador único del evento (slug o ID público)
     * @example EventService.load('mi-conferencia-2025')
     */
    static load(publicIdentifier: string): Promise<EventWithOrganization> {
        return fetchJson(`/admin/api/events/${publicIdentifier}`);
    }
}
