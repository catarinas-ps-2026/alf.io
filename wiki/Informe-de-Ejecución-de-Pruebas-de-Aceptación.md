# Informe de Ejecución de Pruebas de Aceptación del Sistema alf.io

## Índice

- [1. Introducción](#1-introducción)
- [2. Propósito](#2-propósito)
- [3. Alcance](#3-alcance)
- [4. Referencias](#4-referencias)
- [5. Entorno de pruebas](#5-entorno-de-pruebas)
- [6. Resultados de Ejecución – Flujos E2E (Playwright)](#6-resultados-de-ejecución--flujos-e2e-playwright)
- [7. Cumplimiento de criterios de finalización](#7-cumplimiento-de-criterios-de-finalización)
- [8. Conclusión](#8-conclusión)

## 1. Introducción

El presente informe documenta la ejecución y resultados de las pruebas de aceptación realizadas sobre alf.io, un sistema de gestión y venta de entradas para eventos de código abierto. Se presentan los resultados de siete flujos end-to-end ejecutados con Playwright contra la aplicación completa (Spring Boot + PostgreSQL + Angular/Lit) desplegada en Docker, validando el cumplimiento de los requisitos funcionales desde la perspectiva del usuario final.


## 2. Propósito

Este documento sirve como referencia para:

- Presentar los resultados cuantitativos de la ejecución de pruebas de aceptación por flujo E2E.
- Documentar la configuración utilizada para ejecutar las pruebas de manera reproducible.
- Validar el cumplimiento de los criterios de finalización establecidos en el [[Plan-de-Pruebas-de-Aceptación]].
- Registrar los defectos encontrados durante la ejecución de flujos completos de usuario.


## 3. Alcance

Las pruebas de aceptación cubren los siguientes flujos completos de alf.io:

- **Flujo de compra pública (4 flujos):** Navegación de la tienda pública, selección de entradas, formulario de compra, selección de método de pago (OFFLINE/ON_SITE), confirmación de reserva y visualización de tickets.
- **Flujo de administración (3 flujos):** Login de administrador, creación y configuración de eventos, gestión de reservas, búsqueda y acciones administrativas (confirmación/cancelación de pagos).

Los elementos excluidos del alcance son: pruebas de integración de componentes (cubiertas por el [[Plan-de-Pruebas-de-Integración]]), pruebas de rendimiento y carga (cubiertas por el [[Plan-de-Pruebas-de-Sistema]]), pruebas de accesibilidad (a11y) y pruebas con pasarelas de pago reales.


## 4. Referencias

- **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software.
- **Documentación oficial:** [https://alf.io](https://alf.io)
- **Repositorio oficial:** [https://github.com/alfio-event/alf.io](https://github.com/alfio-event/alf.io)
- **Plan de Pruebas de Aceptación:** [[Plan-de-Pruebas-de-Aceptación]]
- **Plan de Pruebas de Sistema:** [[Plan-de-Pruebas-de-Sistema]]
- **Plan de Pruebas de Integración:** [[Plan-de-Pruebas-de-Integración]]
- **Plan de Pruebas Unitarias:** [[Plan-de-Pruebas-Unitarias]]


## 5. Entorno de pruebas

Las pruebas se ejecutan de manera remota mediante GitHub Actions, que ejecuta la suite de aceptación en cada Pull Request y en cada push a la rama `main`. Los reportes de Playwright se publican automáticamente en GitHub Pages para su revisión.

### 5.1 Configuración del entorno

| Componente | Entorno |
| :--- | :--- |
| Playwright (E2E) | GitHub Actions, ubuntu-latest, Node.js 22, Chromium + Firefox |
| Backend | Spring Boot (puerto 8080) via `./gradlew -Pprofile=dev :bootRun` |
| Frontend público | Angular 17 (puerto 4200) |
| Frontend admin | Lit 3 + Shoelace + Vite (puerto 8080/admin) |
| Base de datos | PostgreSQL 16 (servicio GitHub Actions) |
| SMTP | Servidor SMTP local (Mailpit) para captura de correos |
### 5.2 Reportes Playwright

Los reportes HTML de Playwright con traces, screenshots y videos están disponibles en:

**[Reporte Playwright – Pruebas de Aceptación](https://catarinas-ps-2026.github.io/alf.io/reports/playwright/index.html)**

Este reporte se genera automáticamente en cada ejecución del pipeline de CI y contiene el detalle completo de cada test ejecutado, incluyendo:
- Estado de cada test (passed/failed/flaky)
- Screenshots en caso de fallo
- Trace viewer para depuración interactiva
- Videos de los flujos fallidos si los hubiera
- Tiempos de ejecución por test y navegador

## 6. Resultados de Ejecución – Flujos E2E (Playwright)

### 6.1 Resumen General

| Métrica | Valor |
| :--- | :---: |
| Total de tests de aceptación | 22 |
| Tests de administración | 10 |
| Tests de otros flujos | 12 |
| Tests exitosos | 22 |
| Tests fallidos | 0 |
| Tests flaky | 0 |
| Tasa de éxito | 100% |
| Navegador principal | Chromium |
| Duración total | 2m 21s |

### 6.2 Detalle por Flujo

#### CPA-01 – Compra de Entrada con Pago OFFLINE (Happy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo Público |
| **Descripción** | Valida el flujo completo de compra de una entrada desde la tienda pública con método de pago OFFLINE (transferencia bancaria): navegación del evento, selección de ticket, llenado de formulario de datos del comprador, selección de método de pago, aceptación de términos, confirmación de reserva y verificación de página de instrucciones de pago. |
| **Precondiciones** | Evento publicado con categoría de tickets disponible. Método de pago OFFLINE habilitado en la configuración del evento. |
| **Resultado esperado** | La reserva se crea en estado PENDING. Se muestra la página de instrucciones de pago con el monto, concepto y fecha límite de expiración. |
| **Resultado obtenido** | Flujo completado exitosamente. La reserva se crea correctamente en estado PENDING y la página de instrucciones de pago muestra toda la información requerida. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 12.4s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Navegar a la página pública del evento
2. Seleccionar la categoría de ticket y cantidad (1 entrada)
3. Completar el formulario de datos del comprador (nombre, apellido, correo)
4. Seleccionar método de pago OFFLINE (Transferencia bancaria)
5. Aceptar términos y condiciones
6. Hacer clic en "Pagar PEN X.XX"
7. Verificar redirección a página de instrucciones de pago
8. Verificar monto, concepto de pago (ID de reserva) y fecha límite

**Evidencia:**

Flujo completo de compra OFFLINE: selección de ticket → formulario → método de pago → confirmación → instrucciones de pago.

[CPA-01 - Compra OFFLINE exitosa](images/acceptance-tests/other/cpa-01.webm)

---

#### CPA-02 – Compra de Entrada con Pago ON_SITE (Happy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo Público |
| **Descripción** | Valida el flujo completo de compra de una entrada desde la tienda pública con método de pago ON_SITE (efectivo al llegar): navegación del evento, selección de ticket, llenado de formulario, selección de método de pago, aceptación de términos, confirmación de reserva y verificación de generación inmediata del ticket. |
| **Precondiciones** | Evento publicado con categoría de tickets disponible. Método de pago ON_SITE habilitado en la configuración del evento. |
| **Resultado esperado** | La reserva se crea en estado ACQUIRED. El ticket está disponible inmediatamente sin necesidad de aprobación administrativa. No se muestra fecha de expiración de pago. |
| **Resultado obtenido** | Flujo completado exitosamente. El ticket se genera inmediatamente tras la confirmación, mostrando las opciones de visualización y descarga. No se presenta fecha de expiración. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 10.8s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Navegar a la página pública del evento
2. Seleccionar la categoría de ticket y cantidad (1 entrada)
3. Completar el formulario de datos del comprador (nombre, apellido, correo)
4. Seleccionar método de pago ON_SITE (Pago en efectivo al llegar)
5. Aceptar términos y condiciones
6. Hacer clic en "Confirmar"
7. Verificar redirección a página de éxito
8. Verificar que el ticket está disponible inmediatamente (opciones Ver, Descargar)
9. Verificar ausencia de fecha de expiración de pago

**Evidencia:**

Flujo completo de compra ON_SITE: selección de ticket → formulario → método de pago → confirmación → ticket inmediato.


---

#### CPA-03 – Validación de Selección de Entradas (Unhappy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo Público |
| **Descripción** | Valida el comportamiento del sistema cuando el usuario intenta avanzar en el flujo de compra sin seleccionar ninguna entrada (0 tickets), y cuando la categoría de tickets está agotada (sold out). Verifica que el sistema bloquea el avance y muestra los mensajes de error correspondientes. |
| **Precondiciones** | Evento publicado con categoría de tickets disponible y otra categoría agotada (0 disponibles). |
| **Resultado esperado** | Con 0 entradas seleccionadas, el sistema no permite avanzar y muestra un mensaje de error. La categoría agotada muestra la etiqueta "Sold out" y el dropdown de cantidad está deshabilitado o solo permite 0. |
| **Resultado obtenido** | El sistema bloquea correctamente el avance con 0 entradas y muestra la etiqueta "Sold out" en categorías agotadas. El comportamiento es el esperado. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 8.2s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Navegar a la página pública del evento
2. Seleccionar 0 entradas en una categoría
3. Intentar continuar al paso siguiente
4. Verificar que el sistema bloquea el avance
5. Navegar a una categoría agotada (sold out)
6. Verificar que la categoría muestra la etiqueta "Sold out"
7. Verificar que no se puede seleccionar cantidad > 0

**Evidencia:**

Validación de selección de entradas: 0 entradas → error, sold out → etiqueta visible.


---

#### CPA-04 – Verificación de Descarga de Ticket (Happy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo Público |
| **Descripción** | Valida que un usuario que completó una compra con pago ON_SITE puede visualizar y descargar su ticket en PDF desde la página de confirmación. Verifica que el botón de descarga es visible y funcional, y que el PDF contiene la información correcta del ticket. |
| **Precondiciones** | Reserva completada con pago ON_SITE. Ticket generado y disponible en la página de confirmación. |
| **Resultado esperado** | El botón de descarga es visible y funcional. El PDF se descarga correctamente conteniendo la información del ticket (titular, tipo, número de referencia, datos del evento). |
| **Resultado obtenido** | Flujo completado exitosamente. El ticket se visualiza correctamente y el PDF se descarga con toda la información del asistente y del evento. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 15.1s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Completar una compra con método de pago ON_SITE (flujo CPA-02)
2. En la página de éxito, hacer clic en "Ver" (ticket)
3. Verificar que se muestra la página del ticket con la información correcta
4. Hacer clic en "Descargar" (PDF)
5. Verificar que el PDF se descarga correctamente

**Evidencia:**

Verificación de descarga de ticket: compra ON_SITE → ver ticket → descargar PDF.


---

#### CPA-05 – Creación y Publicación de Evento (Happy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo de Administración |
| **Descripción** | Valida el flujo completo de creación de un evento desde el panel de administración: login como administrador, creación de organización, creación de evento con datos completos (nombre, fechas, descripción), configuración de categorías de tickets (precios, capacidad), configuración de impuestos y localización, y publicación del evento. Finalmente verifica que el evento es visible en la tienda pública. |
| **Precondiciones** | Cuenta de administrador global pre-configurada. Base de datos limpia sin eventos previos. |
| **Resultado esperado** | El evento se crea, configura y publica correctamente. Es visible en la tienda pública con los datos correctos (nombre, precio, fechas). |
| **Resultado obtenido** | Flujo completado exitosamente. El evento se publica correctamente y aparece en la tienda pública con toda la información configurada. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 9.5s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Navegar a la página de login del panel de administración
2. Iniciar sesión con credenciales de administrador
3. Crear una nueva organización (nombre, descripción, correo)
4. Crear un nuevo evento (nombre, fechas, descripción)
5. Configurar categoría de tickets (nombre, precio, capacidad)
6. Configurar impuestos (tasa impositiva)
7. Configurar localización (idioma, moneda, zona horaria)
8. Publicar el evento
9. Navegar a la tienda pública
10. Verificar que el evento aparece en el listado público

**Evidencia:**

Flujo completo de administración: login → crear organización → crear evento → configurar tickets → publicar → verificar en tienda.

[CPA-05 - Creación de evento exitosa](images/acceptance-tests/admin/cpa-05.webm)

---

#### CPA-06 – Gestión de Reservas Administrativas (Happy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo de Administración |
| **Descripción** | Valida que un administrador puede gestionar las reservas del evento desde el panel administrativo: buscar reservas por ID y apellido, visualizar detalles de reserva, confirmar un pago OFFLINE pendiente, y verificar que el estado de la reserva cambia de PENDING a COMPLETE tras la confirmación del pago. |
| **Precondiciones** | Evento publicado con al menos una reserva OFFLINE en estado PENDING. Usuario autenticado como administrador. |
| **Resultado esperado** | La búsqueda por ID y apellido funciona correctamente. La confirmación del pago OFFLINE cambia el estado de la reserva a COMPLETE y la elimina de la lista de pagos pendientes. |
| **Resultado obtenido** | Flujo completado exitosamente. La búsqueda funciona para ambos criterios y la confirmación de pago transiciona correctamente el estado de la reserva. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 11.4s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Navegar al panel de administración del evento
2. Ingresar a la sección de reservas
3. Buscar reserva por ID existente
4. Verificar que se muestra la reserva correspondiente
5. Buscar reserva por apellido
6. Verificar que se listan las reservas bajo ese apellido
7. Seleccionar una reserva pendiente
8. Hacer clic en "confirm" para aceptar el pago OFFLINE
9. Completar el modal de confirmación (fecha de recepción, notas)
10. Verificar que el estado cambia a COMPLETE
11. Verificar que la reserva desaparece de Pending Payments

**Evidencia:**

Flujo completo de gestión de reservas: buscar por ID → buscar por apellido → seleccionar pendiente → confirmar pago → verificar cambio de estado.


---

#### CPA-07 – Login, Logout y Control de Acceso por Roles (Unhappy Path)

| Campo | Detalle |
| :--- | :--- |
| **Tipo** | Flujo de Administración |
| **Descripción** | Valida los flujos de autenticación del sistema: login exitoso con credenciales válidas, rechazo con credenciales inválidas, logout y destrucción de sesión, y navegación restringida a usuarios no autenticados. Verifica que el sistema redirige correctamente según el estado de autenticación. |
| **Precondiciones** | Cuenta de administrador global pre-configurada. Credenciales inválidas de prueba definidas. |
| **Resultado esperado** | Credenciales inválidas son rechazadas con mensaje de error. Login exitoso redirige al panel. Logout destruye la sesión. Rutas protegidas redirigen al login sin autenticación. |
| **Resultado obtenido** | Flujo completado exitosamente. El sistema rechaza credenciales inválidas, permite login/logout correctamente y protege las rutas administrativas. |
| **Estado** | **Exitoso** |
| **Tipo de prueba** | Automatizado |
| **Navegador** | Chromium |
| **Duración** | 27.3s |
| **Defectos** | Ninguno |

**Pasos de Ejecución:**

1. Navegar a la página de login del panel de administración
2. Ingresar credenciales inválidas (usuario incorrecto)
3. Verificar que el sistema rechaza el login con mensaje de error
4. Limpiar campos y ingresar credenciales válidas
5. Verificar login exitoso y redirección al panel
6. Hacer clic en "Logout"
7. Verificar que la sesión se destruye y se redirige al login
8. Intentar navegar directamente a una ruta protegida sin autenticación
9. Verificar que el sistema redirige al login

**Evidencia:**

Flujo completo de autenticación: login inválido → rechazo → login válido → logout → ruta protegida → redirección.

[](images/acceptance-tests/admin/cpa-07.webm)

---

## 7. Cumplimiento de criterios de finalización

Conforme a la sección de Criterios de Finalización del [[Plan-de-Pruebas-de-Aceptación]], se validan los siguientes criterios de cierre:

| Criterio | Estado | Evidencia |
| :--- | :---: | :--- |
| Todos los flujos aprobados | Cumplido | Los 22 tests de aceptación pasan al 100% en el pipeline de CI (sección 7) |
| Sin defectos críticos abiertos | Cumplido | 0 fallos en ejecución, 0 defectos registrados |
| Entregables completos | Cumplido | Reporte de ejecución y scripts publicados en la Wiki |
| Pipeline verde | Cumplido | Workflow de GitHub Actions finaliza sin errores |
| Aprobación del Tech Lead | Pendiente | Pendiente de revisión y aprobación vía PR |

## 8. Conclusión

La suite de pruebas de aceptación de alf.io alcanza una tasa de éxito del 100% con un total de 22 tests ejecutados con Playwright: flujos completos de compra pública (OFFLINE, ON_SITE, validación de entradas, descarga de tickets) y flujos de administración (creación de eventos, gestión de reservas, autenticación, ciclos de acceso, vinculación de suscripciones). Todos los tests se ejecutaron en Chromium con un tiempo total de 2 minutos 21 segundos.

Los tests cubren los principales escenarios de uso del sistema: desde la navegación pública de eventos hasta la gestión administrativa completa, incluyendo autenticación, creación de eventos, compra de entradas con diferentes métodos de pago, y gestión de reservas. Los 22 tests pasaron sin defectos, validando que el sistema cumple con los requisitos funcionales desde la perspectiva del usuario final.

Los reportes detallados de Playwright con traces, screenshots y videos están disponibles en [GitHub Pages](https://catarinas-ps-2026.github.io/alf.io/reports/playwright/index.html).

---

> [!NOTE]
> Este documento se actualizará conforme avance la ejecución de las pruebas. Los resultados parciales y los defectos encontrados se registrarán en los issues de GitHub correspondientes.
