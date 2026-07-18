= Metodología <sec:methods>

La estrategia de pruebas siguió un enfoque estructurado con siete actividades principales, cada una abordando un aspecto específico del proceso de aseguramiento de calidad. Esta sección describe cada actividad en detalle.

== Planificación y Metodología

La planificación de pruebas siguió el estándar ISO/IEC/IEEE 29119, con planes de pruebas formales que documentaban alcance, metodología, criterios de entrada y salida, y estrategias de mitigación de riesgos @iso29119. El proyecto adoptó Scrum como marco de desarrollo, con sprints de dos semanas y entregables claramente definidos para cada nivel de prueba @cohn2004. La comunicación se mantuvo a través de WhatsApp para coordinación diaria, Google Meet para ceremonias de sprint y GitHub Projects para seguimiento de tareas.

Cada nivel de prueba tuvo su propio plan de pruebas e informe de ejecución, proporcionando trazabilidad desde los requisitos hasta los resultados de las pruebas. El equipo realizó reuniones de planificación, revisión y retrospectiva de sprint para asegurar alineamiento y mejora continua.

La gestión de riesgos se integró en el proceso de pruebas, con un registro formal de riesgos que identificaba problemas potenciales y sus estrategias de mitigación. La Tabla III resume los riesgos clave identificados.

#figure(
  table(
    columns: (auto, auto, auto, auto, auto),
    align: (left, center, center, center, left),
    table.header([*Riesgo*], [*Prob.*], [*Impacto*], [*Severidad*], [*Mitigación*]),
    [Experiencia limitada en Spring Boot], [4], [4], [16], [Revisiones de código por pares],
    [Fecha de entrega ajustada], [5], [4], [20], [Priorizar módulos críticos],
    [Conflictos de integración], [4], [3], [12], [Políticas estrictas de branching],
    [Dificultades con automatización], [2], [3], [6], [Soporte técnico interno],
  ),
  caption: [Riesgos clave identificados y sus estrategias de mitigación.],
) <tab:risks>

== Pruebas Unitarias

Las pruebas unitarias se enfocaron en validar componentes individuales de forma aislada, utilizando mocks y dobles de prueba para eliminar dependencias externas @myers2011. Las pruebas del backend utilizaron JUnit 5 con Mockito para mocking de dependencias, mientras que las pruebas del frontend utilizaron Vitest con HttpTestingController para Angular y jsdom para componentes Lit @springboot2024.

La suite de pruebas se organizó por capa de aplicación: managers, controladores, repositorios, utilidades y componentes del frontend. Las pruebas unitarias del backend cubrieron managers de lógica de negocio, controladores API REST, repositorios de datos y clases de utilidad. Las pruebas unitarias del frontend validaron comportamiento de componentes, comunicación de servicios, guards de rutas y validaciones de formularios.

La ejecución de pruebas se automatizó a través de GitHub Actions @githubactions2024, con dos workflows: test-pr.yml para pull requests y test-push.yml para pushes a la rama main. La cobertura se midió usando JaCoCo para backend y coverage-v8 de Vitest para frontend @coverage2015.

== Pruebas de Integración

Las pruebas de integración validaron la interacción entre múltiples componentes usando dependencias reales @testcontainers2024. La estrategia adoptó un enfoque Big Bang incremental, integrando primero componentes de infraestructura, luego flujos de lógica de negocio y finalmente validación de contratos de API.

Las pruebas de integración del backend utilizaron Testcontainers para gestionar instancias efímeras de PostgreSQL, proporcionando interacciones reales con la base de datos sin dependencias externas. Las pruebas validaron flujos completos desde controladores hasta repositorios a través de managers. El contenedor Stripe Mock se utilizó para pruebas de integración con pasarelas de pago, mientras que cuentas sandbox de PayPal permitieron la validación de flujos de pago reales.

La validación de contratos de API se realizó usando SpringDoc OpenAPI para generar descriptores automáticamente desde los controladores. Las pruebas de integración frontend con Playwright validaron acciones singulares de administración (autenticación, CRUD de eventos, gestión de usuarios, navegación) contra el backend en ejecución @playwright2024.

== Pruebas Funcionales

Las pruebas funcionales emplearon técnicas de caja negra para validar el comportamiento del sistema contra requisitos sin examinar la estructura interna del código @myers2011. Las técnicas aplicadas incluyeron partición por equivalencia, análisis de valores límite, pruebas de casos de uso y tablas de decisión @iso29119.

El proyecto fue encargado de seleccionar tres módulos funcionales para pruebas enfocadas. Los módulos seleccionados fueron: Manager, Controller/API y Flujos de Reserva. Estos módulos representan la lógica de negocio principal, la capa API y los flujos críticos de usuario del sistema respectivamente.

La suite de pruebas funcionales cubrió casos de prueba manuales en los tres módulos seleccionados, con cobertura adicional de módulos de soporte. Cada caso de prueba siguió un formato estructurado con precondiciones, datos de entrada, pasos de ejecución y resultados esperados.

== Pruebas de Sistema

Las pruebas de sistema se enfocaron en requisitos no funcionales, específicamente seguridad y rendimiento @pressman2014. La estrategia de pruebas de seguridad empleó dos enfoques complementarios: pruebas de seguridad de aplicación estática con SonarQube @sonarqube2024 y fuzzing con Jazzer @jazzer2023. Las pruebas de rendimiento utilizaron K6 para validar el comportamiento del sistema bajo diversas condiciones de carga @k6performance2024.

La Tabla IV resume las herramientas utilizadas para las pruebas de sistema.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, left, left, left),
    table.header([*Herramienta*], [*Categoría*], [*Objetivo*], [*Métrica*]),
    [SonarQube], [SAST - Seguridad], [Análisis estático de código], [Bugs, vulnerabilidades, code smells],
    [Jazzer], [Fuzzing - Seguridad], [35 clases de utilidad], [Crashes, excepciones],
    [K6], [Rendimiento], [Flujo de reserva], [Tiempo de respuesta, throughput, tasa de error],
  ),
  caption: [Herramientas de pruebas de sistema y sus objetivos.],
) <tab:system_tools>

== Pruebas de Aceptación

Las pruebas de aceptación validaron flujos de trabajo completos desde la perspectiva del usuario final. La suite de pruebas de aceptación cubrió la interfaz del panel de administración, incluyendo autenticación, gestión de eventos, procesamiento de reservas y operaciones de check-in. Estas pruebas se ejecutaron contra una instancia de alf.io desplegada en un clúster de Kubernetes.

Las pruebas de aceptación utilizaron Playwright para automatización de navegadores, probando contra el navegador Chromium @playwright2024. Las pruebas validaron que el sistema cumplía con los criterios de aceptación especificados y funcionaba correctamente en un ambiente similar al de producción.

== Automatización CI/CD

La actividad de automatización CI/CD estableció pipelines automatizados para pruebas y despliegue continuo @githubactions2024. El pipeline incluyó formateo de código, verificación de estilo, pruebas unitarias, pruebas de integración, pruebas de extremo a extremo, pruebas de rendimiento, fuzzing y análisis estático @cicd2016.

La Figura 1 muestra el flujo de trabajo del pipeline CI/CD con ejecución paralela de jobs.

#figure(
  image("../diagrams/ci-cd-pipeline.png", width: 100%),
  caption: [Flujo de trabajo del pipeline CI/CD para alf.io mostrando ejecución paralela de jobs.],
) <fig:pipeline>

El pipeline se ejecutó en cada pull request y push a la rama main, asegurando que todos los cambios fueran validados antes del despliegue @cicd2017. Los reportes de cobertura se generaron automáticamente y se publicaron en GitHub Pages para revisión de las partes interesadas.
