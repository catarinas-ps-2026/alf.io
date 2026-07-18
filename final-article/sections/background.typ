= Background <sec:background>

== State of the Art in Software Testing

Software testing has evolved significantly over the past decades, transitioning from manual verification to sophisticated automated frameworks and methodologies @iso29119. The ISO/IEC/IEEE 29119 standard provides a comprehensive framework for software testing, defining concepts, processes, documentation, and techniques that guide testing activities across all levels of software development. This standard emphasizes the importance of systematic test planning, execution, and reporting to ensure software quality.

The testing pyramid model, first proposed by Mike Cohn, remains a foundational concept in test strategy design @cohn2004. This model suggests that teams should invest most heavily in unit tests, followed by integration tests, with fewer end-to-end tests at the top. Modern adaptations of this model consider additional layers such as contract testing and performance testing @testingpyramid2021.

Test automation has become a cornerstone of modern software development practices, particularly in the context of continuous integration and continuous deployment pipelines @cicd2016. Research has demonstrated that automated testing significantly reduces time-to-market while improving code quality @cicd2017. Studies have shown that defects found in production cost significantly more to fix than those identified during development.

Modern testing strategies encompass multiple specialized approaches beyond traditional functional testing. Performance testing tools like K6 enable teams to validate system behavior under various load conditions @k6performance2024. Security testing through fuzzing tools such as Jazzer can identify vulnerabilities that traditional testing might miss @jazzer2023. Static analysis tools like SonarQube provide continuous code quality monitoring, detecting bugs, vulnerabilities, and code smells before they reach production @sonarqube2024.

== Event Management Systems and Open Source Software

Event management systems represent a specialized category of web applications that must handle complex business logic, financial transactions, and real-time operations. The open-source model has proven particularly successful in this domain, as it allows organizations to customize and extend the software to meet their specific needs while avoiding vendor lock-in @ossquality2002.

The alf.io project, created in 2014 by Celestino Bellone in Ticino, Switzerland, exemplifies the evolution of open-source event management software @alfio2014. What began as a simple Spring Boot application has grown into a mature ecosystem with 1,596 GitHub stars, 141,000+ Docker Hub pulls, and 100 releases. The project's name derives from the Italian given name Alfio, reflecting its origins in the Italian-speaking region of Switzerland.

The technology stack of alf.io has evolved significantly over its 12-year history. The backend transitioned from Spring Boot 1.x to 3.x, with Java evolving from version 8 to 17 @springboot2024. The frontend underwent a major transformation from AngularJS to Lit web components, reflecting the broader industry trend toward lightweight, standards-based frameworks. The database layer consolidated from supporting multiple databases to PostgreSQL-only, leveraging advanced features like Row-Level Security for multi-tenancy.

The project's architecture follows a modern microservices-inspired design while maintaining a monolithic deployment model. The backend implements a layered architecture with controllers, managers, repositories, and models. The frontend consists of two separate single-page applications: a public-facing Angular application for ticket purchases and a Lit-based administration interface for event organizers.

Payment gateway integration represents a critical aspect of event management systems. Alf.io supports multiple payment providers including Stripe, PayPal, Mollie, and bank transfers. The system's extension mechanism allows for custom integrations and business logic, making it adaptable to various organizational requirements.
