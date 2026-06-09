import { callDelete, fetchJson, postJson, putJson } from './helpers';

export class CustomPaymentMethodsService {
    /**
     * Obtiene los métodos de pago configurados para una organización.
     *
     * @param organizationId - ID de la organización
     * @returns Array de CustomOfflinePayment con los métodos configurados
     */
    async getPaymentMethodsForOrganization(organizationId: number) {
        const result = await fetchJson<CustomOfflinePayment[]>(
            `/admin/api/configuration/organizations/${organizationId}/payment-method`,
        );
        return result;
    }

    /**
     * Crea un nuevo método de pago para una organización.
     *
     * @param organizationId - ID de la organización
     * @param paymentMethod - Datos del método de pago a crear
     * @returns Promise con la respuesta del servidor
     */
    async createPaymentMethod(
        organizationId: number,
        paymentMethod: CustomOfflinePayment,
    ) {
        const result = await postJson(
            `/admin/api/configuration/organizations/${organizationId}/payment-method`,
            paymentMethod,
        );
        return result;
    }

    /**
     * Actualiza un método de pago existente por su ID.
     *
     * @param organizationId - ID de la organización
     * @param existingMethodId - ID del método a actualizar
     * @param paymentMethod - Datos actualizados del método
     * @returns Promise con la respuesta del servidor
     */
    async updatePaymentMethod(
        organizationId: number,
        existingMethodId: string,
        paymentMethod: CustomOfflinePayment,
    ) {
        const result = await putJson(
            `/admin/api/configuration/organizations/${organizationId}/payment-method/${existingMethodId}`,
            paymentMethod,
        );
        return result;
    }

    /**
     * Elimina un método de pago por su ID.
     *
     * @param organizationId - ID de la organización
     * @param existingMethodId - ID del método a eliminar
     * @returns Promise con la respuesta del servidor
     */
    async deletePaymentMethod(
        organizationId: number,
        existingMethodId: string,
    ) {
        const result = await callDelete(
            `/admin/api/configuration/organizations/${organizationId}/payment-method/${existingMethodId}`,
        );
        return result;
    }

    /**
     * Establece los métodos de pago permitidos para un evento.
     * Reemplaza la lista completa de métodos.
     *
     * @param eventId - ID del evento
     * @param paymentMethodIds - Array de IDs de métodos permitidos
     * @returns Promise con la respuesta del servidor
     */
    async setPaymentMethodsForEvent(
        eventId: number,
        paymentMethodIds: string[],
    ) {
        const result = await postJson(
            `/admin/api/configuration/event/${eventId}/payment-method`,
            paymentMethodIds,
        );
        return result;
    }

    /**
     * Obtiene los métodos de pago permitidos para un evento.
     *
     * @param eventId - ID del evento
     * @returns Array de CustomOfflinePayment con los métodos permitidos
     */
    async getAllowedPaymentMethodsForEvent(eventId: number) {
        const result = await fetchJson<CustomOfflinePayment[]>(
            `/admin/api/configuration/event/${eventId}/payment-method`,
        );
        return result;
    }

    /**
     * Obtiene los métodos de pago denegados para una categoría específica.
     *
     * @param eventId - ID del evento
     * @param categoryId - ID de la categoría
     * @returns Array de strings con los IDs de métodos denegados
     */
    async getDeniedPaymentMethodsForCategory(
        eventId: number,
        categoryId: number,
    ) {
        const result = await fetchJson<string[]>(
            `/admin/api/events/${eventId}/categories/${categoryId}/denied-custom-payment-methods`,
        );
        return result;
    }

    /**
     * Establece los métodos de pago denegados para una categoría específica.
     *
     * @param eventId - ID del evento
     * @param categoryId - ID de la categoría
     * @param paymentMethodIds - Array de IDs de métodos a denegar
     * @returns Promise con la respuesta del servidor
     */
    async setDeniedPaymentMethodsForCategory(
        eventId: number,
        categoryId: number,
        paymentMethodIds: string[],
    ) {
        const result = await postJson(
            `/admin/api/events/${eventId}/categories/${categoryId}/denied-custom-payment-methods`,
            paymentMethodIds,
        );
        return result;
    }
}
