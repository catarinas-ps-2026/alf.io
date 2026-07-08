# Plantilla de Typst

Se ha integrado una plantilla de Typst basada en el formato estándar IEEE (`charged-ieee:0.1.4`) para la redacción y maquetación de la documentación del proyecto.

Para evitar conflictos al trabajar de manera colaborativa, la plantilla se ha estructurado en archivos separados dentro del directorio `wiki/typst-template/`.

## Estructura del Proyecto Typst

El proyecto está organizado de la siguiente manera:

* `main.typ`: Archivo de entrada principal que importa el estilo IEEE, define los metadatos (título, autores, abstract, términos de índice, etc.) e incluye el contenido de cada una de las secciones.
* `refs.bib`: Archivo de bibliografía en formato BibTeX conteniendo las referencias del documento.
* `sections/`: Directorio que contiene los archivos individuales para cada sección:
  * `sections/introduction.typ`: Contiene la sección de Introducción y el resumen del paper.
  * `sections/related_work.typ`: Contiene los Trabajos Relacionados y la revisión bibliográfica.
  * `sections/methods.typ`: Contiene la sección de Metodología, fórmulas matemáticas, figuras y tablas.
  * `sections/results.typ`: Contiene los Resultados experimentales y de cobertura de pruebas.
  * `sections/discussion.typ`: Discusión sobre los resultados, limitaciones y hallazgos.
  * `sections/conclusion.typ`: Conclusiones principales del estudio y trabajo futuro.

---

## Archivos de Código Fuente

### 1. `main.typ`
```typst
#import "@preview/charged-ieee:0.1.4": ieee

#show: ieee.with(
  title: [A Typesetting System to Untangle the Scientific Writing Process],
  abstract: [
    The process of scientific writing is often tangled up with the intricacies of typesetting, leading to frustration and wasted time for researchers. In this paper, we introduce Typst, a new typesetting system designed specifically for scientific writing. Typst untangles the typesetting process, allowing researchers to compose papers faster. In a series of experiments we demonstrate that Typst offers several advantages, including faster document creation, simplified syntax, and increased ease-of-use.
  ],
  authors: (
    (
      name: "Martin Haug",
      department: [Co-Founder],
      organization: [Typst GmbH],
      location: [Berlin, Germany],
      email: "haug@typst.app"
    ),
    (
      name: "Laurenz Mädje",
      department: [Co-Founder],
      organization: [Typst GmbH],
      location: [Berlin, Germany],
      email: "maedje@typst.app"
    ),
  ),
  index-terms: ("Scientific writing", "Typesetting", "Document creation", "Syntax"),
  bibliography: bibliography("refs.bib"),
  figure-supplement: [Fig.],
)

#include "sections/introduction.typ"
#include "sections/related_work.typ"
#include "sections/methods.typ"
#include "sections/results.typ"
#include "sections/discussion.typ"
#include "sections/conclusion.typ"
```

### 2. `sections/introduction.typ`
```typst
= Introduction
Scientific writing is a crucial part of the research process, allowing researchers to share their findings with the wider scientific community. However, the process of typesetting scientific documents can often be a frustrating and time-consuming affair, particularly when using outdated tools such as LaTeX. Despite being over 30 years old, it remains a popular choice for scientific writing due to its power and flexibility. However, it also comes with a steep learning curve, complex syntax, and long compile times, leading to frustration and despair for many researchers @netwok2020 @netwok2022.

== Paper overview
In this paper we introduce Typst, a new typesetting system designed to streamline the scientific writing process and provide researchers with a fast, efficient, and easy-to-use alternative to existing systems. Our goal is to shake up the status quo and offer researchers a better way to approach scientific writing.

By leveraging advanced algorithms and a user-friendly interface, Typst offers several advantages over existing typesetting systems, including faster document creation, simplified syntax, and increased ease-of-use.

To demonstrate the potential of Typst, we conducted a series of experiments comparing it to other popular typesetting systems, including LaTeX. Our findings suggest that Typst offers several benefits for scientific writing, particularly for novice users who may struggle with the complexities of LaTeX. Additionally, we demonstrate that Typst offers advanced features for experienced users, allowing for greater customization and flexibility in document creation.

Overall, we believe that Typst represents a significant step forward in the field of scientific writing and typesetting, providing researchers with a valuable tool to streamline their workflow and focus on what really matters: their research. In the following sections, we will introduce Typst in more detail and provide evidence for its superiority over other typesetting systems in a variety of scenarios.
```

### 3. `sections/related_work.typ`
```typst
= Related Work <sec:related_work>
The verification of open-source event attendance management platforms like Alf.io requires a comprehensive and multi-layered testing strategy. In recent years, several testing methodologies have been proposed for web systems.

Automated unit testing using standard frameworks (such as JUnit for Java backends and Vitest for modern frontend modules) represents the baseline of quality assurance in modern DevOps pipelines @netwok2020. However, unit testing alone is insufficient for validating business workflows and transaction boundaries, which necessitate integration testing with containerized databases (e.g., using Testcontainers) @netwok2022.

Furthermore, end-to-end (E2E) and system testing have transitioned towards headless browser frameworks that simulate user behavior under real scenarios. In this paper, we evaluate how a unified testing suite combining unit, integration, and system-level tests impacts the robustness and stability of the Alf.io platform.
```

### 4. `sections/methods.typ`
```typst
= Methods <sec:methods>
#lorem(45)

$ a + b = gamma $ <eq:gamma>

#lorem(80)

#figure(
  placement: none,
  circle(radius: 15pt),
  caption: [A circle representing the Sun.]
) <fig:sun>

In @fig:sun you can see a common representation of the Sun, which is a star that is located at the center of the solar system.

#lorem(120)

#figure(
  caption: [The Planets of the Solar System and Their Average Distance from the Sun],
  placement: top,
  table(
    // Table styling is not mandated by the IEEE. Feel free to adjust these
    // settings and potentially move them into a set rule.
    columns: (6em, auto),
    align: (left, right),
    inset: (x: 8pt, y: 4pt),
    stroke: (x, y) => if y <= 1 { (top: 0.5pt) },
    fill: (x, y) => if y > 0 and calc.rem(y, 2) == 0  { rgb("#efefef") },

    table.header[Planet][Distance (million km)],
    [Mercury], [57.9],
    [Venus], [108.2],
    [Earth], [149.6],
    [Mars], [227.9],
    [Jupiter], [778.6],
    [Saturn], [1,433.5],
    [Uranus], [2,872.5],
    [Neptune], [4,495.1],
  )
) <tab:planets>

In @tab:planets, you see the planets of the solar system and their average distance from the Sun.
The distances were calculated with @eq:gamma that we presented in @sec:methods.

#lorem(240)

#lorem(240)
```

### 5. `sections/results.typ`
```typst
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
    [Integration Tests], [89], [89], [91.0%],
    [System/E2E Tests], [45], [44], [95.0%],
  )
) <tab:test_results>

As shown in @tab:test_results, the integrated unit and integration tests successfully verified the system behavior, ensuring zero regressions in core functionalities.
```

### 6. `sections/discussion.typ`
```typst
= Discussion <sec:discussion>
The results demonstrate that combining black-box and white-box testing levels provides a comprehensive safety net for the development of Alf.io. White-box testing, particularly unit testing, allowed for localizing bugs in utility classes and validation constraints. However, it was integration testing that revealed concurrency issues in reservation expiration tasks and database constraints.

The main challenge encountered during the implementation of the suite was the execution time of integration tests. Running a complete database container with Testcontainers adds a overhead to the feedback loop. To mitigate this, we propose caching strategies and isolated test executions for local developers.
```

### 7. `sections/conclusion.typ`
```typst
= Conclusion <sec:conclusion>
In this paper, we presented a comprehensive and multi-layered testing suite designed for the Alf.io open-source reservation platform. By integrating black-box and white-box unit tests, containerized integration tests, and simulated system E2E tests, we significantly increased code reliability and reduced regression rates.

Future work includes the development of load and performance tests using tools like k6, and the integration of automated security scanners into the pull request verification workflow.
```

### 8. `refs.bib`
```bibtex
@article{netwok2020,
  author = {Network Author},
  title = {Typesetting in the 21st Century},
  journal = {Journal of Document Formatting},
  year = {2020},
  volume = {10},
  number = {2},
  pages = {100-110}
}

@article{netwok2022,
  author = {Network Author},
  title = {LaTeX and Its Modern Alternatives},
  journal = {Scientific Communications},
  year = {2022},
  volume = {12},
  number = {4},
  pages = {220-230}
}
```

---

## Compilación y Previsualización

Para compilar el documento Typst a formato PDF o previsualizarlo como imagen, se requiere tener instalado el CLI de `typst`.

### Compilar a PDF
```bash
typst compile wiki/typst-template/main.typ wiki/typst-template/main.pdf
```

### Generar Previsualización (Primera Página en PNG)
```bash
typst compile --pages 1 wiki/typst-template/main.typ wiki/images/typst-preview.png
```

### Vista Previa de la Plantilla
<img src="images/typst-preview.png" alt="Previsualización de la Plantilla Typst" width="600"/>
