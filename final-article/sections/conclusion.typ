= Conclusion <sec:conclusion>

The comprehensive testing strategy implemented for alf.io demonstrated the effectiveness of a multi-layered approach to software quality assurance. The project achieved 100% pass rates across unit tests, integration tests, and acceptance tests, while maintaining an aggregated code coverage of 85.76%. These results validated the testing methodology and confirmed the system's functional correctness.

The unit testing phase established a solid foundation by validating individual components in isolation. The combination of JUnit 5 with Mockito for backend and Vitest for frontend provided comprehensive coverage of business logic, API controllers, and user interface components. The automated execution through GitHub Actions ensured consistent test results and rapid feedback for developers.

Integration testing confirmed that system components interacted correctly with real dependencies. The use of Testcontainers for ephemeral PostgreSQL instances, Stripe Mock for payment testing, and Playwright for browser automation provided realistic validation of the complete system stack. The API contract validation through OpenAPI and Redoc ensured API stability across versions.

The functional testing phase, conducted through manual execution of 120+ test cases, validated user-facing functionality across 15 modules. This testing identified defects that automated tests could not catch, such as UI behavior inconsistencies and edge cases in user interactions. The systematic application of black-box testing techniques ensured thorough coverage of functional requirements.

System testing addressed non-functional requirements through performance testing with K6, fuzzing with Jazzer, and static analysis with SonarQube. While the performance tests identified a scalability issue under extreme load, the system demonstrated graceful degradation. Fuzzing confirmed robust handling of invalid inputs with zero critical crashes, and SonarQube identified areas for code quality improvement with an acceptable technical debt ratio of 0.5%.

The implementation established reproducible testing processes through automated CI/CD pipelines, comprehensive documentation following ISO/IEC/IEEE 29119 standards, and systematic defect tracking. The project demonstrated that comprehensive testing strategies, when properly planned and executed, can significantly improve software quality while providing valuable feedback for development teams working on complex web applications.
