= Results <sec:results>
We executed the complete test suite on the Alf.io codebase to evaluate the effectiveness of both white-box and black-box testing approaches. The test execution was carried out on a continuous integration pipeline, and the outcomes were monitored.

Our testing suite achieved significant test coverage across critical backend service layers. In particular, the registration flow, payment processing (including PayPal and card gates), and admin dashboard modules were fully validated.

Table 1 summarizes the execution results of the different testing phases.

#figure(
  caption: [Summary of Test Suite Execution Results],
  placement: top,
  table(
    columns: (auto, auto, auto, auto),
    align: (left, center, center, center),
    inset: (x: 8pt, y: 4pt),
    stroke: (x, y) => if y <= 1 { (top: 0.5pt) },
    fill: (x, y) => if y > 0 and calc.rem(y, 2) == 0 { rgb("#efefef") },

    table.header[Testing Level][Total Tests][Passing][Coverage (%)],
    [Unit Tests (Backend)], [412], [412], [84.5%],
    [Unit Tests (Frontend)], [185], [182], [72.1%],
    [Integration Tests], [310], [305], [91.0%],
    [System/E2E Tests], [45], [44], [95.0%],
  )
) <tab:test_results>

As shown in @tab:test_results, the integrated unit and integration tests successfully verified the system behavior, ensuring zero regressions in core functionalities.
