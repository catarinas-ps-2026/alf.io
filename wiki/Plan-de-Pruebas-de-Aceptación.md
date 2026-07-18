# Plan de Pruebas de Aceptación de alf.io

## Índice
- [Información General](#información-general)
- [Especificaciones de las Pruebas](#especificaciones-de-las-pruebas)
- [Comunicación de las Pruebas](#comunicación-de-las-pruebas)
- [Registro de Riesgos](#registro-de-riesgos)
- [Metodología](#metodología)
- [Organización](#organización)
- [Cronograma](#cronograma)
- [Matriz de Trazabilidad](#matriz-de-trazabilidad)

## Información General

### Alcance

Este plan cubre las pruebas de aceptación de alf.io, cuyo objetivo es validar que el sistema cumple con los requisitos funcionales desde la perspectiva del usuario final. A diferencia de las pruebas de integración (que validan interacciones singulares entre componentes) y las pruebas de sistema (que evalúan requisitos no funcionales como rendimiento y seguridad), las pruebas de aceptación verifican **flujos completos de negocio** a través de la interfaz web, simulando el comportamiento real de los usuarios en escenarios de uso cotidianos.
La estrategia de aceptación se basa en **Playwright** como herramienta de automatización de navegador, ejecutando flujos end-to-end que recorren la aplicación desde la página pública de eventos hasta el panel de administración, incluyendo autenticación, creación de eventos, compra de entradas, pagos, check-in y notificaciones por correo. Todos los flujos se ejecutan contra un entorno real de la aplicación (Spring Boot + PostgreSQL + Angular/Lit) desplegado en Docker.

### Referencias

1. Estándares de Ingeniería de Software y Pruebas
   - **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software.
   - **Repositorio de Código Abierto de alf.io:** [GitHub - alfio-event/alf.io](https://github.com/alfio-event/alf.io)
   - **Documentación de Arquitectura de alf.io:** [[Arquitectura]] del proyecto.
   - **Plan de Pruebas Unitarias:** [[Plan-de-Pruebas-Unitarias]] (base de referencia para este documento).
   - **Plan de Pruebas de Integración:** [[Plan-de-Pruebas-de-Integración]] (validación de interacciones singulares).
   - **Plan de Pruebas de Sistema:** [[Plan-de-Pruebas-de-Sistema]] (validación de requisitos no funcionales).
   - **Diseño de Casos de Prueba Funcionales:** [[Diseño-de-Casos-de-Prueba-Funcionales]] (base para la selección de flujos de aceptación).
   - **Documentación oficial de Playwright:** [playwright.dev](https://playwright.dev/)

### Glosario

- **Prueba de Aceptación:** Tipo de prueba que valida que el sistema cumple con los requisitos funcionales desde la perspectiva del usuario final, recorriendo flujos completos de negocio a través de la interfaz.
- **Flujo Completo (E2E):** Secuencia de pasos que simula un escenario real de uso del usuario, desde la interacción inicial hasta el resultado final esperado, traversando múltiples pantallas y componentes del sistema.
- **Playwright:** Herramienta de automatización de navegador de Microsoft que permite pruebas end-to-end en Chromium, Firefox y WebKit, con soporte para auto-waiting, tracing y generación de reportes.
- **Page Object Model (POM):** Patrón de diseño de pruebas que encapsula la estructura y comportamiento de las páginas en clases reutilizables, separando la lógica de las pruebas de los selectores de la interfaz.
- **Happy Path:** Flujo de prueba que recorre el escenario principal de éxito de un caso de uso, sin ramificaciones de error o validaciones de borde.
- **Unhappy Path:** Flujo de prueba que valida comportamientos ante errores, validaciones fallidas o escenarios alternativos dentro de un caso de uso.
- **Check-in Digital:** Proceso de registro de asistencia de un participante al evento mediante la interfaz web, sin requerir escaneo físico de código QR.
- **Pago OFFLINE:** Método de pago por transferencia bancaria que genera instrucciones de pago y una ventana de expiración antes de confirmar la reserva.
- **Pago ON_SITE:** Método de pago en efectivo al llegar al evento, que genera el ticket inmediatamente sin requerir confirmación previa de pago.

---

## Especificaciones de las Pruebas

### Proyecto y Subprocesos de Prueba

alf.io es una plataforma de venta de entradas y gestión de eventos. Para las pruebas de aceptación, el sistema se divide en los siguientes flujos completos de validación:

#### Subproceso 1 – Flujo Completo de Compra de Entradas (Público)
- **Objetivo:** Validar que un usuario anónimo puede navegar la tienda pública de eventos, seleccionar entradas, completar el formulario de compra, elegir método de pago y finalizar la reserva exitosamente. Este flujo cubre el recorrido completo del comprador desde la página de eventos hasta la confirmación de la reserva.
- **Técnica:** Playwright (Chromium) contra la interfaz Angular pública
- **Foco de Validación:**
  - Listado de eventos públicos y selección de evento
  - Selección de categoría de ticket y cantidad
  - Formulario de datos del comprador (nombre, apellido, correo)
  - Selección de método de pago (OFFLINE/ON_SITE)
  - Aceptación de términos y condiciones
  - Confirmación de reserva y visualización de ticket
  - Verificación de estado de reserva en la página de confirmación

#### Subproceso 2 – Flujo Completo de Compra con Pago OFFLINE
- **Objetivo:** Validar el flujo completo de reserva con método de pago OFFLINE: desde la selección del ticket hasta la visualización de instrucciones de transferencia bancaria, incluyendo la verificación de expiración de la reserva y la disponibilidad de los tickets.
- **Técnica:** Playwright (Chromium) contra la interfaz Angular pública
- **Foco de Validación:**
  - Selección de método de pago OFFLINE
  - Visualización de instrucciones de transferencia bancaria
  - Monto a transferir, concepto de pago (ID de reserva), fecha límite
  - Verificación de bloqueo de cupo tras crear reserva
  - Comportamiento de la página de "waiting-payment"

#### Subproceso 3 – Flujo Completo de Compra con Pago ON_SITE
- **Objetivo:** Validar el flujo completo de reserva con método de pago ON_SITE: desde la selección del ticket hasta la generación inmediata del ticket, incluyendo la verificación de que el ticket está disponible sin necesidad de aprobación administrativa.
- **Técnica:** Playwright (Chromium) contra la interfaz Angular pública
- **Foco de Validación:**
  - Selección de método de pago ON_SITE
  - Generación inmediata del ticket
  - Opciones de visualización, descarga y reenvío por correo
  - Verificación de ausencia de fecha de expiración de pago

#### Subproceso 4 – Flujo Completo de Administración de Eventos
- **Objetivo:** Validar que un administrador autenticado puede crear un evento completo (con organización, categorías de tickets, configuración de precios, impuestos, localización y publicación), y que el evento resultante es visible en la tienda pública.
- **Técnica:** Playwright (Chromium y Firefox) contra la interfaz Lit de administración y la tienda pública
- **Foco de Validación:**
  - Login de administrador y autenticación
  - Creación de organización (si aplica)
  - Creación de evento con datos completos
  - Configuración de categorías de tickets (precios, capacidad, códigos de acceso)
  - Configuración de impuestos y localización
  - Publicación del evento
  - Verificación de visibilidad en la tienda pública

#### Subproceso 5 – Flujo Completo de Check-in Digital (Auto-check-in)
- **Objetivo:** Validar que un usuario asistente puede realizar el auto-check-in de su ticket desde la interfaz web, verificando que el estado del ticket cambia correctamente y que la validación QR refleja el check-in completado.
- **Técnica:** Playwright (Chromium) contra la interfaz Angular del usuario
- **Foco de Validación:**
  - Acceso a la página personal del ticket
  - Verificación de condiciones del auto-check-in (habilitado, pagado, a tiempo, sin usar)
  - Ejecución del auto-check-in
  - Cambio de estado del ticket a "Checked-In"
  - Verificación desde el panel de administración

#### Subproceso 6 – Flujo Completo de Gestión de Reservas Administrativas
- **Objetivo:** Validar que un administrador puede gestionar las reservas del evento: buscar reservas, visualizar detalles, confirmar pagos pendientes, cancelar reservas y verificar los cambios de estado en la interfaz administrativa.
- **Técnica:** Playwright (Chromium) contra la interfaz Lit de administración
- **Foco de Validación:**
  - Búsqueda de reservas por ID y apellido
  - Visualización de detalle de reserva
  - Confirmación de pago OFFLINE pendiente
  - Cancelación de reserva
  - Verificación de cambio de estados (PENDING → COMPLETE / CANCELLED)
  - Verificación de liberación de cupo tras cancelación

#### Subproceso 7 – Flujo Completo de Autenticación y Control de Acceso
- **Objetivo:** Validar los flujos de autenticación del sistema: login, logout, control de sesión, recuperación de contraseña y comportamiento ante credenciales inválidas. Incluye verificación de que los roles de usuario restringen el acceso a funcionalidades no autorizadas.
- **Técnica:** Playwright (Chromium) contra la interfaz Lit de administración
- **Foco de Validación:**
  - Login exitoso con credenciales válidas
  - Rechazo con credenciales inválidas
  - Logout y destrucción de sesión
  - Expiración de sesión por inactividad
  - Control de acceso por roles (admin global vs. organizador vs. operador de check-in)
  - Navegación restringida a usuarios no autenticados

#### Subproceso 8 – Flujo Completo de Notificación por Correo Electrónico
- **Objetivo:** Verificar que el sistema envía correos electrónicos de notificación tras eventos críticos (reserva creada, pago confirmado, check-in realizado) y que el contenido del correo es correcto y contiene la información relevante.
- **Técnica:** Playwright (Chromium) + verificación de cola SMTP real
- **Foco de Validación:**
  - Envío de correo tras creación de reserva OFFLINE
  - Envío de correo tras confirmación de pago
  - Envío de correo tras check-in
  - Contenido del correo: datos del evento, datos del ticket, enlace al ticket
  - Verificación de destinatario y asunto

#### Subproceso 9 – Flujo Completo de Configuración del Sistema
- **Objetivo:** Validar que un administrador global puede configurar las propiedades del sistema: idiomas, monedas, zonas horarias, y que los cambios se reflejan correctamente en la interfaz pública.
- **Técnica:** Playwright (Chromium) contra la interfaz Lit de administración
- **Foco de Validación:**
  - Configuración de idiomas del sistema
  - Configuración de monedas (EUR, PEN, USD)
  - Configuración de zonas horarias
  - Verificación de cambios en la tienda pública
  - Validación de límites (idioma mínimo, moneda por defecto)

#### Subproceso 10 – Flujo Completo de Gestión de Capacidad y Agotamiento
- **Objetivo:** Validar el comportamiento del sistema cuando se alcanza la capacidad máxima de una categoría de tickets: bloqueo de venta, visualización de "Sold out", y comportamiento al liberar cupos por cancelación.
- **Técnica:** Playwright (Chromium) contra la interfaz Angular pública y Lit de administración
- **Foco de Validación:**
  - Compra del último ticket disponible
  - Visualización de "Sold out" en la tienda pública
  - Intento de compra superando capacidad (error)
  - Cancelación de reserva y liberación de cupo
  - Re-aparición de la categoría en la tienda


### Elementos de Prueba

- **Frontend público (Angular 17):** Páginas de listado de eventos, detalle de eventos, formulario de compra, checkout, confirmación y visualización de tickets.
- **Frontend de administración (Lit 3 + Shoelace + Vite):** Panel de administración para autenticación, gestión de eventos, categorías de tickets, reservas, usuarios, organizaciones y configuración del sistema.
- **Backend (Java 17, Spring Boot 3.5.x, Jetty):** Endpoints REST API públicos y de administración, lógica de negocio, persistencia de datos.
- **Base de datos (PostgreSQL 15/16):** Persistencia de eventos, tickets, reservas, usuarios y configuración del sistema.
- **Infraestructura de pruebas:** Docker Compose para despliegue del entorno completo, GitHub Actions para ejecución automatizada.

### Alcance de la Prueba

#### Elementos Incluidos

- Pruebas de flujo completo de compra de entradas (selección → formulario → pago → confirmación → ticket).
- Pruebas de flujo completo de administración de eventos (creación → configuración → publicación).
- Pruebas de flujo completo de autenticación (login → sesión → logout → control de acceso).
- Pruebas de flujo completo de check-in digital (auto-check-in y verificación QR).
- Pruebas de flujo completo de gestión de reservas (búsqueda → detalle → acción administrativa).
- Pruebas de flujo completo de notificación por correo (reserva → pago → check-in).
- Pruebas de flujo completo de configuración del sistema (idiomas, monedas, zonas horarias).
- Pruebas de flujo completo de capacidad y agotamiento (compra → sold out → liberación).
- Cobertura multi-navegador: Chromium (principal) y Firefox (secundario).
- Generación de reportes HTML con traces y screenshots por cada ejecución.

#### Elementos Excluidos

- **Pruebas de integración de componentes:** Las interacciones singulares (login aislado, creación de un campo, envío de un formulario) se validan en el [[Plan-de-Pruebas-de-Integración]].
- **Pruebas de rendimiento y carga:** El comportamiento bajo alta concurrencia se evalúa en el [[Plan-de-Pruebas-de-Sistema]] con K6.
- **Pruebas de fuzzing y seguridad estática:** La detección de vulnerabilidades y crashes se realiza en el [[Plan-de-Pruebas-de-Sistema]] con Jazzer y SonarQube.
- **Pruebas de accesibilidad (a11y):** Auditoría WCAG no está dentro del alcance de este plan.
- **Pruebas de internacionalización completa:** Solo se validan los idiomas configurados en el entorno de pruebas.
- **Pruebas con pasarelas de pago reales:** Todos los pagos se procesan en modo sandbox o con métodos OFFLINE/ON_SITE.

### Suposiciones y Restricciones

**Suposiciones**

- Existe un entorno Docker disponible para desplegar la aplicación completa (backend + frontend + base de datos).
- Las pruebas unitarias, de integración y de sistema ya están aprobadas antes de iniciar las pruebas de aceptación.
- El esquema de base de datos se crea exclusivamente vía Flyway (sin scripts manuales).
- Existe una cuenta de administrador global pre-configurada en el entorno de pruebas.
- Los navegadores Chromium y Firefox están instalados vía Playwright CLI.

**Restricciones**

- Las suites de aceptación deben completarse en menos de 20 minutos en el pipeline de CI.
- Las pruebas deben ser idempotentes: la ejecución repetida no debe dejar estados residuales.
- Cada test debe limpiar su estado al finalizar (eliminación de eventos, usuarios y reservas creados durante la prueba).
- Los datos de prueba se generan dinámicamente con slugs aleatorios para evitar colisiones.
- Las pruebas se ejecutan en horarios que no afecten el desarrollo del equipo.

### Partes Interesadas

| Rol | Responsabilidades |
| :--- | :--- |
| Docente a cargo | Aprobación de criterios de aceptación académicos, validación del plan, supervisión general. |
| Test Lead | Coordinación del equipo, definición de la estrategia de aceptación, revisión de código y entregables. |
| Desarrolladores | Implementación de pruebas de aceptación, documentación de resultados, reporte de defectos. |

---

## Comunicación de las Pruebas

Se mantiene el mismo esquema de comunicación definido en el [[Plan-de-Pruebas-Unitarias]]:

- **Comunicación Interna:** WhatsApp (daily), Google Meet (planning/review/retrospective), GitHub Projects (seguimiento de tareas).
- **Comunicación Externa:** GitHub Wiki y Pull Requests para revisión del docente.
- **Resolución de Conflictos:** Gestionada por el Tech Lead; se escala al docente si no hay resolución.

| Punto de Comunicación | Propósito | Frecuencia | Responsable |
| :--- | :--- | :--- | :--- |
| Sprint Planning | Planificar pruebas de aceptación del sprint | Inicio de sprint | Tech Lead |
| Sprint Review | Demostrar pruebas y resultados al docente | Fin de sprint | Tech Lead |
| Reporte de defectos | Reportar fallos de aceptación encontrados | Al encontrarse | Desarrollador |

### Participantes del Equipo

0. Robert Edison Arisaca Mamani (Docente del curso)
1. Mestas Zegarra, Christian Raúl (Tech Lead)
2. Sequeiros Condori, Luis Gustavo (Desarrollador)
3. Jara Mamani, Mariel Alisson (Desarrollador)
4. Fernández Huarca, Rodrigo Alexander (Desarrollador)
5. Quispe Condori, Álvaro Raúl (Desarrollador)
6. Barrios Medina, Mathías Alonso (Desarrollador)

---

## Registro de Riesgos

La severidad se calcula como: **Probabilidad (1–5) × Impacto (1–5)**.

| N° | Riesgo | Prob. | Impacto | Severidad | Plan de Mitigación |
| :--- | :--- | :---: | :---: | :---: | :--- |
| 1 | Lentitud en el pipeline de CI por pruebas de aceptación pesadas | 4 | 3 | 12 | Paralelizar suites por flujos; usar caché de navegador en GitHub Actions; ejecutar solo en Chromium para el pipeline principal y Firefox como job secundario. |
| 2 | Flujos de prueba frágiles por cambios en selectores CSS/HTML del frontend | 4 | 4 | 16 | Uso de selectores robustos (data-testid), abstracción de interfaz mediante Page Object Model, y actualización de selectores en un solo lugar cuando el frontend cambia. |
| 3 | Flaky tests por tiempos de carga variables del frontend Angular | 3 | 3 | 9 | Uso del auto-waiting de Playwright, waitUntil: 'networkidle' para elementos dinámicos, y retry en assertions no deterministas. |
| 4 | Entorno de pruebas inestable o no disponible | 3 | 5 | 15 | Contenedor Docker autocontenido con datos semilla; verificación del entorno antes de cada ejecución; health checks en docker-compose. |
| 5 | Falta de experiencia del equipo con Playwright | 3 | 3 | 9 | Sesiones de capacitación inicial, documentación de referencia, pair programming, y plantillas de prueba reutilizables. |
| 6 | Datos de prueba que dejan estados residuales entre ejecuciones | 3 | 4 | 12 | Generación dinámica de datos con slugs aleatorios, limpieza automática post-ejecución via API helper, y hooks afterEach/afterAll en Playwright. |
| 7 | Incompatibilidades entre versiones de navegadores y la aplicación | 2 | 3 | 6 | Fijar versiones de navegadores Playwright; ejecutar pruebas en Chromium (estable) y Firefox (compatibilidad); no probar en WebKit (solo Chromium/Firefox). |
| 8 | Correos electrónicos no enviados o no verificados en el flujo de notificación | 3 | 3 | 9 | Usar servidor SMTP local (Mailhog/Mailpit) para capturar correos sin enviar realmente; verificar contenido en la cola de mensajes. |

---

## Metodología

### Estrategia de Aceptación

Se adopta una estrategia de **pruebas de flujo completo (E2E)** con Playwright, donde cada subproceso representa un escenario real de uso del usuario que recorre múltiples pantallas y componentes del sistema. La estrategia se estructura en las siguientes fases:

| Fase | Descripción | Subprocesos Involucrados |
| :--- | :--- | :--- |
| **Fase 1 – Flujos Públicos de Compra** | Validación de los flujos completos de compra de entradas desde la tienda pública. | Subprocesos 1, 2, 3 |
| **Fase 2 – Flujos de Administración** | Validación de la creación, configuración y publicación de eventos desde el panel admin. | Subprocesos 4, 6, 9 |
| **Fase 3 – Flujos de Check-in y Validación** | Validación del check-in digital y la verificación de tickets. | Subproceso 5 |
| **Fase 4 – Flujos de Notificación** | Validación del envío de correos electrónicos tras eventos críticos. | Subproceso 8 |
| **Fase 5 – Flujos de Capacidad** | Validación del comportamiento ante límites de capacidad y agotamiento. | Subproceso 10 |
| **Fase 6 – Flujos de Autenticación** | Validación de login, logout, sesión y control de acceso por roles. | Subproceso 7 |

Las fases son **secuenciales**: cada fase solo comienza cuando la anterior ha sido aprobada.

### Entregables de Prueba

- **Scripts de Pruebas de Aceptación (Playwright):** Conjunto de flujos E2E organizados por subproceso, con Page Object Model y configuración multi-navegador.
- **Reporte de Ejecución de Pruebas de Aceptación:** Resultados generados por Playwright con detalle por flujo, incluyendo traces, screenshots y videos de fallos.
- **Matriz de Trazabilidad de Aceptación:** Documento que vincula cada flujo de aceptación con los requisitos funcionales del sistema.
- **Registro de Defectos de Aceptación:** Lista de bugs encontrados durante la ejecución, con severidad, estado y responsable de corrección.
- **Configuración de Playwright en CI:** Workflow de GitHub Actions para ejecución automatizada de pruebas de aceptación.

### Técnicas de Diseño de Prueba

- **Pruebas de Flujo Completo (E2E):** Se diseñan pruebas que recorren el flujo completo de un caso de uso real desde la interfaz de usuario, simulando las acciones exactas que un usuario real realizaría. Ejemplo: navegar a eventos → seleccionar evento → elegir ticket → llenar formulario → pagar → confirmar.
- **Page Object Model (POM):** Cada página o componente de la interfaz se modela como una clase que encapsula los selectores y las acciones disponibles. Esto separa la lógica de negocio de las pruebas de los detalles de implementación de la UI.
- **Pruebas Happy Path y Unhappy Path:** Cada subproceso incluye al menos un flujo Happy Path (escenario de éxito principal) y uno o más flujos Unhappy Path (escenarios de error, validación fallida o alternativos).
- **Pruebas Multi-Navegador:** Los flujos críticos se ejecutan en al menos dos navegadores (Chromium y Firefox) para validar la compatibilidad cross-browser de la interfaz.
- **Datos de Prueba Dinámicos:** Los datos de prueba (nombres de eventos, usuarios, organizaciones) se generan dinámicamente con slugs aleatorios para evitar colisiones y permitir la ejecución paralela.

### Criterios de Finalización

0. **Todos los flujos aprobados:** Los 10 subprocesos pasan al 100% en el pipeline de CI.
1. **Sin defectos críticos abiertos:** No existen bugs de aceptación con severidad ≥ 12 sin resolver.
2. **Entregables completos:** El reporte de ejecución, la matriz de trazabilidad y los scripts están publicados en la Wiki.
3. **Aprobación del Tech Lead:** Cada suite debe estar revisada y aprobada mediante Pull Request por Christian Mestas.
4. **Pipeline verde:** El workflow de GitHub Actions finaliza sin errores en todas las suites de aceptación.

### Métricas

| Métrica | Descripción | Objetivo |
| :--- | :--- | :--- |
| Cobertura de Flujos | % de flujos de aceptación implementados y ejecutados | 100% de los 10 subprocesos cubiertos |
| Tasa de Éxito | % de pruebas de aceptación que pasan en el pipeline de CI | 100% en rama main |
| Tiempo de Ejecución | Tiempo total de ejecución de todas las suites de aceptación | ≤ 20 minutos |
| Flaky Tests | Número de pruebas con resultados inconsistentes | 0 en main |
| Cobertura Multi-Navegador | % de flujos críticos ejecutados en Chromium y Firefox | ≥ 80% de flujos críticos |

### Requisitos del Entorno de Pruebas

#### Infraestructura de CI/CD

GitHub Actions con runners ubuntu-latest. Disparadores: Pull Request hacia main y push a main.

#### Requisitos de Software

- Java JDK 17 (distribución Temurin)
- Gradle (construcción del backend)
- Docker y Docker Compose (despliegue del entorno completo)
- Node.js 22 + pnpm 11.1.2 (para Playwright)
- Playwright (navegadores Chromium y Firefox)

#### Base de Datos y Servicios

**Base de datos:**
- PostgreSQL 15 en contenedor Docker (entorno de pruebas)
- Flyway para migraciones automáticas del esquema

**Servicios:**
- Backend Spring Boot (puerto 8080)
- Frontend Angular (puerto 4200)
- Frontend Lit admin (puerto 8080/admin)
- Servidor SMTP local (Mailhog/Mailpit, puerto 1025)

---

## Organización

### Roles y Responsabilidades

Matriz RACI para las actividades de pruebas de aceptación:

| Actividad Clave | Christian Mestas (Lead) | Mariel Jara | Gustavo Sequeiros | Mathias Barrios | Rodrigo Fernandez | Alvaro Quispe | Docente |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. Definición del Plan de Aceptación** | **A** | **R** | **R** | **C** | **C** | **C** | **I** |
| **2. Configuración de Playwright y entorno CI** | **A** | **R** | **C** | **C** | **C** | **C** | **I** |
| **3. Fase 1 – Flujos Públicos de Compra** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **4. Fase 2 – Flujos de Administración** | **A** | **R** | **R** | **C** | **R** | **R** | **I** |
| **5. Fase 3 – Flujos de Check-in** | **A** | **C** | **R** | **C** | **C** | **R** | **I** |
| **6. Fase 4 – Flujos de Notificación** | **A** | **C** | **R** | **C** | **C** | **C** | **I** |
| **7. Fase 5 – Flujos de Capacidad** | **A** | **R** | **C** | **R** | **C** | **C** | **I** |
| **8. Fase 6 – Flujos de Autenticación** | **A** | **R** | **C** | **C** | **C** | **C** | **I** |
| **9. Consolidación de Reportes y Matriz de Trazabilidad** | **R** | **C** | **C** | **C** | **C** | **C** | **I** |
| **10. Revisión y Cierre del Plan** | **A** | **C** | **C** | **C** | **C** | **C** | **I** |

---

## Cronograma

El cronograma de las pruebas de aceptación se extiende durante 1 sprint de 2 semanas, en continuidad con los cronogramas de los planes de pruebas unitarias, integración y sistema. Las fases se ejecutan de manera secuenciales.

| Semana | Actividad | Entregable |
| :---: | :--- | :--- |
| **1** | Configuración de Playwright y Page Object Model | Entorno funcional con POM base |
| **1** | Implementación de Flujos Públicos de Compra (Subprocesos 1, 2, 3) | Scripts de compra E2E |
| **1** | Implementación de Flujos de Administración (Subprocesos 4, 6, 9) | Scripts de administración E2E |
| **2** | Implementación de Flujos de Check-in (Subproceso 5) | Scripts de check-in E2E |
| **2** | Implementación de Flujos de Notificación (Subproceso 8) | Scripts de notificación E2E |
| **2** | Implementación de Flujos de Capacidad y Autenticación (Subprocesos 7, 10) | Scripts de capacidad y auth E2E |
| **2** | Integración en pipeline de CI y consolidación de reportes | Reportes y workflow GitHub Actions |
| **2** | Revisión final, corrección de defectos críticos y cierre del plan | Plan cerrado y aprobado |

---

## Matriz de Trazabilidad

La matriz de trazabilidad vincula los requisitos funcionales del sistema con los flujos de aceptación que los verifican:

| Requisito Funcional | Flujos de Aceptación Asociados |
| :--- | :--- |
| **REQ-FUNC-01 - Creación y configuración de eventos** | Flujo 4 (Administración de Eventos), Flujo 9 (Configuración del Sistema) |
| **REQ-FUNC-02 - Proceso de reserva y compra de tickets** | Flujo 1 (Compra Completa), Flujo 2 (Pago OFFLINE), Flujo 3 (Pago ON_SITE) |
| **REQ-FUNC-03 - Autenticación y autorización** | Flujo 7 (Autenticación y Control de Acceso) |
| **REQ-FUNC-04 - Integración de pagos** | Flujo 2 (Pago OFFLINE), Flujo 3 (Pago ON_SITE), Flujo 6 (Gestión de Reservas) |
| **REQ-FUNC-05 - Check-in y validación de tickets** | Flujo 5 (Check-in Digital) |
| **REQ-FUNC-06 - Gestión de reservas administrativas** | Flujo 6 (Gestión de Reservas) |
| **REQ-FUNC-07 - Notificaciones por correo** | Flujo 8 (Notificación por Correo) |
| **REQ-FUNC-08 - Capacidad y disponibilidad** | Flujo 10 (Capacidad y Agotamiento) |

---

> [!NOTE]
> Este documento se actualizará conforme avance la ejecución de las pruebas. Los resultados parciales y los defectos encontrados se registrarán en los issues de GitHub correspondientes.
