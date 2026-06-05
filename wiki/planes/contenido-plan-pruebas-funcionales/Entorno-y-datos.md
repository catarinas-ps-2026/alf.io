# Entorno y Datos de Prueba

**4.1 Entorno de Pruebas**
*   **Servidor / Hosting:** Entorno local utilizando Docker Compose (que levanta la aplicación y los servicios de base de datos) o un servidor de pruebas dedicado (staging environment).
*   **Base de datos:** PostgreSQL en versión compatible con los requerimientos actuales del proyecto (ej. versión 14+), corriendo en un contenedor aislado.
*   **Navegadores soportados:** Pruebas funcionales frontend orientadas a las últimas versiones estables de Google Chrome y Mozilla Firefox.

**4.2 Datos de Prueba**
*   **Cuentas de usuario:** 
    *   Cuenta de *Administrador Global* pre-configurada (datos de las variables de entorno de prueba).
    *   Cuenta de *Organizador de Evento* para pruebas de creación.
*   **Eventos:** Creación de al menos un evento en estado "Publicado" con tickets disponibles, y otro en estado "Borrador".
*   **Métodos de pago:** Uso de tarjetas de crédito de prueba proporcionadas por la pasarela de pagos en modo Sandbox (ej. tarjetas de test de Stripe).
