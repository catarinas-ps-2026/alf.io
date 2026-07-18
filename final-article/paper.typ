#import "@preview/charged-ieee:0.1.4": ieee

#show: ieee.with(
  title: [Implementation of a Comprehensive Testing Strategy for the alf.io Open-Source Event Ticketing Platform],
  abstract: [
    This paper presents the implementation of a comprehensive testing strategy for alf.io, an open-source event ticketing platform. The project involved designing and executing tests across four levels: unit, integration, system, and acceptance testing. The testing covered the entire technology stack including a Java/Spring Boot backend, Angular public frontend, and Lit-based administration interface. Automated testing through GitHub Actions CI/CD pipelines achieved 100% pass rates across unit tests (2,290 cases), integration tests (442 cases), and end-to-end tests (88 cases), with an aggregated code coverage of 85.76%. Performance testing with K6 validated system behavior under load, fuzzing with Jazzer confirmed input handling robustness, and static analysis with SonarQube identified code quality improvements. The results demonstrate the effectiveness of multi-layered testing approaches for ensuring quality in complex web applications.
  ],
  authors: (
    (
      name: "Christian Raúl Mestas Zegarra",
      department: [Facultad de Ingeniería de la Producción y Servicios],
      organization: [Universidad Nacional de San Agustín de Arequipa],
      location: [Arequipa, Perú],
      email: "cmestasz@unsa.edu.pe",
    ),
    (
      name: "Luis Gustavo Sequeiros Condori",
      department: [Facultad de Ingeniería de la Producción y Servicios],
      organization: [Universidad Nacional de San Agustín de Arequipa],
      location: [Arequipa, Perú],
      email: "lsequeiros@unsa.edu.pe",
    ),
    (
      name: "Mariel Alisson Jara Mamani",
      department: [Facultad de Ingeniería de la Producción y Servicios],
      organization: [Universidad Nacional de San Agustín de Arequipa],
      location: [Arequipa, Perú],
      email: "mjarama@unsa.edu.pe",
    ),
    (
      name: "Rodrigo Alexander Fernández Huarca",
      department: [Facultad de Ingeniería de la Producción y Servicios],
      organization: [Universidad Nacional de San Agustín de Arequipa],
      location: [Arequipa, Perú],
      email: "rfernandezh@unsa.edu.pe",
    ),
    (
      name: "Álvaro Raúl Quispe Condori",
      department: [Facultad de Ingeniería de la Producción y Servicios],
      organization: [Universidad Nacional de San Agustín de Arequipa],
      location: [Arequipa, Perú],
      email: "aquispecondo@unsa.edu.pe",
    ),
    (
      name: "Mathías Alonso Barrios Medina",
      department: [Facultad de Ingeniería de la Producción y Servicios],
      organization: [Universidad Nacional de San Agustín de Arequipa],
      location: [Arequipa, Perú],
      email: "mbarriosmed@unsa.edu.pe",
    ),
  ),
  index-terms: ("Software Testing", "Test Automation", "CI/CD", "Open Source", "Event Management", "Quality Assurance"),
  bibliography: bibliography("refs.bib"),
  figure-supplement: [Fig.],
)

#include "sections/introduction.typ"
#include "sections/background.typ"
#include "sections/study_case.typ"
#include "sections/methods.typ"
#include "sections/results.typ"
#include "sections/conclusion.typ"
#include "sections/future_work.typ"
