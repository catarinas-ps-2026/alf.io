= Results <sec:results>

== Components Tested

The testing strategy covered all major components of the alf.io system across three applications and supporting infrastructure @ossquality2007. Table V summarizes the components and their test counts.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, center, center, center),
    table.header([*Component*], [*Technology*], [*Test Count*], [*Pass Rate*]),
    [Backend], [Java 17, Spring Boot 3.5.x], [2,290], [100%],
    [Public Frontend], [Angular 17], [487], [100%],
    [Admin Frontend], [Lit 3], [167], [100%],
    [Integration Backend], [JUnit + Testcontainers], [354], [100%],
    [E2E Tests], [Playwright], [78], [100%],
    [Functional Tests], [Manual], [120+], [97.5%],
  ),
  caption: [Components tested and their results.],
) <tab:components>

== Unit Test Results

The unit test suite executed 2,290 test cases across all three applications, achieving a 100% pass rate. The backend contributed 1,636 tests, the public frontend contributed 487 tests, and the administration frontend contributed 167 tests.

Figure 2 shows the backend code coverage report. The aggregated code coverage reached 85.76% for instructions/statements, exceeding the 85% target @coverage2015.

#figure(
  image("../diagrams/unit-tests/backend_cov.png", width: 100%),
  caption: [Backend code coverage report (JaCoCo).],
) <fig:backend_cov>

Figures 3 and 4 show the frontend coverage reports. The administration frontend achieved 94.03% statement coverage, while the public frontend achieved 85.60% statement coverage.

#figure(
  image("../diagrams/unit-tests/frontend_admin_cov.png", width: 100%),
  caption: [Administration frontend code coverage (Vitest).],
) <fig:admin_cov>

#figure(
  image("../diagrams/unit-tests/frontend_public_cov.png", width: 100%),
  caption: [Public frontend code coverage (Vitest).],
) <fig:public_cov>

== Integration Test Results

Integration testing executed 442 test cases across four components @testcontainers2024. All tests achieved 100% pass rates. Figure 5 shows the backend integration test results.

#figure(
  image("../diagrams/integration-tests/integration_tests_backend.png", width: 100%),
  caption: [Backend integration test results (JUnit).],
) <fig:integration_backend>

The integration test suite validated 30 mandatory endpoints covering the critical flow: Reservation, Payment, and Check-In. Figure 6 shows the Playwright integration test results @playwright2024.

#figure(
  image("../diagrams/integration-tests/integration_tests_playwright.png", width: 100%),
  caption: [Playwright integration test results (Chromium + Firefox).],
) <fig:integration_playwright>

API contract validation confirmed stability between the current implementation and the reference descriptor. Figure 7 shows the API contract validation portal.

#figure(
  image("../diagrams/integration-tests/integration_tests_redoc.png", width: 100%),
  caption: [API contract validation portal (Redoc/OpenAPI).],
) <fig:integration_redoc>

== Functional Test Results

Functional testing covered manual test cases across the three selected modules: Manager, Controller/API, and Reservation Flows @myers2011. The overall functional test pass rate was 97.5%. Table VI summarizes the results by module.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, center, center, left),
    table.header([*Module*], [*Test Cases*], [*Pass Rate*], [*Defects Found*]),
    [Manager], [15], [93%], [1],
    [Controller/API], [20], [95%], [1],
    [Reservation Flows], [25], [96%], [1],
    [Ticket Editing], [11], [82%], [3],
    [Event Configuration], [7], [57%], [3],
    [Other Modules], [42], [100%], [0],
  ),
  caption: [Functional test results by module.],
) <tab:functional_results>

== Defects Found and Fixed

The testing process identified and documented defects across all testing levels @iso29119. Table VII lists the defects discovered during functional testing and the commits that fixed them.

#figure(
  table(
    columns: (auto, auto, auto, auto),
    align: (left, left, left, left),
    table.header([*Test ID*], [*Module*], [*Defect*], [*Commit*]),
    [CPF-01-003], [Ticket Editing], [Server error 500 with 254-char names], [7e524f8],
    [CPF-01-006], [Ticket Editing], [System accepts numbers in name field], [99f17a7],
    [CPF-01-011], [Ticket Editing], [Email length validation not enforced], [dc1341a],
    [CPF-03-006], [Reservation State], [Button visible with incomplete data],

    [7e524f8], [CPF-09-006], [Organization Config], [SQL error exposed with 256-char names],
    [99f17a7], [CPF-10-001], [Event Config], [Date picker scroll bug],
    [dc1341a], [CPF-10-006], [Event Config], [Silent save disable without feedback],
    [7e524f8], [CPF-10-007], [Event Config], [Duplicate hidden codes allowed],
    [99f17a7], [CPF-14-004], [Localization], [Timezone warning not displayed],
    [dc1341a],
  ),
  caption: [Defects discovered during functional testing and their fixes.],
) <tab:defects>

== System Test Results

Figure 8 shows the K6 performance test results @k6performance2024. Performance testing executed 54,176 HTTP requests against the reservation flow, achieving a throughput of 190.43 requests per second. The median response time was 745 milliseconds, meeting the less than 2 second target. However, the error rate of 8.43% exceeded the less than 1% target under extreme load conditions.

#figure(
  image("../diagrams/system-tests/k6_report.png", width: 100%),
  caption: [K6 performance test results.],
) <fig:k6_results>

Figure 9 shows the Jazzer fuzzing test results @jazzer2023. Fuzzing testing executed 35 target classes covering JSON deserialization, input validation, cryptographic utilities, template rendering, business logic, and extension processing @fuzzing2008. The fuzzing campaign found zero critical crashes.

#figure(
  image("../diagrams/system-tests/fuzzing_report.png", width: 100%),
  caption: [Jazzer fuzzing test results.],
) <fig:fuzzing_results>

Figure 10 shows the SonarQube static analysis results @sonarqube2024. Static analysis identified 16 bugs, 10 vulnerabilities, and 1,412 code smells across the codebase. The Quality Gate passed with a technical debt ratio of 0.5% @sonarqube2020.

#figure(
  image("../diagrams/system-tests/sonarqube_report.png", width: 100%),
  caption: [SonarQube static analysis results.],
) <fig:sonarqube_results>
