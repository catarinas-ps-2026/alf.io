import type { AdditionalItem } from '../model/additional-item.ts';
import type { ValidatedResponse } from '../model/validation.ts';
import { callDelete, fetchJson, postJson, putJson } from './helpers.ts';

export type UsageCount = { [id: number]: { [status: string]: number } };

export class AdditionalItemService {
    /**
     * Carga todos los items adicionales para un evento.
     *
     * @param options - Objeto con eventId
     * @param options.eventId - ID del evento
     * @returns Array de AdditionalItem con todos los items configurados
     */
    static async loadAll({
        eventId,
    }: {
        eventId: number;
    }): Promise<Array<AdditionalItem>> {
        return await fetchJson(
            `/admin/api/event/${eventId}/additional-services`,
        );
    }

    /**
     * Obtiene el conteo de uso de items adicionales por evento.
     *
     * @param eventId - ID del evento
     * @returns Objeto UsageCount con conteo por item y estado
     */
    static async useCount(eventId: number): Promise<UsageCount> {
        return await fetchJson(
            `/admin/api/event/${eventId}/additional-services/count`,
        );
    }

    /**
     * Valida un item adicional contra el servidor.
     * Retorna ValidatedResponse con errores si los hay.
     *
     * @param additionalItem - Datos del item a validar (parcial)
     * @returns ValidatedResponse con errores de validación si los hay
     */
    static async validateAdditionalItem(
        additionalItem: Partial<AdditionalItem>,
    ): Promise<ValidatedResponse<AdditionalItem>> {
        const response = await postJson(
            '/admin/api/additional-services/validate',
            additionalItem,
        );
        return response.json();
    }

    /**
     * Actualiza o crea un item adicional.
     * Si tiene id → PUT (actualizar), si no → POST (crear).
     *
     * @param additionalItem - Datos del item (parcial, con o sin id)
     * @param eventId - ID del evento al que pertenece el item
     * @returns Promise con la respuesta del servidor
     */
    static async updateAdditionalItem(
        additionalItem: Partial<AdditionalItem>,
        eventId: number,
    ): Promise<Response> {
        if (additionalItem.id != null) {
            return await putJson(
                `/admin/api/event/${eventId}/additional-services/${additionalItem.id}`,
                additionalItem,
            );
        }
        return await postJson(
            `/admin/api/event/${eventId}/additional-services`,
            additionalItem,
        );
    }

    /**
     * Elimina un item adicional por ID.
     *
     * @param additionalItemId - ID del item a eliminar
     * @param eventId - ID del evento al que pertenece el item
     * @returns Promise con la respuesta del servidor
     */
    static async deleteAdditionalItem(
        additionalItemId: number,
        eventId: number,
    ): Promise<Response> {
        return await callDelete(
            `/admin/api/event/${eventId}/additional-services/${additionalItemId}`,
        );
    }

    /**
     * Intercambia la posición de dos items adicionales.
     * Los IDs se envían como body JSON (no en URL).
     *
     * @param eventId - ID del evento
     * @param firstId - ID del primer item
     * @param secondId - ID del segundo item
     * @returns Promise con la respuesta del servidor
     */
    static async swapItems(
        eventId: number,
        firstId: number,
        secondId: number,
    ): Promise<Response> {
        return await postJson(
            `/admin/api/event/${eventId}/additional-services/swap-position`,
            {
                id1: firstId,
                id2: secondId,
            },
        );
    }
}
