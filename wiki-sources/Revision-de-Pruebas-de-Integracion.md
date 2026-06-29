# Reporte de Revisión y Análisis de Brechas de Pruebas de Integración

## 1. Información General
* **Proyecto:** alf.io (Plataforma de gestión de eventos y venta de entradas)
* **Documento de Referencia:** [[Plan-de-Pruebas-de-Integración.md]](file:///home/cricro/projects/catarinas-ps-2026/alf.io/wiki/Plan-de-Pruebas-de-Integraci%C3%B3n.md)
* **Fecha de Evaluación:** 24 de junio de 2026
* **Estado de la Evaluación:** Finalizado
* **Objetivo:** Auditar la base de código de pruebas del sistema alf.io en la rama `main` y compararla críticamente contra el cronograma, objetivos y alcance de las pruebas de integración definidos en el Plan de Pruebas de Integración, listando todos los vacíos de cobertura y pruebas faltantes.

---

## 2. Resumen Ejecutivo
Tras realizar un análisis estático detallado del directorio de pruebas `src/test/java/` y la configuración de pipelines en `.github/workflows/`, se determinó que **el sistema presenta brechas críticas respecto al Plan de Pruebas de Integración**. 

De las **20 tareas principales** programadas en el plan de pruebas:
* **Completadas:** 4 tareas (flujos esenciales de `EventManager`, `TicketReservationManager`, `SubscriptionManager`, `GroupManager` y `CheckInManager`).
* **Parcialmente Completadas / Desviadas:** 4 tareas (configuración base de contenedores, pruebas de API Admin, y flujos de pago con Stripe).
* **Faltantes / Sin Implementar:** 12 tareas (todos los repositorios excepto uno, integraciones con PayPal y Mollie, webhooks asociados, scripts de post-migración, integridad referencial en BD, tests específicos de arranque, y toda la integración frontend-backend).

### Cuadro de Estado General de Cobertura de Integración
| Fase de Integración | Tareas Planificadas | Implementadas | Parciales / Desviadas | Faltantes | % de Cumplimiento de Fase |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Fase 1: Infraestructura y Repositorios** | 10 | 0 | 2 | 8 | **10.0%** |
| **Fase 2: Negocio Core** | 6 | 3 | 2 | 1 | **58.3%** |
| **Fase 3: API y Comunicación** | 4 | 1 | 0 | 3 | **25.0%** |
| **Total** | **20** | **4** | **4** | **12** | **30.0%** |

---

## 3. Análisis Detallado por Fases y Tareas

A continuación se presenta un desglose de cada tarea listada en el cronograma del plan, contrastando lo planificado contra el estado real en la base de código.

### Fase 1: Infraestructura y Base de Datos (Tareas 1–10)

#### Tarea 1: Configurar `BaseIntegrationTest` con Testcontainers (PostgreSQL 15) y `@DynamicPropertySource`
* **Planificación:** Crear una clase base abstracta de prueba (`BaseIntegrationTest`) que configure el contexto Spring Boot, levante un contenedor Docker PostgreSQL 15 a través de Testcontainers y use `@DynamicPropertySource` para inyectar dinámicamente las propiedades de conexión a la base de datos.
* **Estado Actual:** :warning: **Parcialmente Completado / Desviado**.
  * No existe una clase base abstracta configurada con Spring Boot y `@DynamicPropertySource`.
  * La inyección de propiedades y el inicio del contenedor PostgreSQL se resuelven a través de la clase de configuración [BaseTestConfiguration.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/BaseTestConfiguration.java#L72-L94) utilizando métodos `@Bean` que retornan un `RefreshableDataSource`.
  * La versión por defecto del contenedor en la configuración es **PostgreSQL 10** (`BaseTestConfiguration.java` L76), y solo usa versiones superiores si se especifica la propiedad del sistema `-Dpgsql.version`.
  * Existe la clase [BaseIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/util/BaseIntegrationTest.java) en `alfio.util`, pero carece de anotaciones de Spring; es simplemente una clase con métodos helper estáticos (por ejemplo, para transferir eventos u organizaciones entre pruebas).
  * Los tests de integración reales utilizan una anotación personalizada `@AlfioIntegrationTest` ([AlfioIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/test/util/AlfioIntegrationTest.java)) combinada con `@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class})`.

#### Tarea 2: Configurar pipeline de GitHub Actions para suites de integración
* **Planificación:** Modificar la integración continua (CI) en GitHub Actions para paralelizar la ejecución de las fases 1, 2 y 3 en distintos jobs, configurando caché para las imágenes de Docker utilizadas por Testcontainers.
* **Estado Actual:** :x: **Faltante**.
  * Los workflows activos ([test-pr.yml](file:///home/cricro/projects/catarinas-ps-2026/alf.io/.github/workflows/test-pr.yml#L64-L65) y [test-push.yml](file:///home/cricro/projects/catarinas-ps-2026/alf.io/.github/workflows/test-push.yml#L50-L51)) ejecutan de forma lineal y secuencial un único comando `./gradlew test jacocoTestReport`.
  * No existen jobs de integración divididos en el workflow de CI, ni se ha configurado caching de Docker específico para optimizar Testcontainers.

#### Tarea 3: Validar aplicación de 195 migraciones Flyway (V1–V206) sobre PostgreSQL
* **Planificación:** Crear un test que levante el contenedor PostgreSQL y aplique programáticamente las migraciones de Flyway de principio a fin, validando la ausencia de fallos en los esquemas.
* **Estado Actual:** :warning: **Incompleto / Desactivado**.
  * Existe el archivo [MigrationValidatorTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/MigrationValidatorTest.java), pero está desactivado por defecto en su método `@BeforeAll` (`Assumptions.assumeTrue("true".equals(System.getenv("MIGRATION_TEST")))`).
  * En lugar de validar Flyway programáticamente, este test intenta descargar la versión estable y la dev de alf.io de registros de Docker e inicializarlas en conjunto con un contenedor PostgreSQL 10 para comprobar la respuesta HTTP de salud.
  * No hay una validación integrada y automatizada en el ciclo de pruebas normales de CI que verifique las migraciones de Flyway directamente desde los scripts locales del proyecto.

#### Tarea 4: Verificar arranque del contexto Spring Boot con datasource de Testcontainers
* **Planificación:** Crear un test unitario/integración básico (por ejemplo, `SpringContextIntegrationTest`) encargado de comprobar que el contexto de la aplicación carga sin problemas.
* **Estado Actual:** :x: **Faltante**.
  * No existe una prueba dedicada a validar exclusivamente el arranque de contexto. Aunque otros tests de integración que levantan Spring Boot validan esto de forma implícita, no hay un test aislado de sanidad para tal fin.

#### Tarea 5: Test `EventRepository` y `TicketRepository` – CRUD y queries contra PostgreSQL real
* **Planificación:** Crear pruebas de integración para interactuar con la base de datos real para `EventRepository` y `TicketRepository`.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * [EventRepositoryIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/repository/EventRepositoryIntegrationTest.java) está implementado y prueba operaciones CRUD y consultas complejas (como estadísticas) de `EventRepository` contra la base de datos real.
  * [TicketRepositoryTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/repository/TicketRepositoryTest.java) es un **test unitario** convencional que utiliza Mockito mocks (`mock(NamedParameterJdbcTemplate.class)`) para simular la base de datos. **No existen pruebas de integración reales** para `TicketRepository`.

#### Tarea 6: Test `TicketReservationRepository` y `ConfigurationRepository` – persistencia y paginación
* **Planificación:** Pruebas de integración reales de consultas y persistencia.
* **Estado Actual:** :x: **Faltante**.
  * Los archivos `TicketReservationRepositoryTest.java` y `ConfigurationRepositoryTest.java` son estrictamente pruebas unitarias basadas en Mockito mocks. No se conectan con Testcontainers.

#### Tarea 7: Test `AdditionalServiceRepository` y `PromoCodeDiscountRepository`
* **Planificación:** Validar la persistencia e integridad de datos complementarios de servicios adicionales y descuentos.
* **Estado Actual:** :x: **Faltante**.
  * Los tests para estas clases (`AdditionalServiceRepositoryTest.java` y `PromoCodeDiscountRepositoryTest.java`) son unitarios y mockean la capa de persistencia.

#### Tarea 8: Validar scripts de post-migración y vistas de estadísticas
* **Planificación:** Validar scripts `afterMigrateApplied` y la coherencia de vistas estadísticas dinámicas.
* **Estado Actual:** :x: **Faltante**.
  * No se encontraron pruebas de integración encargadas de comprobar la coherencia del estado post-migración de la base de datos o las vistas SQL.

#### Tarea 9: Test de integridad referencial: foreign keys y constraints
* **Planificación:** Validar el comportamiento de las restricciones de base de datos (por ejemplo, impidiendo la eliminación de organizaciones con eventos activos).
* **Estado Actual:** :x: **Faltante**.
  * No existen tests de integración diseñados para validar llaves foráneas o restricciones a nivel base de datos real.

#### Tarea 10: Test `UserRepository` y `OrganizationRepository` – operaciones de usuarios y organizaciones
* **Planificación:** Validar operaciones críticas de base de datos para perfiles de administración y empresas.
* **Estado Actual:** :x: **Faltante**.
  * Ambos repositorios carecen de cobertura de integración. `UserRepositoryTest.java` es una prueba unitaria con Mockito.

---

### Fase 2: Negocio Core (Tareas 11–16)

#### Tarea 11: Test `EventManager` – flujo completo de creación y configuración de eventos
* **Planificación:** Validar el gestor de eventos de extremo a extremo contra la base de datos real.
* **Estado Actual:** :white_check_mark: **Completado**.
  * [EventManagerIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/manager/EventManagerIntegrationTest.java) es una clase de prueba extensa (más de 2,800 líneas) que interactúa con la base de datos en contenedor real y cubre flujos completos de creación, actualización y edición de eventos y sus categorías.

#### Tarea 12: Test `TicketReservationManager` – flujo de reserva con cálculo de precios e impuestos
* **Planificación:** Pruebas completas de reserva de entradas y cálculos financieros contra el esquema de base de datos real.
* **Estado Actual:** :white_check_mark: **Completado**.
  * [TicketReservationManagerIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/manager/TicketReservationManagerIntegrationTest.java) valida con éxito los flujos de reservas, expiraciones, límites de tickets y cálculos de impuestos en la base de datos.

#### Tarea 13: Test `PaymentManager` – orquestación de pago con stubs de Stripe/PayPal
* **Planificación:** Validar la orquestación de pagos integrando stubs y contenedores mock de pasarelas de pago.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * `PaymentManagerTest.java` es un test unitario que simula todos los comportamientos.
  * [StripeReservationFlowIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/controller/api/v2/user/reservation/StripeReservationFlowIntegrationTest.java) valida de forma real el flujo de pago con Stripe apoyándose en un contenedor Docker de `stripe-mock` configurado en `BaseTestConfiguration.java`.
  * **No existen pruebas de integración reales para PayPal o Mollie**. Las pasarelas secundarias carecen de validación de orquestación de extremo a extremo.

#### Tarea 14: Test `NotificationManager` y `CheckInManager` – emails transaccionales y check-in
* **Planificación:** Validar check-in de entradas y envío de notificaciones por correo electrónico en base a datos reales.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * El módulo de check-in está cubierto adecuadamente por [CheckInManagerIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/manager/CheckInManagerIntegrationTest.java).
  * **No existe un test de integración dedicado para `NotificationManager`**. Su flujo se valida indirectamente a través del envío de notificaciones en otros tests de negocio, pero no hay cobertura aislada que certifique la persistencia y control de mensajes de correo electrónico.

#### Tarea 15: Test `SubscriptionManager`, `PromoCodeRequestManager` y `GroupManager`
* **Planificación:** Pruebas de integración sobre base de datos para suscripciones de eventos, códigos de descuento y gestión de agrupaciones.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * Las suscripciones están probadas por [SubscriptionManagerIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/manager/SubscriptionManagerIntegrationTest.java) y [SubscriptionReservationManagerIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/manager/SubscriptionReservationManagerIntegrationTest.java).
  * Los grupos están cubiertos por [GroupManagerIntegrationTest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/manager/GroupManagerIntegrationTest.java).
  * **No existe un test de integración aislado para `PromoCodeRequestManager`**. Su funcionamiento se valida de forma secundaria dentro de tests de flujo de reserva con descuentos, pero carece de pruebas directas enfocadas en su CRUD y ciclo de vida de persistencia.

#### Tarea 16: Test E2E `NormalFlowE2ETest` – flujo completo reserva→pago→email→check-in
* **Planificación:** Configurar un flujo de prueba extremo a extremo que simule un cliente comprando una entrada hasta hacer check-in.
* **Estado Actual:** :warning: **Incompleto / Desactivado**.
  * La clase de prueba [NormalFlowE2ETest.java](file:///home/cricro/projects/catarinas-ps-2026/alf.io/src/test/java/alfio/e2e/NormalFlowE2ETest.java) está implementada usando Selenium WebDriver.
  * Sin embargo, está decorada con `@EnabledIfEnvironmentVariable(named = "ALFIO_RUN_E2E", matches = "true")`. Dado que los archivos YAML de GitHub Actions no configuran esta variable de entorno para las ramas principales, el test de extremo a extremo está completamente desactivado en la práctica en los pipelines de CI.

---

### Fase 3: API y Comunicación (Tareas 17–20)

#### Tarea 17: Test contrato API admin: `EventApiController`, `ConfigurationApiController`, `AdminReservationApiController`
* **Planificación:** Asegurar la consistencia de los contratos y esquemas JSON devueltos por la API administrativa REST mediante pruebas MockMvc.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * Los controladores `EventApiController` y `ConfigurationApiController` están cubiertos por `EventApiControllerIntegrationTest.java` y `ConfigurationApiControllerIntegrationTest.java`.
  * El controlador de reservas administrativas **NO** cuenta con cobertura de integración; `AdminReservationApiControllerUnitTest.java` es una prueba unitaria pura.

#### Tarea 18: Test webhooks de pago: Stripe, PayPal y Mollie
* **Planificación:** Validar que los controladores encargados de procesar respuestas asíncronas de las pasarelas (webhooks) reciban, validen y persistan los estados de pago correctos.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * Los webhooks de Stripe se integran satisfactoriamente a través de `StripeReservationFlowIntegrationTest.java`.
  * **No existen pruebas de integración para los webhooks de PayPal (`PayPalCallbackController`) ni Mollie (`MolliePaymentWebhookController`)**.

#### Tarea 19: Test contrato API pública V2: `EventApiV2Controller`, `ReservationApiV2Controller`, `TicketApiV2Controller`
* **Planificación:** Pruebas de integración sobre controladores de API pública versión 2.
* **Estado Actual:** :warning: **Parcialmente Completado**.
  * `ReservationApiV2Controller` se encuentra correctamente cubierto por `ReservationApiV2ControllerIntegrationTest.java`.
  * Los controladores de eventos públicos (`EventApiV2Controller`) y de visualización y edición de tickets (`TicketApiV2Controller`) **carecen por completo de pruebas de integración**.

#### Tarea 20: Test integración frontend Angular (reserva/pago) + Lit admin + consolidación
* **Planificación:** Configurar pruebas de integración que validen que el frontend de reserva/pago y el panel de administración Lit se comunican con los endpoints del servidor.
* **Estado Actual:** :x: **Faltante**.
  * La suite de pruebas de frontend está limitada a pruebas unitarias y de componentes aisladas (Vitest, Karma, Jasmine).
  * No hay configuración de Playwright activa en la rama `main` para realizar pruebas reales de interfaz integradas con el backend. (Nota: Existe una rama `ci/configure-playwright` en Git, pero su código no ha sido fusionado a la rama productiva).

---

## 4. Características sin Cobertura de Pruebas de Integración (Brechas)

De acuerdo con el alcance e inclusiones definidas en el plan de pruebas del wiki, se identifican las siguientes áreas de riesgo debido a la total ausencia de pruebas de integración:

### 1. Repositorios de Acceso a Datos (Capa DB)
* **Descripción:** A excepción de `EventRepository`, todas las consultas e interacciones a bajo nivel de la base de datos PostgreSQL se prueban únicamente a nivel unitario simulando el cliente JDBC con Mockito.
* **Características Afectadas:** 
  * Asignación y reserva masiva de asientos/tickets (`TicketRepository`).
  * Persistencia de códigos de descuento complejos y sus restricciones de uso (`PromoCodeDiscountRepository`).
  * Almacenamiento y recuperación de campos de formulario de compra dinámicos (`PurchaseContextFieldRepository`).
  * Consultas y ciclos de facturación de servicios adicionales (`AdditionalServiceRepository`).
  * Creación y gestión de perfiles administrativos (`UserRepository` y `OrganizationRepository`).
* **Riesgo:** Alta probabilidad de fallos silenciosos en producción por incompatibilidades sintácticas en consultas SQL, errores de mapeo JDBC, incoherencias de tipos de datos, o problemas de concurrencia no detectados.

### 2. Integración de Pasarelas de Pago Secundarias (PayPal y Mollie)
* **Descripción:** La plataforma soporta activamente Stripe, PayPal y Mollie como proveedores de pago. Sin embargo, solo Stripe está cubierto por stubs reales en un entorno de integración.
* **Características Afectadas:**
  * Redirección, autorización y captura de pagos de PayPal (`PayPalCallbackController`).
  * Verificación de firmas y estados de cobro asíncronos de Mollie (`MolliePaymentWebhookController`).
  * Orquestación de pasarelas de pago múltiples (`PaymentManager`).
* **Riesgo:** Defectos de integración no detectados en los flujos de cobro de clientes que usen PayPal o Mollie (por ejemplo, transacciones aprobadas en la pasarela que no actualizan el estado a `ACQUIRED` en alf.io).

### 3. API REST y Contratos de Servicio (Endpoints)
* **Descripción:** Varias interfaces públicas y administrativas de tipo REST no cuentan con validación de contrato.
* **Características Afectadas:**
  * Endpoints públicos de listados de eventos y disponibilidad de asientos (`EventApiV2Controller`).
  * Edición y descarga de tickets por parte del cliente final (`TicketApiV2Controller`).
  * Endpoints de reservas desde la interfaz de administración (`AdminReservationApiController`).
* **Riesgo:** Romper la API para aplicaciones de terceros debido a cambios no controlados en los esquemas de respuesta JSON o códigos de estado HTTP incorrectos.

### 4. Integridad Referencial y Post-Migraciones de BD
* **Descripción:** La base de datos alf.io cuenta con cerca de 200 migraciones Flyway y múltiples scripts `afterMigrate` y vistas agregadas de estadísticas.
* **Características Afectadas:**
  * Constraints y Foreign Keys de tablas de alta interconexión (`ticket`, `ticket_category`, `event`, `organization`, `ba_user`).
  * Vistas estadísticas utilizadas en el panel del organizador.
* **Riesgo:** Corrupción de datos por borrados en cascada no controlados o fallos catastróficos en el despliegue al aplicar migraciones inconsistentes en producción.

### 5. Integración Frontend-Backend Real (Playwright/E2E)
* **Descripción:** No hay pruebas funcionales reales que validen la aplicación Angular o Lit comunicándose con endpoints Java.
* **Características Afectadas:**
  * Flujo de compra web público (checkout Angular).
  * Panel de control web del administrador (Lit SPA).
* **Riesgo:** Errores de CORS, inconsistencias en los nombres de campos serializados, o mal comportamiento de JavaScript que impida a los usuarios finalizar reservas reales en producción.

---

## 5. Conclusiones y Recomendaciones

### Conclusión Principal
La cobertura de pruebas de integración de alf.io es **insuficiente** y presenta una **desviación de desarrollo del 70%** respecto a la especificación acordada en [[Plan-de-Pruebas-de-Integración.md]](file:///home/cricro/projects/catarinas-ps-2026/alf.io/wiki/Plan-de-Pruebas-de-Integraci%C3%B3n.md). Aunque la lógica de negocio nuclear (`EventManager` y `TicketReservationManager`) está bien resguardada, el acceso a datos en repositorios individuales, las integraciones de pago (PayPal/Mollie) y los contratos REST secundarios están expuestos a fallos graves no detectados automáticamente en la integración continua.

### Recomendaciones de Mitigación
1. **Migración de Repositorios a Integration Tests:** Transformar de manera prioritaria `TicketRepositoryTest`, `TicketReservationRepositoryTest` y `UserRepositoryTest` de pruebas unitarias con Mockito a pruebas de integración con `@AlfioIntegrationTest` y base de datos real.
2. **Implementación de Stubs para PayPal y Mollie:** Integrar WireMock o configurar stubs equivalentes a `stripe-mock` para validar la orquestación y recepción de callbacks/webhooks en `PayPalCallbackController` y `MolliePaymentWebhookController`.
3. **Activar E2E en CI:** Establecer la variable de entorno `ALFIO_RUN_E2E: true` en el pipeline de GitHub Actions ([test-push.yml](file:///home/cricro/projects/catarinas-ps-2026/alf.io/.github/workflows/test-push.yml)) para obligar la ejecución de `NormalFlowE2ETest` en cada PR o Push hacia `main`.
4. **Completar Integración de Playwright:** Fusionar los cambios de la rama `ci/configure-playwright` a `main` e implementar la suite E2E de interfaz de usuario de acuerdo con el plan del sprint anterior.
