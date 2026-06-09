# Plan de Pruebas de Integración del Sistema alf.io

## Índice
- [Información General](#1-información-general)
- [Especificaciones de las Pruebas](#2-especificaciones-de-las-pruebas)
- [Comunicación de las Pruebas](#3-comunicación-de-las-pruebas)
- [Registro de Riesgos](#4-registro-de-riesgos)
- [Metodología](#5-metodología)
<!--
- [Estructura de Pruebas](#6-estructura-de-pruebas)
-->
- [Organización](#7-organización)
- [Cronograma](#8-cronograma)

## Información General

### Alcance
Este plan cubre las pruebas de integración de alf.io, cuyo objetivo es verificar que los módulos del sistema interactúan correctamente entre sí y con sus dependencias reales (base de datos PostgreSQL, servicios de correo, pasarelas de pago simuladas, etc.). A diferencia de las pruebas unitarias, aquí **no se usan mocks** para las capas internas del sistema; se valida la colaboración real entre componentes como controladores, managers, repositorios y la base de datos.

La estrategia de integración adoptada es **Big Bang incremental**: primero se integran y validan los módulos de infraestructura (base de datos, migraciones Flyway, configuración de Spring), luego los flujos de negocio críticos (reservas, pagos, check-in) y finalmente la comunicación entre el backend y el frontend.

### Referencias
1. Estándares de Ingeniería de Software y Pruebas
   - **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software.
   - **Repositorio de Código Abierto de alf.io:** [GitHub - alfio-event/alf.io](https://github.com/alfio-event/alf.io)
   - **Documentación de Arquitectura de alf.io:** [[Arquitectura]] del proyecto.
   - **Plan de Pruebas Unitarias:** [[Plan-de-Pruebas-Unitarias]] (base de referencia para este documento).
   - **Backend:**
     - Documentación de Spring Boot 3.x y Spring Test.
     - Documentación de JUnit 5 y Testcontainers.
     - Documentación de Flyway (migraciones de esquema).
   - **Frontend:**
     - Documentación de Angular HttpClient y pruebas de integración con servidores reales.

### Glosario

- **Prueba de Integración:** Tipo de prueba que valida la interacción correcta entre dos o más módulos o componentes del sistema, incluyendo capas reales como la base de datos.
- **Testcontainers:** Librería Java que levanta contenedores Docker efímeros (PostgreSQL, etc.) durante la ejecución de pruebas, garantizando un entorno real y reproducible.
- **Flyway:** Herramienta de migración de base de datos. En las pruebas de integración, se ejecuta sobre un contenedor PostgreSQL real para validar que el esquema es coherente.
- **@SpringBootTest:** Anotación de Spring Test que levanta el contexto completo de la aplicación para pruebas de integración de backend.
- **Estrategia Big Bang incremental:** Estrategia de integración donde los módulos se van conectando en grupos lógicos (de infraestructura a negocio) antes de integrar el sistema completo.
- **Smoke Test:** Conjunto mínimo de pruebas de integración que verifica que el sistema arranca y sus flujos principales funcionan, sin entrar en todos los casos de borde.
- **API Contract:** Acuerdo implícito sobre la forma (endpoints, schemas JSON, códigos HTTP) de la API REST. Las pruebas de integración validan su cumplimiento.
- **End-to-End parcial (E2E parcial):** Flujo que atraviesa varias capas reales (Controller → Manager → Repository → DB) sin involucrar la interfaz de usuario.

---

## Especificaciones de las Pruebas

### Proyecto y Subprocesos de Prueba

alf.io es una plataforma de venta de entradas y gestión de eventos. Para las pruebas de integración, el sistema se divide en los siguientes subprocesos de validación:

#### Subproceso 1 – Integración de Infraestructura y Base de Datos
- **Objetivo:** Verificar que Flyway aplica correctamente todas las migraciones sobre PostgreSQL y que el contexto de Spring Boot carga sin errores.
- **Técnica:** `@SpringBootTest` con Testcontainers (PostgreSQL).
- **Foco:** Migraciones de esquema, configuración de datasource, inicialización de beans críticos.

#### Subproceso 2 – Integración de Capas de Negocio (Backend E2E parcial)
- **Objetivo:** Validar que los flujos de negocio completos (Controller → Manager → Repository → DB) funcionan correctamente de extremo a extremo en el backend.
- **Técnica:** Tests `@SpringBootTest` con `MockMvc` o `TestRestTemplate` contra un PostgreSQL real en contenedor.
- **Foco:**
  - Flujo de creación y configuración de eventos.
  - Flujo de reserva y compra de entradas (incluyendo cálculo de precios e impuestos).
  - Flujo de check-in y validación de tickets.
  - Envío de notificaciones por correo (con servidor SMTP simulado o log).

#### Subproceso 3 – Integración de API REST (Contrato de API)
- **Objetivo:** Verificar que los endpoints REST devuelven los esquemas JSON, códigos HTTP y cabeceras correctas según el contrato de la API.
- **Técnica:** `MockMvc` con assertions sobre el cuerpo de la respuesta y serialización/deserialización Jackson.
- **Foco:** Endpoints de creación de eventos, compra de entradas, autenticación (SSO/OIDC), gestión de usuarios y check-in.

#### Subproceso 4 – Integración Frontend–Backend
- **Objetivo:** Verificar que el frontend (Angular / Lit) se comunica correctamente con el backend real, especialmente en los flujos críticos.
- **Técnica:** Pruebas con servidor de desarrollo real o mocks de HTTP de alto nivel (interceptores Angular).
- **Foco:** Formulario de compra de entrada, flujo de pago, acceso a rutas de administrador.

### Elementos de Prueba

| Capa | Elementos bajo prueba |
| :--- | :--- |
| Infraestructura | Migraciones Flyway, arranque del contexto Spring, configuración de datasource |
| Repositorios | Queries personalizadas contra PostgreSQL real, paginación y filtros |
| Managers de negocio | `ReservationManager`, `PaymentManager`, `CheckInManager`, `EventManager`, `UserManager` |
| Controladores REST | Todos los endpoints públicos y de administración (`/api/v1/...`) |
| Integración de correo | Servicio de envío de emails (SMTP simulado con MailHog o logs) |
| Frontend–Backend | Formularios de compra, checkout y panel de administración |

### Alcance de la Prueba

#### Elementos Incluidos
- Validación de migraciones de base de datos con Flyway sobre PostgreSQL real.
- Flujos completos Controller → Manager → Repository → DB para los módulos críticos.
- Contrato de la API REST: códigos HTTP, schemas JSON, manejo de errores.
- Integración del sistema de autenticación (SSO/OIDC) con el contexto de Spring Security.
- Envío de correos transaccionales (con servidor simulado).
- Comunicación entre el frontend Angular y los endpoints del backend.

#### Elementos Excluidos
- **Pruebas de rendimiento y carga:** No se valida el comportamiento bajo alta concurrencia.
- **Pruebas E2E con navegador (Selenium/Playwright):** Quedan fuera del alcance de este plan.
- **Transacciones reales con pasarelas de pago (Stripe, PayPal):** Solo se usan ambientes sandbox o mocks de alto nivel.
- **Pruebas de seguridad avanzadas (Penetration Testing):** No se auditan vulnerabilidades de red ni inyección SQL.
- **Pruebas de aceptación del usuario (UAT):** Son responsabilidad del docente y quedan para la validación final académica.

### Suposiciones y Restricciones

**Suposiciones**
- Existe un entorno con Docker disponible para levantar contenedores de Testcontainers.
- Las pruebas unitarias ya están aprobadas y con cobertura ≥ 85% antes de iniciar la integración.
- El esquema de base de datos se crea exclusivamente vía Flyway (sin scripts manuales).
- El servidor SMTP de pruebas puede reemplazarse por un servicio simulado (MailHog o modo log).

**Restricciones**
- Cada suite de integración debe completarse en menos de 15 minutos en el pipeline de CI.
- Las pruebas de integración deben ser reproducibles: no deben depender de datos preexistentes en la base de datos.
- Cada test debe limpiar su estado al finalizar (uso de `@Transactional` con rollback o truncado de tablas).

### Partes Interesadas

| Rol | Responsabilidades |
| :--- | :--- |
| Docente a cargo | Aprobación de criterios de aceptación académicos, validación del plan, supervisión general. |
| Test Lead | Coordinación del equipo, definición de la estrategia de integración, revisión de código y entregables. |
| Desarrolladores | Implementación de pruebas de integración, documentación de resultados, reporte de defectos. |

---

## Comunicación de las Pruebas

Se mantiene el mismo esquema de comunicación definido en el [[Plan-de-Pruebas-Unitarias]]:

- **Comunicación Interna:** WhatsApp (daily), Google Meet (planning/review/retrospective), GitHub Projects (seguimiento de tareas).
- **Comunicación Externa:** GitHub Wiki y Pull Requests para revisión del docente.
- **Resolución de Conflictos:** Gestionada por el Tech Lead; se escala al docente si no hay resolución.

| Punto de Comunicación | Propósito | Frecuencia | Responsable |
| :--- | :--- | :--- | :--- |
| Sprint Planning | Planificar pruebas de integración del sprint | Inicio de sprint | Tech Lead |
| Daily Standup | Sincronización de avances y bloqueos | Diario | Desarrollador |
| Sprint Review | Demostrar pruebas y resultados al docente | Fin de sprint | Tech Lead |
| Reporte de defectos | Reportar fallos de integración encontrados | Al encontrarse | Desarrollador |
| Reunión con docente | Validar avances y recibir feedback | 2 veces por semana | Tech Lead |

### Participantes del Equipo

1. Robert Edison Arisaca Mamani (Docente del curso)
2. Mestas Zegarra, Christian Raúl (Tech Lead)
3. Sequeiros Condori, Luis Gustavo (Desarrollador)
4. Jara Mamani, Mariel Alisson (Desarrollador)
5. Fernández Huarca, Rodrigo Alexander (Desarrollador)
6. Quispe Condori, Álvaro Raúl (Desarrollador)
7. Barrios Medina, Mathías Alonso (Desarrollador)

---

## Registro de Riesgos

La severidad se calcula como: **Probabilidad (1–5) × Impacto (1–5)**.

| N° | Riesgo | Prob. | Impacto | Severidad | Plan de Mitigación |
| :--- | :--- | :---: | :---: | :---: | :--- |
| 1 | Lentitud en el pipeline de CI por pruebas de integración pesadas | 4 | 3 | 12 | Paralelizar suites; usar caché de dependencias en GitHub Actions. |
| 2 | Datos de prueba inconsistentes que causan fallos intermitentes (flaky tests) | 4 | 4 | 16 | Usar `@Transactional` con rollback y fixtures controlados por prueba. |
| 3 | Incompatibilidades entre versiones de PostgreSQL en contenedor y producción | 2 | 4 | 8 | Fijar la versión del contenedor Testcontainers a la misma que producción (v15). |
| 4 | Dificultad para reproducir flujos de pago sin acceso a sandbox de Stripe | 3 | 3 | 9 | Implementar stubs de alto nivel para la pasarela; validar solo la lógica de orquestación. |
| 5 | Deuda técnica de pruebas unitarias incompletas que bloquea integración | 3 | 5 | 15 | Completar al 100% las pruebas unitarias de los módulos críticos antes de integrar. |

---

<!--
## Estructura de Pruebas

Las pruebas de integración se ubican separadas de las unitarias, siguiendo la convención de nomenclatura `*IntegrationTest`:

```
src/test/java/alfio/
├── integration/
│   ├── infrastructure/
│   │   ├── FlywayMigrationIntegrationTest.java      # Fase 1
│   │   └── SpringContextIntegrationTest.java        # Fase 1
│   ├── business/
│   │   ├── EventCreationIntegrationTest.java        # Fase 2
│   │   ├── ReservationFlowIntegrationTest.java      # Fase 2
│   │   ├── PaymentFlowIntegrationTest.java          # Fase 2
│   │   └── CheckInIntegrationTest.java              # Fase 2
│   └── api/
│       ├── EventControllerIntegrationTest.java      # Fase 3
│       ├── ReservationControllerIntegrationTest.java # Fase 3
│       └── AuthControllerIntegrationTest.java       # Fase 3
```

Todas las clases de integración extienden de una clase base común `BaseIntegrationTest` que configura el contenedor de PostgreSQL con Testcontainers y el contexto de Spring Boot:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---
-->

## Metodología

### Estrategia de Integración

Se adopta la estrategia **Big Bang incremental por módulos**, dividida en tres fases:

| Fase | Descripción | Módulos Involucrados |
| :--- | :--- | :--- |
| **Fase 1 – Infraestructura** | Arranque del contexto Spring con PostgreSQL real; validación de todas las migraciones Flyway. | DataSource, Flyway, Spring Context |
| **Fase 2 – Negocio Core** | Pruebas E2E parciales de los flujos críticos de negocio sobre la base de datos real. | EventManager, ReservationManager, PaymentManager, CheckInManager |
| **Fase 3 – API y Comunicación** | Validación del contrato REST y de la integración Frontend–Backend. | Controllers, Angular Services, Admin SPA |

Las fases son **secuenciales**: la Fase 2 solo comienza cuando la Fase 1 pasa al 100%, y la Fase 3 cuando la Fase 2 ha sido aprobada.

### Entregables de Prueba

- **Reporte de Ejecución de Pruebas de Integración:** Resultados generados por JUnit / GitHub Actions con detalle por test y suite.
- **Reporte de Cobertura de Integración:** Evidencia de la cobertura alcanzada en los flujos integrados (JaCoCo).
- **Matriz de Trazabilidad de Integración:** Documento que vincula cada prueba de integración con el flujo de negocio o requisito funcional que valida.
- **Registro de Defectos de Integración:** Lista de bugs encontrados durante la integración, con severidad, estado y responsable de corrección.

### Técnicas de Diseño de Prueba

- **Pruebas de Flujo (Use-Case driven):** Se diseñan pruebas que recorren el flujo completo de un caso de uso real (ej. crear evento → publicar → comprar entrada → recibir email → hacer check-in).
- **Pruebas de Contrato de API:** Se verifica que cada endpoint devuelva el schema JSON correcto, el código HTTP esperado y las cabeceras adecuadas.
- **Pruebas de Estado de Base de Datos:** Tras ejecutar una operación, se consulta directamente la base de datos para verificar que el estado persistido es el esperado.
- **Pruebas de Regresión de Integración:** Se re-ejecutan pruebas de integración previas ante cada cambio en los módulos integrados para detectar regresiones.

### Criterios de Finalización

El proceso de pruebas de integración se dará por concluido cuando:

1. **Todas las fases aprobadas:** Las tres fases de la estrategia Big Bang incremental pasan al 100% en el pipeline de CI.
2. **Sin defectos críticos abiertos:** No existen bugs de integración con severidad ≥ 12 (según la fórmula Probabilidad × Impacto) sin resolver.
3. **Entregables completos:** El reporte de ejecución, el reporte de cobertura y la matriz de trazabilidad están publicados en la Wiki.
4. **Aprobación del Tech Lead:** Cada suite debe estar revisada y aprobada mediante Pull Request por Christian Mestas.
5. **Pipeline verde:** El workflow de GitHub Actions finaliza sin errores en todas las suites de integración.

### Métricas

| Métrica | Descripción | Objetivo |
| :--- | :--- | :--- |
| Cobertura de flujos de negocio | % de flujos de negocio críticos cubiertos por al menos una prueba de integración | ≥ 90% |
| Tasa de éxito de pruebas | % de pruebas que pasan en el pipeline de CI | 100% en rama `main` |
| Tiempo de ejecución de suite | Tiempo total de ejecución de todas las pruebas de integración en CI | ≤ 15 min |
| Defectos de integración encontrados | Número de bugs identificados por fase | Monitoreo continuo |
| Flaky tests | Número de pruebas con resultados inconsistentes | 0 tolerados en `main` |

### Requisitos del Entorno de Pruebas

#### Infraestructura de CI/CD
- **Plataforma:** GitHub Actions con runners `ubuntu-latest`.
- **Disparadores:** Pull Request hacia `main` y push a `main`.
- **Paralelismo:** Las suites de Fase 1, 2 y 3 pueden ejecutarse en jobs paralelos dentro del mismo workflow.

#### Requisitos de Software
- **Java JDK 17** (distribución Temurin).
- **Gradle** (construcción y ejecución de pruebas de backend).
- **Node.js 22.x** (pruebas de frontend).
- **Docker** (obligatorio para Testcontainers).

#### Base de Datos y Servicios
- **PostgreSQL 15** en contenedor efímero gestionado por Testcontainers.
- **MailHog** (o modo log de Spring Mail) para captura de correos transaccionales en pruebas.
- **Servidor mock de pago:** Stub HTTP para Stripe/PayPal con WireMock o MockServer.

---

## Organización

### Roles y Responsabilidades

Matriz RACI para las actividades de pruebas de integración:

| Actividad Clave | Christian Mestas (Lead) | Mariel Jara | Gustavo Sequeiros | Mathias Barrios | Rodrigo Fernandez | Alvaro Quispe | Docente |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. Definición de Estrategia de Integración** | **A** | **R** | **R** | **C** | **C** | **C** | **I** |
| **2. Configuración de Testcontainers y entorno CI** | **A** | **R** | **C** | **C** | **C** | **C** | **I** |
| **3. Fase 1 – Infraestructura (Flyway, Spring Context)** | **A** | **C** | **R** | **R** | **C** | **C** | **I** |
| **4. Fase 2 – Negocio Core (Reservas, Pagos, Check-in)** | **A** | **R** | **R** | **R** | **R** | **R** | **I** |
| **5. Fase 3 – API y Frontend–Backend** | **A** | **R** | **R** | **C** | **R** | **R** | **I** |
| **6. Consolidación de Reportes y Matriz de Trazabilidad** | **R** | **C** | **C** | **C** | **C** | **C** | **I** |
| **7. Revisión y Cierre del Plan** | **A** | **C** | **C** | **C** | **C** | **C** | **I** |

---

## Cronograma

El cronograma de las pruebas de integración se gestiona mediante el Roadmap de GitHub del equipo, en continuidad con el cronograma del plan de pruebas unitarias. Las fases se ejecutan de manera secuencial dentro del sprint activo:

| Semana | Fase | Actividades |
| :--- | :--- | :--- |
| Semana 1 | Fase 1 – Infraestructura | Configuración de Testcontainers, validación de Flyway y arranque de Spring Context. |
| Semana 1–2 | Fase 2 – Negocio Core | Implementación de pruebas de flujos de reserva, pago y check-in. |
| Semana 2 | Fase 3 – API y Frontend | Pruebas de contrato REST e integración Frontend–Backend. Consolidación de reportes. |

> [!IMPORTANT]
> Las Fases son secuenciales. No se inicia la Fase 2 hasta que la Fase 1 tenga el pipeline verde. El reporte final debe estar publicado en la Wiki antes del cierre académico del sprint.


