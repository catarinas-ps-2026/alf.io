import { callDelete, fetchJson, postJson, putJson } from './helpers';

export class CustomPaymentMethodsService {
    /**
     * Obtiene los métodos de pago configurados para una organización.
     */
    async getPaymentMethodsForOrganization(organizationId: number) {
        const result = await fetchJson<CustomOfflinePayment[]>(
            `/admin/api/configuration/organizations/${organizationId}/payment-method`,
        );
        return result;
    }

    /**
     * Crea un nuevo método de pago para una organización.
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
     */
    async getAllowedPaymentMethodsForEvent(eventId: number) {
        const result = await fetchJson<CustomOfflinePayment[]>(
            `/admin/api/configuration/event/${eventId}/payment-method`,
        );
        return result;
    }

    /**
     * Obtiene los métodos de pago denegados para una categoría específica.
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
