= Study Case <sec:study_case>

== Project History and Evolution

Alf.io was created on July 26, 2014, by Celestino Bellone, a software developer based in Rovio, Switzerland @alfio2014. The project emerged from the need for a self-hosted, open-source ticketing solution that could be customized for various event types. Bellone, who later co-founded Swicket, a premium hosted version of alf.io, has contributed 2,531 commits to the project, representing approximately 64% of all contributions.

The project's co-maintainer, Sylvain Jermini, joined in 2010 and has contributed 1,295 commits, accounting for 33% of total contributions. Jermini, based in Switzerland, has been instrumental in the frontend architecture evolution, particularly the migration from AngularJS to Lit web components. Together, Bellone and Jermini have maintained continuous development over 12 years, releasing 100 versions and establishing alf.io as a mature, production-ready solution @ossquality2007.

The project's evolution can be traced through major version milestones. Table I summarizes the key versions and their features.

#figure(
  table(
    columns: (auto, auto, auto),
    align: (left, left, left),
    table.header([*Version*], [*Date*], [*Key Features*]),
    [1.0], [2014], [Initial release],
    [1.14], [Apr 2018], [Extension system with scripting hooks],
    [2.0-M0], [Nov 2018], [Spring Boot 2, MySQL/HSQLDB removed],
    [2.0-M4], [Apr 2022], [Organizations, multi-tenancy, Stripe/Mollie/PayPal],
    [2.0-M5], [Sep 2024], [Spring Boot 3.2, Lit admin frontend, Cloudflare Turnstile],
    [2.0-M5-2606], [Jun 2026], [Security fixes, custom offline payments, pnpm frontend],
  ),
  caption: [Key versions and milestones of alf.io.],
) <tab:versions>

== Technology Stack

The current technology stack of alf.io reflects modern web application development practices @springboot2024. Table II summarizes the main components.

#figure(
  table(
    columns: (auto, auto),
    align: (left, left),
    table.header([*Component*], [*Technology*]),
    [Backend], [Java 17, Spring Boot 3.5.x, Jetty],
    [Build System], [Gradle with test automation integration],
    [Database], [PostgreSQL 10/15/16 with Flyway migrations],
    [Public Frontend], [Angular 17],
    [Admin Frontend], [Lit 3 with Shoelace, Vite],
    [Unit Testing], [JUnit 5],
    [Integration Testing], [Testcontainers @testcontainers2024],
  ),
  caption: [Technology stack of alf.io.],
) <tab:stack>

== System Architecture

The system architecture follows a layered design pattern with clear separation of concerns. The backend implements a standard Spring Boot architecture with controllers handling HTTP requests, managers implementing business logic, repositories managing database interactions, and models representing domain entities @springboot2024.

The database schema implements Row-Level Security in PostgreSQL to ensure data isolation between organizations in multi-tenancy deployments. This approach provides database-level security without requiring separate database instances for each tenant. The schema is version-controlled through Flyway migrations, ensuring consistent database state across development, testing, and production environments.

External service integrations include payment gateways, email services, and digital wallet providers. The system supports both online and offline payment methods, with built-in mechanisms for handling payment confirmation, expiration, and reconciliation.

== Fork and Academic Context

The repository under study, catarinas-ps-2026/alf.io, is a fork of the original alfio-event/alf.io project, created for academic purposes. The fork was established on May 24, 2026, as part of a software testing course at the Universidad Nacional de San Agustin de Arequipa. The project team consists of six students led by a test lead, with Professor Robert Edison Arisaca Mamani providing academic supervision.

The academic context required a systematic approach to testing that went beyond typical open-source contribution practices @ossquality2002. The team implemented comprehensive test plans following ISO/IEC/IEEE 29119 standards, with formal documentation of test strategies, execution results, and defect tracking @iso29119.
