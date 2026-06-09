import type { EventWithOrganization } from '../model/event.ts';
import type { PurchaseContextType } from '../model/purchase-context.ts';
import type { SubscriptionDescriptor } from '../model/subscription-descriptor.ts';
import { EventService } from './event.ts';
import { SubscriptionDescriptorService } from './subscription-descriptor.ts';

export class PurchaseContextService {
    /**
     * Carga un contexto de compra (evento o suscripción) según el tipo indicado.
     * - Si type es 'subscription', delega a SubscriptionDescriptorService
     * - Si type es 'event', delega a EventService
     *
     * @param publicIdentifier - Identificador único del contexto de compra
     * @param type - Tipo de contexto: 'event' o 'subscription'
     * @param organizationId - ID de la organización (usado solo para suscripciones)
     * @returns Objeto con eventWithOrganization o subscriptionDescriptor según el tipo
     *
     * @example
     * // Cargar evento
     * await PurchaseContextService.load('mi-evento', 'event', 1)
     * // → { eventWithOrganization: {...} }
     *
     * // Cargar suscripción
     * await PurchaseContextService.load('sub-mensual', 'subscription', 42)
     * // → { subscriptionDescriptor: {...} }
     */
    static async load(
        publicIdentifier: string,
        type: PurchaseContextType,
        organizationId: number,
    ): Promise<{
        eventWithOrganization?: EventWithOrganization;
        subscriptionDescriptor?: SubscriptionDescriptor;
    }> {
        if (type === 'subscription') {
            return {
                subscriptionDescriptor:
                    await SubscriptionDescriptorService.load(
                        publicIdentifier,
                        organizationId,
                    ),
            };
        }
        return {
            eventWithOrganization: await EventService.load(publicIdentifier),
        };
    }
}
