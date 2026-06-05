# Técnicas de Prueba

**6.1 Técnicas de Caja Negra**
Se utilizarán técnicas formales de diseño de pruebas basadas en los requisitos funcionales del sistema de reservas:
*   **Partición de equivalencia y Valores límite:**
    *   Se aplicará especialmente en los campos de entrada como la cantidad de tickets a comprar (ej: 0, 1, límite máximo de tickets permitidos por transacción, y por encima del límite).
    *   Fechas de inicio y fin de eventos y validez de categorías de tickets.
    *   Precios de tickets y aplicación de códigos de descuento.
*   **Transición de estados:**
    *   Verificación de los diferentes estados de un ticket (reservado, pagado, cancelado, verificado en puerta).

**6.2 Pruebas Exploratorias**
*   Se ejecutarán sesiones de pruebas exploratorias para evaluar la usabilidad de la interfaz de administración del evento y el flujo de compra desde dispositivos móviles, detectando anomalías que las pruebas guiadas por casos no hayan cubierto.
