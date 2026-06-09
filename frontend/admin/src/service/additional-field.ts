import type {
    AdditionalField,
    AdditionalFieldCreateRequest,
    AdditionalFieldStats,
    AdditionalFieldTemplate,
} from '../model/additional-field.ts';
import type { PurchaseContext } from '../model/purchase-context.ts';
import type { ValidatedResponse } from '../model/validation.ts';
import { callDelete, fetchJson, postJson } from './helpers.ts';

export class AdditionalFieldService {
    /**
     * Carga estadísticas de valores restringidos para un campo específico.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @param id - ID del campo adicional
     * @returns Array con las estadísticas por cada valor restringido
     */
    static loadRestrictedValuesStats(
        purchaseContext: PurchaseContext,
        id: number,
    ): Promise<ReadonlyArray<AdditionalFieldStats>> {
        return fetchJson(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/${id}/stats`,
        );
    }

    /**
     * Carga todos los campos adicionales para un contexto de compra (evento o suscripción).
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @returns Array de AdditionalField con todos los campos configurados
     */
    static loadAllByPurchaseContext(
        purchaseContext: PurchaseContext,
    ): Promise<ReadonlyArray<AdditionalField>> {
        return fetchJson(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field`,
        );
    }

    /**
     * Elimina un campo adicional por ID.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @param id - ID del campo a eliminar
     * @returns Promise con la respuesta del servidor
     */
    static deleteField(
        purchaseContext: PurchaseContext,
        id: number,
    ): Promise<Response> {
        return callDelete(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/${id}`,
        );
    }

    /**
     * Intercambia la posición de dos campos adicionales.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @param id1 - ID del primer campo
     * @param id2 - ID del segundo campo
     * @returns Promise con la respuesta del servidor
     */
    static swapFieldPosition(
        purchaseContext: PurchaseContext,
        id1: number,
        id2: number,
    ): Promise<Response> {
        return postJson(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/swap-position/${id1}/${id2}`,
            null,
        );
    }

    /**
     * Mueve un campo adicional a una posición específica.
     * Envía la posición como URLSearchParams.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @param id - ID del campo a mover
     * @param position - Nueva posición deseada (0-indexed)
     * @returns Promise con la respuesta del servidor
     */
    static moveField(
        purchaseContext: PurchaseContext,
        id: number,
        position: number,
    ): Promise<Response> {
        const body = new URLSearchParams();
        body.append('newPosition', String(position));
        return postJson(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/set-position/${id}`,
            body,
        );
    }

    /**
     * Carga las plantillas de campos adicionales disponibles.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @returns Array de AdditionalFieldTemplate con las plantillas disponibles
     */
    static loadTemplates(
        purchaseContext: PurchaseContext,
    ): Promise<ReadonlyArray<AdditionalFieldTemplate>> {
        return fetchJson(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/templates`,
        );
    }

    /**
     * Crea un nuevo campo adicional.
     * Retorna ValidatedResponse con errores de validación si los hay.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @param field - Datos del campo a crear
     * @returns ValidatedResponse con el campo creado o errores de validación
     */
    static async createNewField(
        purchaseContext: PurchaseContext,
        field: AdditionalFieldCreateRequest,
    ): Promise<ValidatedResponse<AdditionalField>> {
        const response = await postJson(
            `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/new`,
            field,
        );
        return response.json();
    }

    /**
     * Guarda un campo adicional existente.
     * Usa field.id en la URL para identificar el recurso.
     *
     * @param purchaseContext - Contexto de compra (evento o suscripción)
     * @param field - Campo adicional con id existente
     * @returns Promise con la respuesta del servidor
     */
    static async saveField(
        purchaseContext: PurchaseContext,
        field: AdditionalField,
    ): Promise<Response> {
        const url = `/admin/api/${purchaseContext.type}/${purchaseContext.publicIdentifier}/additional-field/${field.id}`;
        return await postJson(url, field);
    }
}
