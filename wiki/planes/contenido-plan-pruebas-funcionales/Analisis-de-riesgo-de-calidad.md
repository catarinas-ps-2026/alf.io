# Análisis de Riesgo de Calidad

**2.1 Riesgos Críticos (Alta probabilidad / Alto impacto)**
*   **Reserva concurrente y sobreventa (Overselling):**
    *   *Impacto:* Si múltiples usuarios intentan comprar los últimos tickets al mismo tiempo y el sistema falla, se venderían más tickets de los disponibles, causando problemas logísticos y de reputación graves.
    *   *Mitigación:* Diseño de casos de prueba funcionales enfocados en transacciones límite y validación estricta de concurrencia y control de cupos.
*   **Fallas en el procesamiento del pago simulado o generación de tickets:**
    *   *Impacto:* Los usuarios pagan (o creen pagar) pero no reciben su código QR de acceso. Impide la entrada al evento.
    *   *Mitigación:* Pruebas de integración exhaustivas en los flujos de confirmación de pago y emisión de tickets.

**2.2 Riesgos Medios y Bajos**
*   **Visualización incorrecta de los detalles del evento (Medio):**
    *   *Impacto:* Los asistentes pueden ver información desactualizada o incorrecta sobre fechas o lugares del evento.
    *   *Mitigación:* Pruebas de flujos CRUD básicos para eventos.
*   **Notificaciones de correo electrónico no enviadas (Bajo-Medio):**
    *   *Impacto:* Los asistentes no reciben correos recordatorios, pero pueden acceder a sus tickets a través de la plataforma si han guardado el enlace.
    *   *Mitigación:* Verificación de la integración de envío de correos utilizando servicios de test (ej. Mailhog o logs de consola).
