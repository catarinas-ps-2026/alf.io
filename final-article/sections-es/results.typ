= Resultados <sec:results>

== Componentes Evaluados

La estrategia de pruebas cubrió todos los componentes principales del sistema alf.io en tres aplicaciones e infraestructura de soporte @ossquality2007. La Tabla V resume los componentes y sus conteos de pruebas.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, center, center, center),
    table.header([*Componente*], [*Tecnología*], [* conteo de Pruebas*], [*Tasa de Aprobación*]),
    [Backend], [Java 17, Spring Boot 3.5.x], [2,290], [100%],
    [Frontend Público], [Angular 17], [487], [100%],
    [Frontend Admin], [Lit 3], [167], [100%],
    [Integración Backend], [JUnit + Testcontainers], [354], [100%],
    [Pruebas E2E], [Playwright], [78], [100%],
    [Pruebas Funcionales], [Manual], [120+], [97.5%],
  ),
  caption: [Componentes evaluados y sus resultados.],
) <tab:components>

== Resultados de Pruebas Unitarias

La suite de pruebas unitarias ejecutó 2,290 casos de prueba en las tres aplicaciones, alcanzando una tasa de aprobación del 100%. El backend contribuyó con 1,636 pruebas, el frontend público con 487 pruebas y el frontend de administración con 167 pruebas.

La Figura 2 muestra el reporte de cobertura de código del backend. La cobertura de código agregada alcanzó el 85.76% para instrucciones/sentencias, superando el objetivo del 85% @coverage2015.

#figure(
  image("../diagrams/unit-tests/backend_cov.png", width: 100%),
  caption: [Reporte de cobertura de código del backend (JaCoCo).],
) <fig:backend_cov>

Las Figuras 3 y 4 muestran los reportes de cobertura del frontend. El frontend de administración alcanzó el 94.03% de cobertura de sentencias, mientras que el frontend público alcanzó el 85.60%.

#figure(
  image("../diagrams/unit-tests/frontend_admin_cov.png", width: 100%),
  caption: [Cobertura de código del frontend de administración (Vitest).],
) <fig:admin_cov>

#figure(
  image("../diagrams/unit-tests/frontend_public_cov.png", width: 100%),
  caption: [Cobertura de código del frontend público (Vitest).],
) <fig:public_cov>

== Resultados de Pruebas de Integración

Las pruebas de integración ejecutaron 442 casos de prueba en cuatro componentes @testcontainers2024. Todas las pruebas alcanzaron tasas de aprobación del 100%. La Figura 5 muestra los resultados de las pruebas de integración del backend.

#figure(
  image("../diagrams/integration-tests/integration_tests_backend.png", width: 100%),
  caption: [Resultados de pruebas de integración del backend (JUnit).],
) <fig:integration_backend>

La suite de pruebas de integración validó 30 endpoints obligatorios cubriendo el flujo crítico: Reserva, Pago y Check-In. La Figura 6 muestra los resultados de las pruebas Playwright @playwright2024.

#figure(
  image("../diagrams/integration-tests/integration_tests_playwright.png", width: 100%),
  caption: [Resultados de pruebas Playwright (Chromium + Firefox).],
) <fig:integration_playwright>

La validación de contratos de API confirmó la estabilidad entre la implementación actual y el descriptor de referencia. La Figura 7 muestra el portal de validación de contratos de API.

#figure(
  image("../diagrams/integration-tests/integration_tests_redoc.png", width: 100%),
  caption: [Portal de validación de contratos API (Redoc/OpenAPI).],
) <fig:integration_redoc>

== Resultados de Pruebas Funcionales

Las pruebas funcionales cubrieron casos de prueba manuales en los tres módulos seleccionados: Manager, Controller/API y Flujos de Reserva @myers2011. La tasa de aprobación global de pruebas funcionales fue del 97.5%. La Tabla VI resume los resultados por módulo.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, center, center, left),
    table.header([*Módulo*], [*Casos de Prueba*], [*Tasa de Aprobación*], [*Defectos Encontrados*]),
    [Manager], [15], [93%], [1],
    [Controller/API], [20], [95%], [1],
    [Flujos de Reserva], [25], [96%], [1],
    [Edición de Tickets], [11], [82%], [3],
    [Configuración de Eventos], [7], [57%], [3],
    [Otros Módulos], [42], [100%], [0],
  ),
  caption: [Resultados de pruebas funcionales por módulo.],
) <tab:functional_results>

== Defectos Encontrados y Corregidos

El proceso de pruebas identificó y documentó defectos en todos los niveles de prueba @iso29119. La Tabla VII lista los defectos descubiertos durante las pruebas funcionales y los commits que los corrigieron.

#figure(
  table(
    columns: (auto, auto, auto, auto, auto),
    align: (left, left, left, left, left),
    table.header([*ID de Prueba*], [*Módulo*], [*Defecto*], [*Commit*], [*Estado*]),
    [CPF-01-003], [Edición de Tickets], [Error 500 con nombres de 254 caracteres], [7e524f8], [Corregido],
    [CPF-01-006], [Edición de Tickets], [Sistema acepta números en campo nombre], [99f17a7], [Corregido],
    [CPF-01-011], [Edición de Tickets], [Validación de longitud de email no aplicada], [dc1341a], [Corregido],
    [CPF-03-006], [Estado de Reserva], [Botón visible con datos incompletos], [7e524f8], [Corregido],
    [CPF-09-006], [Config. Organización], [Error SQL expuesto con nombres de 256 caracteres], [99f17a7], [Corregido],
    [CPF-10-001], [Config. Evento], [Bug de scroll en selector de fechas], [dc1341a], [Corregido],
    [CPF-10-006], [Config. Evento], [Guardado silencioso deshabilitado sin retroalimentación], [7e524f8], [Corregido],
    [CPF-10-007], [Config. Evento], [Códigos ocultos duplicados permitidos], [99f17a7], [Corregido],
    [CPF-14-004], [Localización], [Advertencia de zona horaria no mostrada], [dc1341a], [Corregido],
  ),
  caption: [Defectos descubiertos en pruebas funcionales y sus correcciones.],
) <tab:defects>

== Resultados de Pruebas de Sistema

La Figura 8 muestra los resultados de pruebas de rendimiento K6 @k6performance2024. Las pruebas de rendimiento ejecutaron 54,176 peticiones HTTP contra el flujo de reserva, alcanzando un throughput de 190.43 peticiones por segundo. El tiempo de respuesta mediano fue de 745 milisegundos, cumpliendo con el objetivo de menos de 2 segundos. Sin embargo, la tasa de error del 8.43% excedió el objetivo de menos del 1% bajo condiciones de carga extrema.

#figure(
  image("../diagrams/system-tests/k6_report.png", width: 70%),
  caption: [Resultados de pruebas de rendimiento K6.],
) <fig:k6_results>

La Figura 9 muestra los resultados de fuzzing con Jazzer @jazzer2023. Las pruebas de fuzzing ejecutaron 35 clases objetivo cubriendo deserialización JSON, validación de entrada, utilidades criptográficas, renderizado de plantillas, lógica de negocio y procesamiento de extensiones @fuzzing2008. La campaña de fuzzing encontró cero crashes críticos.

#figure(
  image("../diagrams/system-tests/fuzzing_report.png", width: 70%),
  caption: [Resultados de pruebas de fuzzing Jazzer.],
) <fig:fuzzing_results>

La Figura 10 muestra los resultados de análisis estático SonarQube @sonarqube2024. El análisis estático identificó 16 bugs, 10 vulnerabilidades y 1,412 code smells en el código fuente. El Quality Gate pasó con un ratio de deuda técnica del 0.5% @sonarqube2020.

#figure(
  image("../diagrams/system-tests/sonarqube_report.png", width: 70%),
  caption: [Resultados de análisis estático SonarQube.],
) <fig:sonarqube_results>
