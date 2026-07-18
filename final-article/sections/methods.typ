= Metodología <sec:methods>

Esta sección describe la metodología empleada para validar el sistema Alf.io. La estrategia de validación sigue un enfoque de pruebas multinivel, cubriendo los niveles unitario, integración, sistema y aceptación mediante una combinación de técnicas automatizadas y manuales.

== Resumen de la Estrategia de Pruebas

La estrategia general de pruebas se diseñó en torno a un enfoque basado en requisitos, impulsado por los requisitos funcionales derivados de los flujos de negocio principales de la plataforma: creación y configuración de eventos, reserva de entradas y procesamiento de pagos, registro de asistencia y gestión administrativa. Para cada requisito, se diseñaron casos de prueba en múltiples niveles de la pila de software, asegurando que los defectos se detecten lo antes posible en el ciclo de vida del desarrollo.

== Planificación y Gestión de Pruebas

Las actividades de prueba se organizaron siguiendo el marco Scrum, con sprints de dos semanas. Un Líder de Pruebas fue responsable de coordinar al equipo, definir la estrategia de pruebas y revisar los entregables. El equipo de desarrollo, compuesto por seis miembros, implementó los casos de prueba, documentó los resultados y reportó los defectos.

Se identificaron y gestionaron riesgos durante todo el proceso. Los riesgos más significativos incluyeron la familiaridad limitada del equipo con el ecosistema Spring Boot, plazos de entrega ajustados, conflictos de fusión por desarrollo paralelo e inestabilidad en el pipeline de integración continua. Las estrategias de mitigación incluyeron programación en pares para tareas complejas, priorización de módulos críticos, convenciones estrictas de gestión de ramas y validación automatizada en pull requests.

== Entorno e Infraestructura de Pruebas

El entorno de pruebas se configuró para replicar las condiciones de producción lo más fielmente posible. El backend, construido con Java 17 y Spring Boot 3.5, se desplegó contra una base de datos PostgreSQL (versiones 10, 15 y 16) gestionada mediante contenedores Docker efímeros usando Testcontainers. El frontend público, desarrollado en Angular 17, y el frontend de administración, construido con Lit 3 y Shoelace, se probaron contra la misma instancia del backend.

Todas las pruebas automatizadas se ejecutaron en ejecutores de GitHub Actions (ubuntu-latest). Se definieron dos flujos de trabajo principales: un flujo de trabajo de pull request activado en cada PR hacia main, y un flujo de trabajo de push activado después de las fusiones a main. El flujo de trabajo de pull request ejecutó verificaciones de formato, análisis estático, pruebas de backend, pruebas de frontend, pruebas de extremo a extremo y pruebas de rendimiento. El flujo de trabajo de push extendió esto ejecutando las pruebas de backend contra múltiples versiones de PostgreSQL en una estrategia de matriz para garantizar la compatibilidad de bases de datos. Los informes de cobertura se publicaron automáticamente en GitHub Pages, y se publicó un comentario con enlace a los informes en cada pull request.

Se utilizaron las siguientes herramientas en los diferentes niveles de prueba:

#figure(
  caption: [Herramientas y Marcos de Trabajo de Pruebas],
  placement: top,
  table(
    columns: (auto, auto, auto),
    align: (left, left, left),
    inset: (x: 8pt, y: 4pt),
    stroke: (x, y) => if y <= 1 { (top: 0.5pt) },
    fill: (x, y) => if y > 0 and calc.rem(y, 2) == 0 { rgb("#efefef") },

    table.header[Nivel de Prueba][Herramienta / Marco][Propósito],
    [Unitario (Backend)], [JUnit 5, Mockito, JaCoCo], [Aislamiento de componentes y cobertura],
    [Unitario (Frontend Público)], [Vitest, \@vitest/coverage-v8], [Validación de componentes y servicios],
    [Unitario (Frontend Admin)], [Vitest, \@vitest/coverage-v8], [Pruebas de componentes web],
    [Integración], [JUnit 5, Testcontainers, Flyway], [Interacción de capas con BD real],
    [Contrato API], [Spring REST Docs, OpenAPI], [Verificación de cumplimiento de endpoints],
    [E2E / Sistema], [Playwright 1.59.1], [Automatización de flujos en navegador],
    [Rendimiento], [k6], [Pruebas de carga, estrés y resistencia],
    [Análisis Estático], [Checkstyle, Spotless], [Aplicación de estilo de código],
    [CI / Automatización], [GitHub Actions], [Orquestación de pipeline y generación de informes],
  )
)

== Pruebas Unitarias

Las pruebas unitarias formaron la base del proceso de validación, enfocándose en componentes individuales de forma aislada. Para el backend, las pruebas se escribieron usando JUnit 5 con Mockito para simular dependencias externas, asegurando que cada unidad se probara independientemente de sus colaboradores. Las pruebas se centraron en los gestores de lógica de negocio, controladores REST, repositorios de datos, clases utilitarias y trabajos programados. Los componentes clave validados incluyeron el gestor de reservas, gestor de pagos, gestor de registro, gestor de eventos y gestor de billetera. Para cada componente, se probaron múltiples escenarios cubriendo operación normal, condiciones límite y manejo de errores.

Para el frontend, se desarrollaron dos conjuntos de pruebas separados. El frontend público, construido con Angular, utilizó Vitest como ejecutor de pruebas. Las pruebas validaron el renderizado de componentes, la comunicación de servicios mediante mocks HTTP (`HttpTestingController`), los guardias de ruta para control de acceso y la lógica de validación de formularios. El frontend de administración, basado en Lit 3 con componentes Shoelace, también se probó con Vitest. Dada la arquitectura de componentes web, las pruebas se centraron en funciones utilitarias puras, funciones auxiliares HTTP, servicios de negocio para campos adicionales, métodos de pago y configuración de eventos, y las fábricas y mocks utilizados para simular respuestas del servidor en el entorno de prueba.

El diseño de casos de prueba para pruebas unitarias empleó técnicas de caja negra que incluyen particionamiento de equivalencias, análisis de valores límite y tablas de decisión. Por ejemplo, las utilidades de conversión de cadenas se probaron en particiones que cubrían entradas nulas, indefinidas, numéricas, booleanas y de cadena, con atención especial a valores límite como cadenas vacías y valores falsy. Los métodos de servicio se probaron usando tablas de decisión para validar la lógica condicional, como la elección entre HTTP PUT y POST dependiendo de si un identificador de recurso estaba presente.

== Pruebas Funcionales

Las pruebas funcionales se ejecutaron manualmente contra un despliegue remoto común alojado en un clúster de Kubernetes. La imagen de la aplicación se construyó y publicó en el GitHub Container Registry mediante el pipeline de CI en cada push a main, asegurando que todos los miembros del equipo probaran contra la misma compilación.

Los casos de prueba se diseñaron utilizando técnicas de caja negra. Se aplicó particionamiento de equivalencias para agrupar los datos de entrada en clases válidas e inválidas, como categorías de entradas con diferentes precios y estados, o roles de usuario con diferentes niveles de permiso. El análisis de valores límite se utilizó para validar límites de longitud de campos de texto, niveles de stock de entradas y rangos de fechas. Las tablas de decisión modelaron reglas de negocio complejas, incluyendo las condiciones para mostrar el botón de descarga de entradas según la modalidad del evento, el estado del pago y factores temporales. Los diagramas de transición de estados capturaron las transiciones permitidas entre los estados de reserva a medida que los pagos se aprobaban, cancelaban o expiraban. Se diseñaron y ejecutaron un total de 18 casos de prueba funcionales, cubriendo edición de entradas, búsqueda de reservas, gestión de estado de pagos, disponibilidad de descarga de entradas, selección de método de pago, procesamiento de pagos offline y en sitio, gestión de pagos pendientes, auto-registro, validación de código QR, generación de credenciales y configuración del sistema para organizaciones, eventos, categorías de entradas, capacidad, impuestos, localización y creación de usuarios.

== Pruebas de Integración

Las pruebas de integración se diseñaron para validar la interacción entre las capas del sistema utilizando dependencias reales. La estrategia adoptada fue un enfoque incremental Big Bang, organizado en tres fases secuenciales. La Fase 1 validó la capa de infraestructura, confirmando que las migraciones de Flyway se aplicaban correctamente en un contenedor PostgreSQL real y que el contexto de la aplicación Spring Boot se cargaba sin errores. La Fase 2 probó los flujos de negocio principales — creación de eventos, reserva de entradas, procesamiento de pagos y registro de asistencia — ejercitando la cadena completa controlador-gestor-repositorio-base de datos. La Fase 3 validó el contrato de la API REST para 30 endpoints críticos, verificando que cada endpoint devolviera los códigos de estado HTTP esperados, los esquemas de respuesta JSON y el comportamiento de manejo de errores.

Todas las pruebas de integración utilizaron `@SpringBootTest` para cargar el contexto completo de la aplicación y Testcontainers para gestionar instancias PostgreSQL efímeras. Esto aseguró que cada ejecución de prueba comenzara desde un estado limpio de la base de datos y que las pruebas fueran reproducibles en diferentes entornos. Se implementó un mecanismo de limpieza de datos para eliminar el estado residual después de cada prueba, evitando fallos intermitentes causados por contaminación de datos.

Los 30 endpoints cubiertos en la Fase 3 se seleccionaron del flujo crítico de reserva-pago-registro, incluyendo endpoints para administración de eventos, gestión de reservas, inicialización y verificación de estado de pagos, manejo de webhooks para Stripe y Mollie, y el flujo completo de registro con soporte para confirmación de pago en sitio, operaciones por lote y reversión de registros.

== Pruebas de Contrato API

Las pruebas de contrato API se integraron en el pipeline de integración continua para garantizar que la superficie de la API REST se mantuviera consistente a medida que el código base evolucionaba. La especificación OpenAPI se generó a partir de los controladores Spring, y las diferencias de contrato se detectaron automáticamente en cada pull request. Se publicó un portal basado en Redoc junto con los informes de prueba, proporcionando una referencia legible para la superficie de la API. Esto permitió a los revisores identificar cambios disruptivos antes de que se fusionaran.

== Pruebas de Sistema

Las pruebas a nivel de sistema se dividieron en dos categorías complementarias: pruebas de extremo a extremo basadas en navegador y validación automatizada entre navegadores.

Las pruebas de extremo a extremo se implementaron con Playwright y siguieron el patrón Page Object Model para abstraer las interacciones de la interfaz de usuario en objetos de página reutilizables. El conjunto de pruebas cubrió todos los flujos de negocio críticos: compra completa de entradas desde el listado de eventos hasta la selección, pago y confirmación; creación y configuración de eventos desde el panel de administración; registro de asistentes mediante escaneo de código QR; gestión de reservas incluyendo cancelaciones y reembolsos; generación de entradas digitales en formato PDF; autenticación y control de acceso basado en roles para administradores, organizadores y operadores de registro; comportamiento de la sala de espera bajo alta demanda; y aplicación de códigos de descuento y promociones. Las pruebas se parametrizaron para ejecutarse tanto en Chromium como en Firefox en paralelo, proporcionando validación de compatibilidad entre navegadores.

Los datos de prueba se generaron dinámicamente utilizando helpers de API para crear y configurar eventos programáticamente antes de cada ejecución de prueba y para limpiar el estado residual después de la ejecución. Esto aseguró el aislamiento y la idempotencia de las pruebas. Se capturaron grabaciones de video y trazas en caso de fallo de prueba para facilitar la depuración.

== Pruebas de Rendimiento

Las pruebas de rendimiento se realizaron utilizando k6, una herramienta de pruebas de carga de código abierto. Las pruebas se ejecutaron contra una instancia completamente desplegada de la aplicación, incluyendo el backend compilado, los assets del frontend construidos y una base de datos PostgreSQL, todo orquestado dentro del pipeline de CI. Se definieron cuatro escenarios de rendimiento, basados en los patrones de uso esperados de un evento mediano a grande:

#figure(
  caption: [Escenarios de Pruebas de Rendimiento],
  placement: top,
  table(
    columns: (auto, auto, auto, auto),
    align: (left, center, center, center),
    inset: (x: 8pt, y: 4pt),
    stroke: (x, y) => if y <= 1 { (top: 0.5pt) },
    fill: (x, y) => if y > 0 and calc.rem(y, 2) == 0 { rgb("#efefef") },

    table.header[Escenario][Usuarios Virtuales][Duración][Objetivo de Métrica],
    [Prueba de Carga], [50], [5 min], [Tiempo de respuesta < 2s],
    [Prueba de Estrés], [500+], [5 min], [Degradación gradual],
    [Prueba de Resistencia], [100], [30 min], [Sin fugas de memoria],
    [Prueba de Pico], [200], [1 min de incremento], [Rendimiento > 100 RPS],
  )
)

La prueba de carga simuló condiciones normales de tráfico. La prueba de estrés aumentó progresivamente la carga más allá de la capacidad esperada del sistema para identificar puntos de quiebre y evaluar el comportamiento de degradación. La prueba de resistencia mantuvo una carga sostenida durante un período prolongado para detectar fugas de memoria o degradación del rendimiento. La prueba de pico aplicó un aumento repentino de tráfico para simular la apertura de venta de entradas para un evento popular.

== Análisis Estático y Calidad del Código

El análisis estático se realizó utilizando Checkstyle para código fuente Java y archivos de prueba. El trabajo de checkstyle se integró en el pipeline de CI y se ejecutó en paralelo con los conjuntos de pruebas funcionales. Además, se utilizó Spotless para aplicar un formato de código consistente tanto en código Java como en frontend. Las correcciones de formato se confirmaron y enviaron automáticamente de vuelta a la rama de características durante el flujo de trabajo de pull request, asegurando que la rama principal siempre cumpliera con las convenciones de codificación del proyecto.

== Integración Continua y Automatización

Todo el conjunto de pruebas se automatizó a través de GitHub Actions. El flujo de trabajo de pull request consistió en seis trabajos paralelos: verificación de formato, pruebas de backend, análisis de checkstyle, pruebas de frontend, pruebas de extremo a extremo y pruebas de rendimiento. Todos los trabajos se ejecutaron concurrentemente después de que el trabajo de formato se completara exitosamente. El flujo de trabajo de push ejecutó los mismos conjuntos pero también ejecutó pruebas de backend contra las versiones 10, 15 y 16 de PostgreSQL en una estrategia de matriz, validando la compatibilidad de la base de datos entre versiones.

Después de que todos los trabajos se completaran exitosamente, un trabajo de despliegue de cobertura recopiló los informes generados de cada trabajo y los publicó como un panel unificado en GitHub Pages. El panel agregó informes de cobertura de JaCoCo para el backend y de Vitest para ambas aplicaciones frontend, junto con el informe de pruebas de Playwright, el informe de rendimiento de k6, el informe de Checkstyle, el informe de pruebas de integración de JUnit y el portal de contrato OpenAPI.

== Gestión de Defectos y Trazabilidad

Los defectos encontrados durante las pruebas se registraron como issues de GitHub con etiquetas de severidad determinadas por el producto de probabilidad e impacto, cada una calificada en una escala del 1 al 5. Se definió un umbral de severidad para cada nivel de prueba; los defectos que superaban el umbral debían resolverse antes de que la fase de prueba pudiera considerarse completa.

La trazabilidad se mantuvo a través de una matriz que vinculaba cada caso de prueba con su requisito funcional correspondiente. Para las pruebas de integración, la matriz vinculaba cada prueba a uno de los 30 endpoints críticos de la API. Para las pruebas de sistema, la matriz conectaba cada prueba con el flujo de negocio que validaba. Esto aseguró que cada requisito estuviera cubierto por al menos un caso de prueba y que ninguna funcionalidad crítica quedara sin validar.

== Criterios de Aceptación y Finalización

Cada nivel de prueba definió criterios de finalización específicos. Para las pruebas unitarias, el criterio principal fue una cobertura de código agregada de al menos 85%, medida en instrucciones y sentencias, combinada con la ausencia de defectos abiertos de alta severidad. Para las pruebas de integración, se requirió que las tres fases de la estrategia incremental pasaran al 100% en el pipeline de CI. Para las pruebas de sistema, se requirió una tasa de aprobación mínima del 95% para el conjunto E2E, y las métricas de rendimiento debían cumplir los objetivos definidos para tiempo de respuesta, rendimiento y tasa de error bajo carga normal.

Todas las fases de prueba requirieron aprobación a través del proceso de pull request, con verificaciones de CI exitosas y revisión formal por parte del Líder de Pruebas. Al completar cada fase, los entregables correspondientes — informes de ejecución, informes de cobertura, matrices de trazabilidad y registros de defectos — se publicaron en la wiki del proyecto.
