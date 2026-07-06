= Related Work <sec:related_work>
The verification of open-source event attendance management platforms like Alf.io requires a comprehensive and multi-layered testing strategy. In recent years, several testing methodologies have been proposed for web systems.

Automated unit testing using standard frameworks (such as JUnit for Java backends and Vitest for modern frontend modules) represents the baseline of quality assurance in modern DevOps pipelines @netwok2020. However, unit testing alone is insufficient for validating business workflows and transaction boundaries, which necessitate integration testing with containerized databases (e.g., using Testcontainers) @netwok2022.

Furthermore, end-to-end (E2E) and system testing have transitioned towards headless browser frameworks that simulate user behavior under real scenarios. In this paper, we evaluate how a unified testing suite combining unit, integration, and system-level tests impacts the robustness and stability of the Alf.io platform.
