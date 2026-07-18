= Methods <sec:methods>

The testing strategy followed a structured approach with seven major activities, each addressing a specific aspect of the quality assurance process. This section describes each activity in detail.

== Planning and Methodology

Test planning followed the ISO/IEC/IEEE 29119 standard, with formal test plans documenting scope, methodology, entry and exit criteria, and risk mitigation strategies @iso29119. The project adopted Scrum as the development framework, with two-week sprints and clearly defined deliverables for each testing level @cohn2004. Communication was maintained through WhatsApp for daily coordination, Google Meet for sprint ceremonies, and GitHub Projects for task tracking.

Each testing level had its own test plan and execution report, providing traceability from requirements to test results. The team conducted sprint planning, review, and retrospective meetings to ensure alignment and continuous improvement.

Risk management was integrated into the testing process, with a formal risk register identifying potential issues and their mitigation strategies. Table III summarizes the key risks identified.

#figure(
  table(
    columns: (auto, auto, auto, auto, auto),
    align: (left, center, center, center, left),
    table.header([*Risk*], [*Prob.*], [*Impact*], [*Severity*], [*Mitigation*]),
    [Limited Spring Boot experience], [4], [4], [16], [Peer code reviews],
    [Tight delivery deadline], [5], [4], [20], [Prioritize critical modules],
    [Integration conflicts], [4], [3], [12], [Strict branching policies],
    [Tool automation difficulties], [2], [3], [6], [Internal technical support],
  ),
  caption: [Key risks identified and their mitigation strategies.],
) <tab:risks>

== Unit Testing

Unit testing focused on validating individual components in isolation, using mocks and test doubles to eliminate external dependencies @myers2011. The backend tests used JUnit 5 with Mockito for dependency mocking, while frontend tests used Vitest with HttpTestingController for Angular and jsdom for Lit components @springboot2024.

The test suite was organized by application layer: managers, controllers, repositories, utilities, and frontend components. Backend unit tests covered business logic managers, REST API controllers, data repositories, and utility classes. Frontend unit tests validated component behavior, service communication, route guards, and form validations.

The test execution was automated through GitHub Actions @githubactions2024, with two workflows: test-pr.yml for pull requests and test-push.yml for main branch pushes. Coverage was measured using JaCoCo for backend and Vitest's coverage-v8 for frontend @coverage2015.

== Integration Testing

Integration testing validated the interaction between multiple components using real dependencies @testcontainers2024. The strategy adopted a Big Bang incremental approach, first integrating infrastructure components, then business logic flows, and finally API contract validation.

Backend integration tests used Testcontainers to manage ephemeral PostgreSQL instances, providing real database interactions without external dependencies. The tests validated complete flows from controllers through managers to repositories. Stripe Mock container was used for payment gateway testing, while PayPal sandbox accounts enabled real payment flow validation.

API contract validation was performed using SpringDoc OpenAPI to generate descriptors automatically from controllers. Playwright frontend integration tests validated singular admin actions (authentication, event CRUD, user management, navigation) against the running backend @playwright2024.

== Functional Testing

Functional testing employed black-box testing techniques to validate system behavior against requirements without examining internal code structure @myers2011. The techniques applied included equivalence partitioning, boundary value analysis, use case testing, and decision tables @iso29119.

The project was tasked with selecting three functional modules for focused testing. The selected modules were: Manager, Controller/API, and Reservation Flows. These modules represent the core business logic, API layer, and critical user workflows of the system respectively.

The functional test suite covered manual test cases across the three selected modules, with additional coverage of supporting modules. Each test case followed a structured format with preconditions, input data, execution steps, and expected results.

== System Testing

System testing focused on non-functional requirements, specifically security and performance @pressman2014. The security testing strategy employed two complementary approaches: static application security testing with SonarQube @sonarqube2024 and fuzzing with Jazzer @jazzer2023. Performance testing used K6 to validate system behavior under various load conditions @k6performance2024.

Table IV summarizes the tools used for system testing.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, left, left, left),
    table.header([*Tool*], [*Category*], [*Target*], [*Metric*]),
    [SonarQube], [SAST - Security], [Static code analysis], [Bugs, vulnerabilities, code smells],
    [Jazzer], [Fuzzing - Security], [35 utility classes], [Crashes, exceptions],
    [K6], [Performance], [Reservation flow], [Response time, throughput, error rate],
  ),
  caption: [System testing tools and their targets.],
) <tab:system_tools>

== Acceptance Testing

Acceptance testing validated complete user workflows from the end-user perspective. The acceptance test suite covered the admin panel interface, including authentication, event management, reservation processing, and check-in operations. These tests were executed against a running instance of alf.io deployed in a Kubernetes cluster.

The acceptance tests used Playwright for browser automation, testing against the Chromium browser @playwright2024. The tests validated that the system met the specified acceptance criteria and functioned correctly in a production-like environment.

== CI/CD Automation

The CI/CD automation activity established automated pipelines for continuous testing and deployment @githubactions2024. The pipeline included code formatting, style checking, unit testing, integration testing, end-to-end testing, performance testing, fuzzing, and static analysis @cicd2016.

Figure 1 shows the CI/CD pipeline workflow with parallel job execution.

#figure(
  image("../diagrams/ci-cd-pipeline.png", width: 100%),
  caption: [CI/CD pipeline workflow for alf.io showing parallel job execution.],
) <fig:pipeline>

The pipeline executed on every pull request and push to the main branch, ensuring that all changes were validated before deployment @cicd2017. Coverage reports were automatically generated and published to GitHub Pages for stakeholder review.
