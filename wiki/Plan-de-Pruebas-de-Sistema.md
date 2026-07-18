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
El plan se centra en las pruebas de sistema para la plataforma de reserva de entradas alf.io. El objetivo principal es evaluar el comportamiento global del sistema integrado, garantizando el cumplimiento de los requisitos no funcionales (rendimiento, estabilidad bajo carga masiva, seguridad y calidad de código). La estrategia de pruebas de sistema se basa en una estrategia no funcional dirigida por objetivos.

- *Validación de Rendimiento*: Pruebas de carga y estrés concurrentes sobre los servicios del sistema mediante K6.
- *Pruebas de Fuzzing*: Pruebas de mutación guiadas por cobertura mediante Jazzer para detectar crashes y vulnerabilidades en clases utilitarias y de modelo.
- *Análisis Estático de Seguridad*: Análisis de vulnerabilidades, bugs y code smells mediante SonarQube Community.

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
   - Documentación oficial de K6: [grafana.com/docs/k6](https://grafana.com/docs/k6/)
   - Documentación de Jazzer (fuzzing): [github.com/CodeIntelligenceTesting/jazzer](https://github.com/CodeIntelligenceTesting/jazzer)
   - Documentación de SonarQube: [docs.sonarsource.com/sonarqube](https://docs.sonarsource.com/sonarqube/)
6. Stack técnico del proyecto:
   - Backend: Java 17, Spring Boot 3.5.x, Jetty, PostgreSQL
   - Frontend público: Angular 17
   - Frontend admin: Lit 3 + Vite (Web Components)

### 1.3 Glosario

- **K6:** Herramienta de pruebas de rendimiento y carga escrita en Go, con scripts en JavaScript.
- **SPA (Single Page Application):** Aplicación web que carga una sola página HTML y actualiza dinámicamente el contenido.
- **Check-in:** Proceso de validación y registro de asistencia de un participante al evento mediante escaneo de código QR o entrada manual.
- **Flyway:** Herramienta de migración de bases de datos que gestiona el versionamiento del esquema.
- **Stress Test:** Prueba que lleva el sistema más allá de su carga máxima para identificar puntos de quiebre.
- **RPS (Requests Per Second):** Métrica que indica el número de peticiones por segundo que procesa el sistema.
- VU (Virtual User): Hilo de ejecución concurrente utilizado por K6 que simula el comportamiento continuo de un usuario real en el sistema.
- **Jazzer:** Herramienta de fuzzing guiada por cobertura para JVM, basada en libFuzzer. Detecta crashes, exception errors y vulnerabilidades de seguridad mutando inputs.
- **Fuzzing:** Técnica de prueba automatizada que genera datos de entrada aleatorios o mutados para encontrar comportamientos inesperados, crashes y vulnerabilidades.
- **SonarQube:** Plataforma de análisis estático de código que detecta bugs, vulnerabilidades de seguridad, code smells y deuda técnica.
- **SAST (Static Application Security Testing):** Análisis estático de seguridad del código fuente para identificar vulnerabilidades sin ejecutar la aplicación.

## 2. Especificaciones de las Pruebas
El sistema de venta de entradas se divide en los siguientes subprocesos funcionales y de rendimiento para la cobertura del sistema completo:

### 2.1 Proyecto y Subprocesos de Prueba
Esta sección detalla la estrategia de pruebas de sistema para alf.io, validando la integración completa de los componentes del sistema en un entorno que simula producción.

El proceso de pruebas de sistema se divide en los siguientes subprocesos:

#### Subproceso 1 - Pruebas de Rendimiento con K6
- **Objetivo:** Evaluar el comportamiento del sistema bajo cargas variables de trabajo, midiendo tiempos de respuesta, throughput y estabilidad.
- **Técnica:** Scripts de pruebas de carga, estrés y resistencia con K6.
- **Foco de Validación:**
  - Tiempos de respuesta en endpoints críticos bajo carga normal.
  - Capacidad de concurrentes simultáneos.
  - Comportamiento bajo estrés extremo (pico de ventas de un evento popular).
  - Degradação graceful del sistema bajo sobrecarga.

#### Subproceso 2 - Pruebas de Fuzzing con Jazzer
- **Objetivo:** Detectar crashes, excepciones no manejadas y vulnerabilidades de seguridad en clases utilitarias, de modelo y de procesamiento de datos mediante mutación guiada por cobertura.
- **Técnica:** Fuzzing guiado por cobertura (coverage-guided fuzzing) usando Jazzer en modo JUnit, con mutación de inputs para explorar caminos de ejecución no convencionales.
- **Foco de Validación:**
  - Deserialización de JSON (modelos Event, Ticket, TicketReservation, BillingDocument).
  - Validadores (email, nombre italiano, código fiscal, PIN).
  - Utilidades criptográficas (CheckInManager.encrypt, HMAC, MD5).
  - Utilidades de plantillas (Mustache, traducciones i18n).
  - Utilidades de negocio (precios, impuestos, moneda, rangos de horario).
  - Procesamiento de extensiones (JSON.parse/stringify, conversión de tipos).
- **Herramienta:** `com.code-intelligence:jazzer-junit:0.30.0`
- **Archivos de prueba:** 35 clases fuzz bajo `src/test/java/alfio/fuzz/`

#### Subproceso 3 - Análisis Estático de Seguridad con SonarQube
- **Objetivo:** Identificar bugs, vulnerabilidades de seguridad, code smells y deuda técnica en el código fuente del backend Java.
- **Técnica:** Análisis estático (SAST) mediante SonarQube Community Edition ejecutado en un contenedor Docker efímero durante el pipeline de CI.
- **Foco de Validación:**
  - Bugs de confiabilidad (métodos transaccionales llamados vía `this`, assertions en try-catch).
  - Vulnerabilidades de seguridad (hash débil, cifrado inseguro, CSRF deshabilitado, directorios escribibles).
  - Code smells de mantenibilidad (complejidad cognitiva excesiva, literales duplicados, imports wildcard).
  - Deuda técnica y ratio de duplicación de código.
- **Herramienta:** SonarQube Community Edition (`sonarqube:community`) + `sonarqube-community-reporter` para generación de HTML.
- **Integration:** Plugin Gradle `org.sonarqube` v7.3.0.8198, ejecutado como job en GitHub Actions.

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

**Requisitos No Funcionales Seleccionados para Validación:**

Los requisitos no funcionales seleccionados para pruebas de sistema son **seguridad** y **rendimiento**:

| Categoría NR | Descripción | Herramienta | Subproceso |
|:---|:---|:---|:---|
| **Rendimiento** | Validar tiempos de respuesta, throughput y estabilidad bajo carga concurrente | K6 | Subproceso 1 |
| **Seguridad (Fuzzing)** | Detectar crashes, excepciones no manejadas y vulnerabilidades en procesamiento de datos | Jazzer | Subproceso 2 |
| **Seguridad (SAST)** | Identificar bugs, vulnerabilidades de seguridad y code smells mediante análisis estático | SonarQube | Subproceso 3 |

**Pruebas de Rendimiento (K6):**

| ID | Escenario de Rendimiento | Descripción | Métrica Objetivo |
|:---|:---|:---|:---|
| PR-01 | Carga normal | 50 usuarios concurrentes navegando y comprando entradas | Tiempo respuesta < 2s |
| PR-02 | Pico de ventas | 200 usuarios concurrentes en los primeros 5 minutos de apertura de venta | Throughput > 100 RPS |
| PR-03 | Estrés extremo | 500+ usuarios concurrentes para identificar puntos de quiebre | Degradación gradual, sin caída completa |
| PR-04 | Resistencia | Carga sostenida de 100 usuarios durante 30 minutos | Sin memory leaks, tiempos estables |

**Pruebas de Fuzzing (Jazzer):**

| ID | Objetivo Fuzzing | Clases Objetivo | Herramienta |
|:---|:---|:---|:---|
| FZ-01 | Deserialización de modelos JSON | Event, Ticket, TicketReservation, BillingDocument, PromoCodeDiscount | Jazzer |
| FZ-02 | Validadores de entrada | ItalianTaxIdValidator, Validator (email), PinGenerator | Jazzer |
| FZ-03 | Utilidades criptográficas | CheckInManager.encrypt, ExtensionUtils (HMAC, MD5, Base64) | Jazzer |
| FZ-04 | Utilidades de plantillas | MustacheCustomTag, TemplateManager (i18n) | Jazzer |
| FZ-05 | Utilidades de negocio | MonetaryUtil, HoursRange, EventUtil, WorkingDaysAdjusters | Jazzer |
| FZ-06 | Procesamiento de extensiones | ExtensionJSON, ExtensionUtils, SqlUtils, HttpUtils | Jazzer |

**Análisis Estático de Seguridad (SonarQube):**

| ID | Categoría | Descripción | Herramienta |
|:---|:---|:---|:---|
| SQ-01 | Bugs | Defectos de confiabilidad detectados por análisis estático | SonarQube |
| SQ-02 | Vulnerabilities | Vulnerabilidades de seguridad (hash débil, cifrado, CSRF) | SonarQube |
| SQ-03 | Code Smells | Problemas de mantenibilidad y deuda técnica | SonarQube |
| SQ-04 | Security Hotspots | Código que requiere revisión manual de seguridad | SonarQube |

**Endpoints de API críticos a probar:**
- `GET /api/v1/public/events` - Listado de eventos públicos.
- `POST /api/v1/event/{eventId}/ticket/{ticketId}/check-in` - Check-in de asistentes.
- `POST /api/v1/admin/event/create` - Creación de eventos.
- `GET /api/v1/admin/event/{eventShortName}/attendees` - Lista de asistentes.
- `POST /api/v1/public/event/{eventId}/ticket/{ticketCategoryId}/reserve` - Reserva de entradas.
- `POST /api/v1/public/event/{eventId}/order/{orderId}/stripe/charge` - Proceso de pago.

#### Elementos Excluidos del Alcance

- **Pruebas funcionales E2E:** Las pruebas de flujos completos de usuario (Playwright) se ejecutan como pruebas de aceptación, no como pruebas de sistema.
- **Pruebas de Regresión Completa:** Se priorizan los requisitos no funcionales seleccionados; la regresión funcional se cubre con las suites de pruebas unitarias y de integración existentes.
- **Pruebas con Pasarelas de Pago Reales:** Todas las transacciones de pago se simulán en entorno de sandbox o se mockearán para evitar cargos reales.
- **Pruebas de Usabilidad (UX Testing):** Evaluación subjetiva de la experiencia de usuario con usuarios reales.
- **Pruebas de Accesibilidad (a11y):** Auditoría completa de accesibilidad WCAG.
- **Pruebas de Internacionalización (i18n):** Validación completa de traducciones.

### 2.4 Suposiciones y Restricciones

**Suposiciones**
- El entorno de pruebas está desplegado y accesible (localmente vía Docker o en un entorno de staging).
- La base de datos de pruebas está en un estado conocido y reproducible al inicio de cada ejecución.
- El equipo tiene acceso a las herramientas de pruebas: K6, Jazzer y SonarQube.
- Docker está disponible para ejecutar el contenedor de SonarQube y el entorno de pruebas.

**Restricciones**
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
| 2 | Flujos de prueba frágiles por cambios en selectores CSS/HTML | 4 | 3 | 12 | Uso de selectores robustos (data-testid), abstracción de interfaz. |
| 3 | Tiempo de ejecución de pruebas excesivo | 3 | 3 | 9 | Ejecución selectiva por tags, optimización de wait times. |
| 4 | Resultados inconsistentes en pruebas de rendimiento (flaky) | 3 | 4 | 12 | Múltiples iteraciones, warm-up previo, aislamiento de red, ejecución en horarios de baja actividad. |
| 5 | Falta de experiencia del equipo con K6/Jazzer/SonarQube | 3 | 3 | 9 | Sesiones de capacitación inicial, documentación de referencia, pair programming. |
| 6 | Datos de prueba que dejan estados residuales | 2 | 4 | 8 | Generación dinámica de datos con slugs aleatorios, limpieza automática post-ejecución via API helper. |
| 7 | Dificultades con la integración de pago en sandbox | 2 | 3 | 6 | Mock de respuestas de pago, uso de entorno sandbox de Stripe/PayPal con credenciales de prueba. |
| 8 | Fuzzing encuentra crashes en código legado sin plan de corrección | 3 | 3 | 9 | Clasificar crashes por severidad; los crashes en utilidades puras se corrigen inmediatamente; los de baja prioridad se documentan como deuda técnica. |
| 9 | SonarQube reporta vulnerabilidades críticas sin corrección inmediata | 2 | 4 | 8 | Priorizar corrección de vulnerabilidades BLOCKER/CRITICAL; documentar las demás como deuda técnica con plan de resolución. |

## 5. Metodología

### 5.1 Entregables de Prueba   

Para el proceso de pruebas de sistema de `alf.io`, se generarán los siguientes artefactos como evidencia del cumplimiento de los objetivos de calidad:

- **Scripts de Pruebas de Rendimiento (K6):** Conjunto de scripts para pruebas de carga, estrés y resistencia con métricas documentadas.
- **Scripts de Pruebas de Fuzzing (Jazzer):** 35 clases fuzz target cubriendo utilidades, modelos, validadores y procesamiento de extensiones.
- **Reporte de Pruebas de Rendimiento:** Análisis de métricas de rendimiento (tiempos de respuesta, throughput, errores) con gráficas y comparativas.
- **Reporte de Pruebas de Fuzzing:** Resultados del fuzzing guiado por cobertura, incluyendo cobertura de código y crashes encontrados.
- **Reporte de Análisis Estático (SonarQube):** Análisis de bugs, vulnerabilidades, code smells y deuda técnica con desglose por severidad.
- **Matriz de Trazabilidad:** Documento que vincula los casos de prueba de sistema con los requisitos no funcionales del sistema.
- **Lista de Defectos Encontrados:** Registro de todos los bugs identificados durante la ejecución de las pruebas, con severidad, prioridad y estado.

Los entregables se encuentran en la sección correspondiente de esta Wiki.

### 5.2 Técnicas de Diseño de Prueba

#### Pruebas de Rendimiento (K6)

- **Prueba de Carga (Load Test):** Simula el volumen esperado de usuarios concurrentes para validar que el sistema cumple con los objetivos de rendimiento bajo carga normal.

- **Prueba de Estrés (Stress Test):** Incrementa progresivamente la carga hasta superar la capacidad máxima del sistema para identificar puntos de quiebre y comportamiento de degradación.

- **Prueba de Resistencia (Soak Test):** Mantiene una carga moderada durante un período prolongado para detectar memory leaks y degradación gradual del rendimiento.

- **Prueba de Pico (Spike Test):** Aplica un aumento repentino y extremo de carga para simular escenarios como la apertura de venta de un evento popular.

- **Escenario Basado (Scenario-Based):** K6 permite definir escenarios con múltiples flujos de usuarios ejecutándose simultáneamente (ej: 70% navegando, 20% comprando, 10% en check-in).

### 5.3 Criterio de Finalización y Prueba

El proceso de pruebas de sistema se dará por concluido únicamente cuando se cumplan satisfactoriamente los siguientes criterios:

1. **Objetivos de Rendimiento:** Los resultados de las pruebas de rendimiento deben cumplir con las métricas definidas:
   - Tiempo de respuesta promedio < 2 segundos bajo carga normal.
   - Throughput mínimo de 100 RPS en escenario de pico.
   - Tasa de error < 1% bajo carga normal.
2. **Cobertura de Fuzzing:** Se deben ejecutar las 35 clases fuzz target sin crashes críticos.
3. **Análisis de Seguridad:** SonarQube debe completar el análisis con Quality Gate OK.
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
| **Rendimiento (K6)** | • **Tiempos:** Promedio < 2s, percentiles p95 y p99.<br>• **Carga:** Throughput (RPS) y tasa de error (4xx/5xx).<br>• **Conexión:** Tiempo de TLS handshake. |
| **Fuzzing (Jazzer)** | • **Cobertura de fuzzing:** Número de clases fuzz target ejecutadas.<br>• **Crashes encontrados:** Número de crashes o exceptions inesperadas.<br>• **Cobertura de código:** Instrucciones cubiertas por las clases fuzzed. |
| **Seguridad (SonarQube)** | • **Quality Gate:** Estado (OK/BLOCKED).<br>• **Bugs:** Número total y por severidad.<br>• **Vulnerabilities:** Número total y por severidad.<br>• **Code Smells:** Número total y ratio de deuda técnica. |

### 5.5 Requisitos del Entorno de Pruebas

#### Variables de Entorno

| Variable | Descripción | Valor por defecto |
|:---|:---|:---|
| `BASE_URL` | URL base del sistema bajo prueba (K6) | `http://localhost:8080` |
| `API_KEY` | API Key de administrador para crear eventos de prueba (K6) | (requerida) |
| `CI` | Indica entorno de integración continua | `false` |

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

#### Base de Datos y Servicios
- **PostgreSQL:** Entorno de pruebas con datos semilla para escenarios de prueba.
- **Docker Compose:** Orquestación del entorno completo (backend + frontend + DB) para pruebas locales.
- **Flyway:** Migraciones automáticas del esquema de base de datos al iniciar el contenedor.

### 5.6 Matriz de Trazabilidad

La matriz de trazabilidad vincula los requisitos no funcionales del sistema con los casos de prueba de sistema correspondientes:

| ID Requisito | Descripción del Requisito | Caso de Prueba | Herramienta | Estado |
|:---|:---|:---|:---|:---|
| REQ-NF-01 | El sistema debe responder en menos de 2s bajo carga normal | PR-01: Carga normal | K6 | Pendiente |
| REQ-NF-02 | El sistema debe soportar al menos 100 RPS en pico de ventas | PR-02: Pico de ventas | K6 | Pendiente |
| REQ-NF-03 | El sistema debe degradarse graceful bajo sobrecarga | PR-03: Estrés extremo | K6 | Pendiente |
| REQ-NF-04 | El sistema no debe presentar memory leaks en uso prolongado | PR-04: Resistencia | K6 | Pendiente |
| REQ-NF-05 | El sistema no debe contener crashes en procesamiento de datos inválidos | FZ-01 a FZ-06: Fuzzing | Jazzer | Pendiente |
| REQ-NF-06 | El código no debe contener vulnerabilidades de seguridad conocidas | SQ-01 a SQ-04: Análisis estático | SonarQube | Pendiente |

## 6. Organización

Esta sección establece la distribución de funciones dentro del equipo de pruebas de sistema.

### 6.1 Roles y Responsabilidades

Se utiliza una matriz RACI para las actividades clave del plan de pruebas de sistema:

| Actividad Clave / Tarea | Christian Mestas (Lead) | Mariel Jara (DEV) | Gustavo Sequeiros (DEV) | Mathias Barrios (DEV) | Rodrigo Fernandez (DEV) | Alvaro Quispe (DEV) | Docente |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **1. Definición del Plan de Pruebas de Sistema** | **A** | **R** | **R** | **C** | **C** | **C** | **I** |
| **2. Configuración del Entorno de Pruebas (Docker, K6, Jazzer, SonarQube)** | **A** | **R** | **C** | **C** | **C** | **C** | **I** |
| **3. Diseño de Casos de Prueba No Funcionales** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **4. Implementación de Scripts K6 (Rendimiento)** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **5. Implementación de Pruebas Fuzzing (Jazzer)** | **A** | **C** | **R** | **C** | **C** | **C** | **I** |
| **6. Configuración de Análisis Estático (SonarQube)** | **A** | **R** | **C** | **C** | **C** | **C** | **I** |
| **7. Ejecución y Análisis de Pruebas de Rendimiento** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **8. Ejecución de Pruebas Fuzzing y Análisis de Resultados** | **A** | **C** | **R** | **C** | **C** | **C** | **I** |
| **11. Consolidación de Reportes y Matriz de Trazabilidad** | **R** | **C** | **C** | **C** | **C** | **C** | **I** |

## 8. Cronograma
El cronograma de actividades para el ciclo de pruebas de sistema se distribuye de la siguiente manera:

| Semana | Actividad | Entregable |
|:---:|:---|:---|
| **1** | Configuración del entorno de pruebas (Docker, K6, Jazzer, SonarQube) | Entorno funcional documentado |
| **1** | Diseño de casos de prueba no funcionales (rendimiento, fuzzing, seguridad) | Documento de diseño de pruebas |
| **1** | Implementación de scripts K6 (Escenarios PR-01 a PR-04) | Scripts de pruebas de rendimiento |
| **2** | Configuración e implementación de pruebas fuzzing con Jazzer | 35 clases fuzz target implementadas |
| **2** | Configuración de SonarQube en pipeline de CI | Análisis estático configurado |
| **2** | Ejecución de pruebas de rendimiento y análisis de métricas | Reporte de rendimiento |
| **3** | Ejecución de pruebas fuzzing y análisis de crashes | Reporte de fuzzing |
| **3** | Ejecución de análisis SonarQube y análisis de vulnerabilidades | Reporte de SonarQube |
| **3** | Corrección de defectos críticos y re-ejeción | Defectos resueltos |
| **3** | Consolidación de matriz de trazabilidad y entregables finales | Todos los entregables publicados |
| **3** | Revisión final y aprobación del plan | Plan cerrado y aprobado |

---

> [!NOTE]
> Este documento se actualizará conforme avance la ejecución de las pruebas. Los resultados parciales y los defectos encontrados se registrarán en los issues de GitHub correspondientes.
