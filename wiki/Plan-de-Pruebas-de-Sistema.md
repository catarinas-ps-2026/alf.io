# Plan de Pruebas de Sistema de alf.io

## Índice
- [1. Información General](#1-información-general)
- [2. Especificaciones de las Pruebas](#2-especificaciones-de-las-pruebas)
- [3. Comunicación de las Pruebas](#3-comunicación-de-las-pruebas)
- [4. Registro de Riesgos](#4-registro-de-riesgos)
- [5. Metodología](#5-metodología)
- [6. Estructura de Pruebas](#6-estructura-de-pruebas)
- [7. Organización](#7-organización)
- [8. Cronograma](#8-cronograma)

## 1. Información General

### 1.1 Alcance
El plan se centra en las pruebas de sistema para la plataforma de reserva de entradas alf.io. El objetivo principal es evaluar el comportamiento global del sistema integrado de extremo a extremo (E2E), garantizando el cumplimiento tanto de los requisitos funcionales desde la perspectiva del usuario final como de los requisitos no funcionales (rendimiento y estabilidad bajo carga masiva). La estrategia de pruebas de sistema se basa en una estrategia basada en requisitos (Requirement-Based) y Casos de Uso, combinada con una estrategia no funcional dirgida por objetivos.

- *Validación Funcional Web E2E*: Automatización de flujos de interacción del usuario mediante Playwright.
- *Validación de Rendimiento*: Pruebas de carga y estrés concurrentes sobre los servicios del sistema mediante K6.

### 1.2 Referencias
1. Estándares de Ingeniería de Software y Pruebas
   - **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software (conceptos, procesos, documentación y técnicas de diseño de pruebas).
   - **ISTQB Foundation Level Syllabus:** Cuerpo de conocimiento para certificación de testers, base para la definición de técnicas y niveles de prueba.
2. Repositorio de Código Abierto de alf.io: [GitHub - alfio-event/alf.io](https://github.com/alfio-event/alf.io)
3. Documentación de Arquitectura del proyecto: [[Arquitectura]]
4. Planes de pruebas previos:
   - [[Plan de Pruebas Unitarias]] - Pruebas aisladas de componentes.
   - [[Plan de Pruebas de Integración]] - Pruebas de interacción entre módulos.
   - [[Diseño de Casos de Prueba Funcionales]] - Diseño de pruebas funcionales.
5. Herramientas de automatización:
   - Documentación oficial de Playwright: [playwright.dev](https://playwright.dev/)
   - Documentación oficial de K6: [grafana.com/docs/k6](https://grafana.com/docs/k6/)
6. Stack técnico del proyecto:
   - Backend: Java 17, Spring Boot 3.5.x, Jetty, PostgreSQL
   - Frontend público: Angular 17
   - Frontend admin: Lit 3 + Vite (Web Components)

### 1.3 Glosario

- **E2E (End-to-End):** Pruebas que validan flujos completos del sistema desde la interfaz de usuario hasta la persistencia de datos.
- **Playwright:** Framework de automatización de navegadores web para pruebas E2E, soportado por Chromium, Firefox y WebKit.
- **K6:** Herramienta de pruebas de rendimiento y carga escrita en Go, con scripts en JavaScript.
- **SPA (Single Page Application):** Aplicación web que carga una sola página HTML y actualiza dinámicamente el contenido.
- **Check-in:** Proceso de validación y registro de asistencia de un participante al evento mediante escaneo de código QR o entrada manual.
- **Flyway:** Herramienta de migración de bases de datos que gestiona el versionamiento del esquema.
- **Stress Test:** Prueba que lleva el sistema más allá de su carga máxima para identificar puntos de quiebre.
- **RPS (Requests Per Second):** Métrica que indica el número de peticiones por segundo que procesa el sistema.
- VU (Virtual User): Hilo de ejecución concurrente utilizado por K6 que simula el comportamiento continuo de un usuario real en el sistema.

## 2. Especificaciones de las Pruebas
El sistema de venta de entradas se divide en los siguientes subprocesos funcionales y de rendimiento para la cobertura del sistema completo:

### 2.1 Proyecto y Subprocesos de Prueba
Esta sección detalla la estrategia de pruebas de sistema para alf.io, validando la integración completa de los componentes del sistema en un entorno que simula producción.

El proceso de pruebas de sistema se divide en los siguientes subprocesos:

#### Subproceso 1 - Pruebas E2E con Playwright
- **Objetivo:** Validar flujos completos de usuario a través de la interfaz de navegador, verificando la interacción entre frontend y backend.
- **Técnica:** Automatización de navegador con Playwright, ejecución en Chromium y Firefox.
- **Foco de Validación:**
  - Flujo completo de compra de entradas (selección → checkout → pago → confirmación).
  - Gestión de eventos desde el panel de administración.
  - Proceso de check-in de asistentes.
  - Comportamiento en múltiples navegadores y resoluciones.

#### Subproceso 2 - Pruebas de Rendimiento con K6
- **Objetivo:** Evaluar el comportamiento del sistema bajo cargas variables de trabajo, midiendo tiempos de respuesta, throughput y estabilidad.
- **Técnica:** Scripts de pruebas de carga, estrés y resistencia con K6.
- **Foco de Validación:**
  - Tiempos de respuesta en endpoints críticos bajo carga normal.
  - Capacidad de concurrentes simultáneos.
  - Comportamiento bajo estrés extremo (pico de ventas de un evento popular).
  - Degradação graceful del sistema bajo sobrecarga.

#### Subproceso 3 - Pruebas de Compatibilidad Cross-Browser
- **Objetivo:** Garantizar que la aplicación funcione correctamente en los navegadores principales utilizados por los usuarios.
- **Técnica:** Ejecución paralela de suites E2E en Chromium y Firefox mediante la configuración de proyectos de Playwright.
- **Foco de Validación:**
  - Renderizado consistente de formularios y componentes UI.
  - Compatibilidad de JavaScript/TypeScript en diferentes motores de renderizado.
  - Comportamiento de APIs del navegador (localStorage, sessionStorage, Service Workers).

### 2.2 Elementos de Prueba
Para garantizar la repetibilidad y el control absoluto del estado físico del sistema sin afectar ambientes reales, las pruebas de sistema se configuran sobre un entorno local orquestado dentro de contenedores:

- **Backend (Spring Boot):**
  - Controladores REST API públicos y de administración.
  - Endpoints de gestión de eventos, categorías de tickets, reservas y check-in.
  - Endpoints de integración con pasarelas de pago (Stripe, PayPal).
- **Frontend público (Angular):**
  - Páginas de listado de eventos, detalle de eventos.
  - Formulario de compra de entradas y checkout.
  - Página de confirmación y visualización de tickets.
  - Flujo de cola de espera para eventos de alta demanda.
- **Frontend admin (Lit):**
  - Panel de administración de eventos.
  - Gestión de categorías de tickets y configuración de precios.
  - Visualización de asistentes y exportación de datos.
  - Funcionalidad de check-in desde el panel admin.
- **Base de datos (PostgreSQL):**
  - Persistencia de datos de eventos, tickets, reservas y asistentes.
  - Migraciones de esquema con Flyway.
  - Row-Level Security para aislamiento de datos por organización.
- **Infraestructura:**
  - Docker y Docker Compose para despliegue del entorno de pruebas.
  - Configuración de variables de entorno para pruebas.
  - GitHub Actions para ejecución automatizada.

### 2.3 Alcance de la Prueba

#### Elementos Incluidos en el Alcance

**Flujos Críticos de Negocio (E2E):**

| ID | Flujo Crítico | Descripción | Prioridad |
|:---|:---|:---|:---:|
| FC-01 | Flujo completo de compra de entradas | Navegación desde listado de eventos hasta confirmación de compra con pago exitoso | Alta |
| FC-02 | Creación y configuración de eventos | Panel admin crea evento, configura categorías, precios y fechas de venta | Alta |
| FC-03 | Check-in de asistentes | Escaneo de código QR y validación de entrada al evento | Alta |
| FC-04 | Gestión de reservas y cancelaciones | Flujo de reserva con tiempo límite, cancelación y reembolso | Alta |
| FC-05 | Generación de tickets digitales | Emisión de tickets en formato PDF, Apple Wallet y Google Wallet | Media |
| FC-06 | Autenticación y autorización | Login de administrador, control de acceso por roles (admin, organizador, check-in) | Alta |
| FC-07 | Cola de espera | Comportamiento del sistema bajo alta demanda con cola virtual | Media |
| FC-08 | Gestión de descuentos y promociones | Aplicación de códigos de descuento y precios dinámicos | Media |

**Pruebas de Rendimiento:**

| ID | Escenario de Rendimiento | Descripción | Métrica Objetivo |
|:---|:---|:---|:---|
| PR-01 | Carga normal | 50 usuarios concurrentes navegando y comprando entradas | Tiempo respuesta < 2s |
| PR-02 | Pico de ventas | 200 usuarios concurrentes en los primeros 5 minutos de apertura de venta | Throughput > 100 RPS |
| PR-03 | Estrés extremo | 500+ usuarios concurrentes para identificar puntos de quiebre | Degradación gradual, sin caída completa |
| PR-04 | Resistencia | Carga sostenida de 100 usuarios durante 30 minutos | Sin memory leaks, tiempos estables |

**Endpoints de API críticos a probar:**
- `GET /api/v1/public/events` - Listado de eventos públicos.
- `POST /api/v1/event/{eventId}/ticket/{ticketId}/check-in` - Check-in de asistentes.
- `POST /api/v1/admin/event/create` - Creación de eventos.
- `GET /api/v1/admin/event/{eventShortName}/attendees` - Lista de asistentes.
- `POST /api/v1/public/event/{eventId}/ticket/{ticketCategoryId}/reserve` - Reserva de entradas.
- `POST /api/v1/public/event/{eventId}/order/{orderId}/stripe/charge` - Proceso de pago.

#### Elementos Excluidos del Alcance

- **Pruebas de Seguridad Avanzadas (Penetration Testing):** Auditoría activa de vulnerabilidades, inyección SQL, XSS y CSRF quedan fuera de este plan (se abordan en revisiones de código estático y herramientas SAST/DAST separadas).
- **Pruebas de Usabilidad (UX Testing):** Evaluación subjetiva de la experiencia de usuario con usuarios reales.
- **Pruebas de Regresión Completa:** Se priorizan los flujos críticos; la regresión total se cubre con las suites de pruebas unitarias y de integración existentes.
- **Pruebas con Pasarelas de Pago Reales:** Todas las transacciones de pago se simulán en entorno de sandbox o se mockearán para evitar cargos reales.
- **Pruebas de Accesibilidad (a11y):** Auditoría completa de accesibilidad WCAG, aunque se verificarán aspectos básicos de navegación por teclado y lectores de pantalla en flujos críticos.
- **Pruebas de Internacionalización (i18n):** Validación completa de traducciones; se cubrirán solo los idiomas principales (inglés y español).

### 2.4 Suposiciones y Restricciones

**Suposiciones**
- El entorno de pruebas está desplegado y accesible (localmente vía Docker o en un entorno de staging).
- Se dispone de las credenciales de administrador necesarias para ejecutar pruebas E2E que requieran autenticación.
- La base de datos de pruebas está en un estado conocido y reproducible al inicio de cada suite de pruebas.
- El equipo tiene acceso a las herramientas de pruebas: Playwright (v1.59.1) y K6.
- Los navegadores Chromium y Firefox están instalados y configurados para Playwright.

**Restricciones**
- Las pruebas E2E deben completarse en menos de 15 minutos por ejecución completa.
- Las pruebas de rendimiento se ejecutarán en horarios que no afecten el desarrollo del equipo.
- No se realizarán pruebas de sistema contra el entorno de producción.
- Los datos de prueba serán generados dinámicamente y eliminados después de cada ejecución.
- Las pruebas deben ser idempotentes: la ejecución repetida no debe dejar estados residuales.

### 2.5 Partes Interesadas
| Rol | Responsabilidades |
| :--- | :--- |
| Docente a cargo | Aprobación de criterios de aceptación académicos, validación del plan, supervisión general. |
| Test Lead | Coordinación del equipo, definición de la estrategia de integración, revisión de código y entregables. |
| Desarrolladores | Implementación de pruebas de integración, documentación de resultados, reporte de defectos. |

## 3. Comunicación de las Pruebas
Se mantiene el mismo esquema de comunicación definido en el [[Plan-de-Pruebas-Integración]:

- **Comunicación Interna:** WhatsApp (daily), Google Meet (planning/review/retrospective), GitHub Projects (seguimiento de tareas).
- **Comunicación Externa:** GitHub Wiki y Pull Requests para revisión del docente.
- **Resolución de Conflictos:** Gestionada por el Tech Lead; se escala al docente si no hay resolución.

| Punto de Comunicación | Propósito | Frecuencia | Responsable |
| :--- | :--- | :--- | :--- |
| Sprint Planning | Planificar pruebas de integración del sprint | Inicio de sprint | Tech Lead |
| Sprint Review | Demostrar pruebas y resultados al docente | Fin de sprint | Tech Lead |
| Reporte de defectos | Reportar fallos de integración encontrados | Al encontrarse | Desarrollador |

### Participantes del Equipo

1. Robert Edison Arisaca Mamani (Docente del curso)
2. Mestas Zegarra, Christian Raúl (Tech Lead)
3. Sequeiros Condori, Luis Gustavo (Desarrollador)
4. Jara Mamani, Mariel Alisson (Desarrollador)
5. Fernández Huarca, Rodrigo Alexander (Desarrollador)
6. Quispe Condori, Álvaro Raúl (Desarrollador)
7. Barrios Medina, Mathías Alonso (Desarrollador)

## 4. Registro de Riesgos

En esta sección se identifican los riesgos que afectan directamente al proceso de ejecución de las pruebas de sistema. La severidad se calcula como: **Probabilidad (1-5) * Impacto (1-5)**.

| N° | Riesgo | Probabilidad | Impacto | Severidad | Plan de Mitigación |
|:---|:---|:---:|:---:|:---:|:---|
| 1 | Entorno de pruebas inestable o no disponible | 3 | 5 | 15 | Contenedor Docker autocontenido con datos de prueba semilla. Verificación del entorno antes de cada ejecución. |
| 2 | Flujos E2E frágiles por cambios en selectores CSS/HTML | 4 | 3 | 12 | Uso de selectores robustos (data-testid), Page Object Model para abstraer la interfaz. |
| 3 | Tiempo de ejecución de pruebas E2E excesivo | 3 | 3 | 9 | Paralelización con `fullyParallel: true`, ejecución selectiva por tags, optimización de wait times. |
| 4 | Resultados inconsistentes en pruebas de rendimiento (flaky) | 3 | 4 | 12 | Múltiples iteraciones, warm-up previo, aislamiento de red, ejecución en horarios de baja actividad. |
| 5 | Falta de experiencia del equipo con Playwright/K6 | 3 | 3 | 9 | Sesiones de capacitación inicial, documentación de referencia, pair programming. |
| 6 | Datos de prueba que dejan estados residuales | 2 | 4 | 8 | Generación dinámica de datos con slugs aleatorios, limpieza automática post-ejecución via API helper. |
| 7 | Dificultades con la integración de pago en sandbox | 2 | 3 | 6 | Mock de respuestas de pago, uso de entorno sandbox de Stripe/PayPal con credenciales de prueba. |

## 5. Metodología

### 5.1 Entregables de Prueba   

Para el proceso de pruebas de sistema de `alf.io`, se generarán los siguientes artefactos como evidencia del cumplimiento de los objetivos de calidad:

- **Scripts de Pruebas E2E (Playwright):** Suite completa de pruebas automatizadas de extremo a extremo cubriendo los flujos críticos de negocio.
- **Scripts de Pruebas de Rendimiento (K6):** Conjunto de scripts para pruebas de carga, estrés y resistencia con métricas documentadas.
- **Reporte de Ejecución de Pruebas E2E:** Resultados detallados de la ejecución de pruebas en Chromium y Firefox, incluyendo capturas de pantalla en caso de fallos.
- **Reporte de Pruebas de Rendimiento:** Análisis de métricas de rendimiento (tiempos de respuesta, throughput, errores) con gráficas y comparativas.
- **Matriz de Trazabilidad:** Documento que vincula los casos de prueba de sistema con los requisitos funcionales del sistema.
- **Lista de Defectos Encontrados:** Registro de todos los bugs identificados durante la ejecución de las pruebas, con severidad, prioridad y estado.

Los entregables se encuentran en la sección correspondiente de esta Wiki.

### 5.2 Técnicas de Diseño de Prueba

#### Pruebas E2E (Playwright)

- **Pruebas de Flujo de Usuario (Happy Path):** Se ejecutan los flujos principales del sistema desde la perspectiva del usuario final:
  - Navegación → Selección → Checkout → Pago → Confirmación.
  - Login → Creación de evento → Configuración → Publicación.

- **Partición por Equivalencia:** Los datos de entrada se agrupan en clases válidas e inválidas:
  - Categorías de tickets con diferentes precios y estados (activa, inactiva, agotada).
  - Tipos de usuario con diferentes permisos (admin, organizador, check-in operator).

- **Análisis de Valores Límite:**
  - Campos de formulario con límites de caracteres.
  - Fechas límite de venta de tickets (venta cerrada vs. venta activa).
  - Stock mínimo de tickets (último ticket disponible vs. agotado).

- **Pruebas de Compatibilidad Cross-Browser:** Ejecución en Chromium y Firefox para validar diferencias de renderizado y comportamiento JavaScript.

- **Page Object Model (POM):** Patrón de diseño para abstraer la interfaz de usuario en objetos reutilizables, facilitando el mantenimiento de los scripts.

#### Pruebas de Rendimiento (K6)

- **Prueba de Carga (Load Test):** Simula el volumen esperado de usuarios concurrentes para validar que el sistema cumple con los objetivos de rendimiento bajo carga normal.

- **Prueba de Estrés (Stress Test):** Incrementa progresivamente la carga hasta superar la capacidad máxima del sistema para identificar puntos de quiebre y comportamiento de degradación.

- **Prueba de Resistencia (Soak Test):** Mantiene una carga moderada durante un período prolongado para detectar memory leaks y degradación gradual del rendimiento.

- **Prueba de Pico (Spike Test):** Aplica un aumento repentino y extremo de carga para simular escenarios como la apertura de venta de un evento popular.

- **Escenario Basado (Scenario-Based):** K6 permite definir escenarios con múltiples flujos de usuarios ejecutándose simultáneamente (ej: 70% navegando, 20% comprando, 10% en check-in).

### 5.3 Criterio de Finalización y Prueba

El proceso de pruebas de sistema se dará por concluido únicamente cuando se cumplan satisfactoriamente los siguientes criterios:

1. **Cobertura de Flujos Críticos:** Se deben haber ejecutado y aprobado el 100% de los flujos críticos definidos en la matriz de trazabilidad (FC-01 a FC-08).
2. **Tasa de Éxito E2E:** Las pruebas E2E deben alcanzar una tasa de éxito mínima del 95% en cada ejecución.
3. **Objetivos de Rendimiento:** Los resultados de las pruebas de rendimiento deben cumplir con las métricas definidas:
   - Tiempo de respuesta promedio < 2 segundos bajo carga normal.
   - Throughput mínimo de 100 RPS en escenario de pico.
   - Tasa de error < 1% bajo carga normal.
4. **Severidad de Defectos:** No deben existir defectos abiertos con severidad crítica o alta sin plan de resolución.
5. **Integridad de Entregables:** Todos los entregables definidos en la sección 5.1 deben estar completos y publicados en la Wiki.
6. **Aprobación y Verificación:** Toda contribución debe pasar por un Pull Request (PR) hacia `main` con:
   - Checks exitosos de la suite de GitHub Actions.
   - Aprobación formal del Test Lead tras la revisión de código y resultados.

### 5.4 Métricas

Esta sección detalla el conjunto de métricas que se recogerán durante la ejecución de las pruebas de sistema:

Esta sección detalla el conjunto de métricas que se recogerán durante la ejecución de las pruebas de sistema:

| Categoría | Métricas Clave y Objetivos |
| :--- | :--- |
| **Efectividad** | • **Cobertura:** 100% de flujos críticos.<br>• **Defectos:** Tasa por cada 100 horas y porcentaje de detección temprana (antes de producción). |
| **Eficiencia** | • **Tiempo E2E:** Menor a 15 minutos.<br>• **Automatización:** Porcentaje de casos automatizados vs. manuales.<br>• **Mantenibilidad:** Cambios requeridos tras cada release. |
| **Rendimiento (K6)** | • **Tiempos:** Promedio < 2s, percentiles p95 y p99.<br>• **Carga:** Throughput (RPS) y tasa de error (4xx/5xx).<br>• **Conexión:** Tiempo de TLS handshake. |

### 5.5 Requisitos del Entorno de Pruebas

#### Variables de Entorno

| Variable | Descripción | Valor por defecto |
|:---|:---|:---|
| `PLAYWRIGHT_BASE_URL` | URL base del sistema bajo prueba | `http://localhost:8080` |
| `E2E_SERVER_APIKEY` | API Key de administrador para crear/eliminar eventos de prueba | (requerida) |
| `CI` | Indica entorno de integración continua | `false` |

#### Entorno de Pruebas E2E

| Componente | Especificación |
|:---|:---|
| **URL base** | `http://localhost:8080` (configurable via `PLAYWRIGHT_BASE_URL`) |
| **Navegadores** | Chromium (Desktop Chrome), Firefox (Desktop Firefox) |
| **Framework** | Playwright v1.59.1 |
| **Lenguaje** | TypeScript |
| **Package Manager** | pnpm |
| **Node.js** | v22.x |
| **Sistema Operativo** | Linux (NixOS compatible, Ubuntu, Kde) |

<!--#### Configuración de Playwright

La configuración de Playwright se encuentra en `src/test/e2e/playwright.config.ts`:

```typescript
export default defineConfig({
    testDir: "./tests",
    fullyParallel: true,
    forbidOnly: false,
    retries: process.env.CI ? 2 : 0,
    workers: process.env.CI ? 1 : undefined,
    reporter: [["html", { open: "never" }], ["list"]],
    use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL || "http://localhost:8080",
        trace: "on-first-retry",
        screenshot: "only-on-failure",
        video: "retain-on-failure",
    },
    projects: [
        { name: "chromium", use: { ...devices["Desktop Chrome"] } },
        { name: "firefox", use: { ...devices["Desktop Firefox"] } },
    ],
});
```

**Configuración clave:**
- **`fullyParallel: true`**: Ejecución paralela de tests para reducir tiempo total.
- **`retries: 2` en CI**: Reintentos automáticos en entorno de integración continua.
- **`workers: 1` en CI**: Ejecución secuencial en CI para evitar conflictos de recursos.
- **`trace: "on-first-retry"`**: Captura de traza completa solo en el primer reintento (para debugging).
- **`screenshot: "only-on-failure"`**: Capturas de pantalla automáticas al fallar.
- **`video: "retain-on-failure"`**: Grabación de video conservada solo si el test falla.-->

#### Entorno de Pruebas de Rendimiento
| Componente | Especificación |
|:---|:---|
| **Herramienta** | K6 (versión estable actual) |
| **Target URL** | `http://localhost:8080` (configurable) |
| **VUS (Virtual Users)** | Configurable por escenario (50-500) |
| **Duración** | Variable según escenario (30s - 30min) |
| **Métricas exportadas** | JSON, Prometheus (opcional) |

#### Infraestructura de CI/CD
- **Plataforma:** GitHub Actions con runners `ubuntu-latest`.
- **Disparadores:** Pull Request hacia `main` y push a `main`.
- **Cache:** Dependencias de pnpm y binarios de Playwright para acelerar ejecuciones.

#### Base de Datos y Servicios
- **PostgreSQL:** Entorno de pruebas con datos semilla para escenarios de prueba.
- **Docker Compose:** Orquestación del entorno completo (backend + frontend + DB) para pruebas locales.
- **Flyway:** Migraciones automáticas del esquema de base de datos al iniciar el contenedor.

### 5.6 Matriz de Trazabilidad

La matriz de trazabilidad vincula los requisitos funcionales del sistema con los casos de prueba de sistema correspondientes:

| ID Requisito | Descripción del Requisito | Caso de Prueba | Herramienta | Estado |
|:---|:---|:---|:---|:---|
| REQ-01 | El sistema debe permitir a los usuarios comprar entradas para eventos | FC-01: Flujo completo de compra | Playwright | Pendiente |
| REQ-02 | El sistema debe permitir a los administradores crear y gestionar eventos | FC-02: Creación y configuración de eventos | Playwright | Pendiente |
| REQ-03 | El sistema debe validar la asistencia mediante check-in | FC-03: Check-in de asistentes | Playwright | Pendiente |
| REQ-04 | El sistema debe gestionar reservas con tiempo límite | FC-04: Gestión de reservas y cancelaciones | Playwright | Pendiente |
| REQ-05 | El sistema debe generar tickets digitales (PDF, Apple Wallet, Google Wallet) | FC-05: Generación de tickets digitales | Playwright | Pendiente |
| REQ-06 | El sistema debe controlar el acceso por roles | FC-06: Autenticación y autorización | Playwright | Pendiente |
| REQ-07 | El sistema debe manejar colas de espera en eventos de alta demanda | FC-07: Cola de espera | Playwright | Pendiente |
| REQ-08 | El sistema debe soportar códigos de descuento y precios dinámicos | FC-08: Gestión de descuentos | Playwright | Pendiente |
| REQ-09 | El sistema debe responder en menos de 2s bajo carga normal | PR-01: Carga normal | K6 | Pendiente |
| REQ-10 | El sistema debe soportar al menos 100 RPS en pico de ventas | PR-02: Pico de ventas | K6 | Pendiente |
| REQ-11 | El sistema debe degradarse graceful bajo sobrecarga | PR-03: Estrés extremo | K6 | Pendiente |
| REQ-12 | El sistema no debe presentar memory leaks en uso prolongado | PR-04: Resistencia | K6 | Pendiente |

<!--## 6. Estructura de Pruebas

En este capítulo se presenta la organización general de las pruebas de sistema.

### 6.1 Estructura de Directorios-->
<!--
```
src/test/e2e/
├── playwright.config.ts          # Configuración de Playwright
├── package.json                  # Dependencias y scripts
├── tsconfig.json                 # Configuración TypeScript
├── biome.json                    # Linter/formatter
├── README.md                     # Documentación de configuración
├── tests/
│   ├── smoke.spec.ts             # Pruebas de smoke (carga de página)
│   ├── purchase-flow.spec.ts     # Flujo completo de compra
│   ├── admin-event.spec.ts       # Gestión de eventos desde admin
│   ├── check-in.spec.ts          # Flujo de check-in
│   ├── reservation.spec.ts       # Reservas y cancelaciones
│   └── authentication.spec.ts    # Login y control de acceso
├── fixtures/
│   └── test-fixtures.ts          # Fixtures personalizados de Playwright
├── helpers/
│   └── api-helper.ts             # Utilidades para crear/eliminar eventos vía API
└── resources/
    └── e2e/
        └── create-event-for-e2e.json  # Template JSON para creación de eventos
```-->

<!--### 6.2 Patrón Page Object Model (POM)

Las pruebas E2E se organizan utilizando el patrón Page Object Model para abstraer la interacción con la interfaz de usuario:

```typescript
// Ejemplo de estructura POM
pages/
├── EventListPage.ts      # Interacción con el listado de eventos
├── EventDetailPage.ts    # Interacción con el detalle de un evento
├── CheckoutPage.ts       # Interacción con el formulario de checkout
├── ConfirmationPage.ts   # Interacción con la página de confirmación
├── AdminDashboard.ts     # Interacción con el panel de administración
└── CheckInPage.ts        # Interacción con la funcionalidad de check-in
```
-->

<!--
### 6.3 Organización de Pruebas de Rendimiento

```
tests/performance/
├── load-test.ts          # Prueba de carga normal
├── stress-test.ts        # Prueba de estrés
├── soak-test.ts          # Prueba de resistencia
├── spike-test.ts         # Prueba de pico
└── utils/
    ├── thresholds.ts     # Umbrales de aceptación
    └── scenarios.ts      # Configuración de escenarios
```-->

## 6. Organización

Esta sección establece la distribución de funciones dentro del equipo de pruebas de sistema.

### 6.1 Roles y Responsabilidades

Se utiliza una matriz RACI para las actividades clave del plan de pruebas de sistema:

| Actividad Clave / Tarea | Christian Mestas (Lead) | Mariel Jara (DEV) | Gustavo Sequeiros (DEV) | Mathias Barrios (DEV) | Rodrigo Fernandez (DEV) | Alvaro Quispe (DEV) | Docente |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **1. Definición del Plan de Pruebas de Sistema** | **A** | **R** | **R** | **C** | **C** | **C** | **I** |
| **2. Configuración del Entorno de Pruebas (Docker, Playwright, K6)** | **A** | **R** | **C** | **C** | **C** | **C** | **I** |
| **3. Diseño de Casos de Prueba E2E** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **4. Implementación de Scripts Playwright** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **5. Implementación de Scripts K6 (Rendimiento)** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **6. Ejecución y Reporte de Pruebas E2E** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **7. Ejecución y Análisis de Pruebas de Rendimiento** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **8. Consolidación de Reportes y Matriz de Trazabilidad** | **R** | **C** | **C** | **C** | **C** | **C** | **I** |

## 8. Cronograma
El cronograma de actividades para el ciclo de pruebas de sistema se distribuye de la siguiente manera:

| Semana | Actividad | Entregable |
|:---:|:---|:---|
| **1** | Configuración del entorno de pruebas (Docker, Playwright, K6) | Entorno funcional documentado |
| **1** | Diseño de casos de prueba E2E para flujos críticos | Documento de diseño de pruebas |
| **1** | Implementación de scripts Playwright (Flujos FC-01 a FC-04) | Scripts de pruebas E2E |
| **2** | Implementación de scripts Playwright (Flujos FC-05 a FC-08) | Scripts de pruebas E2E completos |
| **2** | Implementación de scripts K6 (Escenarios PR-01 a PR-04) | Scripts de pruebas de rendimiento |
| **2** | Ejecución de pruebas E2E y reporte de defectos | Reporte de ejecución E2E |
| **3** | Ejecución de pruebas de rendimiento y análisis de métricas | Reporte de rendimiento |
| **3** | Corrección de defectos críticos y re-ejecución | Defectos resueltos |
| **3** | Consolidación de matriz de trazabilidad y entregables finales | Todos los entregables publicados |
| **3** | Revisión final y aprobación del plan | Plan cerrado y aprobado |

---

> [!NOTE]
> Este documento se actualizará conforme avance la ejecución de las pruebas. Los resultados parciales y los defectos encontrados se registrarán en los issues de GitHub correspondientes.
