import { postJson } from './helpers';

export class ConfigurationService {
    /**
     * Actualiza una configuración clave-valor en el servidor.
     * Usado para configuraciones globales del sistema (tema, idioma, etc.).
     *
     * @param kv - Objeto con key (nombre de la configuración) y value (nuevo valor)
     * @example ConfigurationService.update({ key: 'theme', value: 'dark' })
     */
    static update(kv: { key: string; value: string }): Promise<Response> {
        return postJson('/admin/api/configuration/update', kv);
    }
}
