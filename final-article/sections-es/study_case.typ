= Caso de Estudio <sec:study_case>

== Historia y Evolución del Proyecto

Alf.io fue creado el 26 de julio de 2014 por Celestino Bellone, un desarrollador de software con base en Rovio, Suiza @alfio2014. El proyecto surgió de la necesidad de una solución de venta de entradas autohospedada y de código abierto que pudiera personalizarse para diversos tipos de eventos. Bellone, quien posteriormente cofundó Swicket, una versión premium hospedada de alf.io, ha contribuido 2,531 commits al proyecto, representando aproximadamente el 64% de todas las contribuciones.

El co-mantenedor del proyecto, Sylvain Jermini, se unió en 2010 y ha contribuido 1,295 commits, representando el 33% del total de contribuciones. Jermini, con base en Suiza, ha sido instrumental en la evolución de la arquitectura del frontend, particularmente en la migración de AngularJS a componentes web Lit. Juntos, Bellone y Jermini han mantenido un desarrollo continuo durante 12 años, lanzando 100 versiones y estableciendo alf.io como una solución madura y lista para producción @ossquality2007.

La evolución del proyecto puede rastrearse a través de hitos importantes de versión. La Tabla I resume las versiones clave y sus características.

#figure(
  table(
    columns: (auto, auto, auto),
    align: (left, left, left),
    table.header([*Versión*], [*Fecha*], [*Características Clave*]),
    [1.0], [2014], [Versión inicial],
    [1.14], [Abr 2018], [Sistema de extensiones con hooks de scripting],
    [2.0-M0], [Nov 2018], [Spring Boot 2, eliminación de MySQL/HSQLDB],
    [2.0-M4], [Abr 2022], [Organizaciones, multi-tenencia, Stripe/Mollie/PayPal],
    [2.0-M5], [Sep 2024], [Spring Boot 3.2, frontend admin Lit, Cloudflare Turnstile],
    [2.0-M5-2606], [Jun 2026], [Correcciones de seguridad, pagos offline personalizados, frontend pnpm],
  ),
  caption: [Versiones clave y hitos de alf.io.],
) <tab:versions>

== Pila Tecnológica

La pila tecnológica actual de alf.io refleja las prácticas modernas de desarrollo de aplicaciones web @springboot2024. La Tabla II resume los componentes principales.

#figure(
  table(
    columns: (auto, auto),
    align: (left, left),
    table.header([*Componente*], [*Tecnología*]),
    [Backend], [Java 17, Spring Boot 3.5.x, Jetty],
    [Sistema de Build], [Gradle con integración de automatización de pruebas],
    [Base de Datos], [PostgreSQL 10/15/16 con migraciones Flyway],
    [Frontend Público], [Angular 17],
    [Frontend Admin], [Lit 3 con Shoelace, Vite],
    [Pruebas Unitarias], [JUnit 5, Vitest, Mockito],
    [Pruebas de Integración], [Testcontainers @testcontainers2024, Playwright @playwright2024],
    [Pruebas de Rendimiento], [K6 @k6performance2024],
    [Fuzzing], [Jazzer @jazzer2023],
    [Análisis Estático], [SonarQube @sonarqube2024],
  ),
  caption: [Pila tecnológica de alf.io.],
) <tab:stack>

== Arquitectura del Sistema

La arquitectura del sistema sigue un patrón de diseño por capas con separación clara de responsabilidades. El backend implementa una arquitectura estándar de Spring Boot con controladores que manejan solicitudes HTTP, managers que implementan lógica de negocio, repositorios que gestionan interacciones con la base de datos y modelos que representan entidades de dominio @springboot2024.

El esquema de la base de datos implementa Row-Level Security en PostgreSQL para asegurar el aislamiento de datos entre organizaciones en despliegues multi-tenancy. Este enfoque proporciona seguridad a nivel de base de datos sin requerir instancias separadas de base de datos para cada tenant. El esquema está controlado por versiones a través de migraciones Flyway, asegurando un estado consistente de la base de datos entre ambientes de desarrollo, pruebas y producción.

Las integraciones con servicios externos incluyen pasarelas de pago, servicios de correo electrónico y proveedores de billeteras digitales. El sistema soporta tanto métodos de pago en línea como fuera de línea, con mecanismos incorporados para manejar confirmación de pagos, expiración y reconciliación.

== Fork y Contexto Académico

El repositorio en estudio, catarinas-ps-2026/alf.io, es un fork del proyecto original alfio-event/alf.io, creado con fines académicos. El fork fue establecido el 24 de mayo de 2026, como parte de un curso de pruebas de software en la Universidad Nacional de San Agustín de Arequipa. El equipo del proyecto consta de seis estudiantes liderados por un líder de pruebas, con el profesor Robert Edison Arisaca Mamani proporcionando supervisión académica.

El contexto académico requirió un enfoque sistemático de pruebas que trascendiera las prácticas típicas de contribución a código abierto @ossquality2002. El equipo implementó planes de pruebas integrales siguiendo los estándares ISO/IEC/IEEE 29119, con documentación formal de estrategias de prueba, resultados de ejecución y seguimiento de defectos @iso29119.
