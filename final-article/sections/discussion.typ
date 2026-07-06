= Discussion <sec:discussion>
The results demonstrate that combining black-box and white-box testing levels provides a comprehensive safety net for the development of Alf.io. White-box testing, particularly unit testing, allowed for localizing bugs in utility classes and validation constraints. However, it was integration testing that revealed concurrency issues in reservation expiration tasks and database constraints.

The main challenge encountered during the implementation of the suite was the execution time of integration tests. Running a complete database container with Testcontainers adds a overhead to the feedback loop. To mitigate this, we propose caching strategies and isolated test executions for local developers.
