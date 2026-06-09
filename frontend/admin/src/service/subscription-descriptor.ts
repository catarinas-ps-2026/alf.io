import type { SubscriptionDescriptor } from '../model/subscription-descriptor.ts';
import { fetchJson } from './helpers.ts';

export class SubscriptionDescriptorService {
    /**
     * Carga un descriptor de suscripción por identificador y organización.
     *
     * @param publicIdentifier - Identificador único de la suscripción
     * @param organizationId - ID de la organización propietaria
     * @example SubscriptionDescriptorService.load('sub-mensual-1', 42)
     */
    static load(
        publicIdentifier: string,
        organizationId: number,
    ): Promise<SubscriptionDescriptor> {
        return fetchJson(
            `/admin/api/organization/${organizationId}/subscription/${publicIdentifier}`,
        );
    }
}
