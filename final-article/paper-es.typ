#import "lib-es.typ": ieee

#show: ieee.with(
  title: [Implementación de una Estrategia Integral de Pruebas para la Plataforma de Código Abierto alf.io de Venta de Entradas de Eventos],
  abstract: [
    Este artículo presenta la implementación de una estrategia integral de pruebas para alf.io, una plataforma de código abierto para venta de entradas de eventos. El proyecto implicó diseñar y ejecutar pruebas en cuatro niveles: pruebas unitarias, de integración, de sistema y de aceptación. Las pruebas cubrieron toda la pila tecnológica incluyendo un backend Java/Spring Boot, frontend público Angular y una interfaz de administración basada en Lit. Las pruebas automatizadas a través de pipelines CI/CD de GitHub Actions alcanzaron tasas de aprobación del 100% en pruebas unitarias (2,290 casos), pruebas de integración (442 casos) y pruebas de extremo a extremo (88 casos), con una cobertura de código agregada del 85.76%. Las pruebas de rendimiento con K6 validaron el comportamiento del sistema bajo carga, el fuzzing con Jazzer confirmó la robustez del manejo de entradas, y el análisis estático con SonarQube identificó mejoras en la calidad del código. Los resultados demuestran la efectividad de los enfoques de pruebas multicapa para asegurar la calidad en aplicaciones web complejas.
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
  index-terms: (
    "Pruebas de Software",
    "Automatización de Pruebas",
    "CI/CD",
    "Código Abierto",
    "Gestión de Eventos",
    "Aseguramiento de Calidad",
  ),
  bibliography: bibliography("refs.bib"),
  figure-supplement: [Fig.],
)

#include "sections-es/introduction.typ"
#include "sections-es/background.typ"
#include "sections-es/study_case.typ"
#include "sections-es/methods.typ"
#include "sections-es/results.typ"
#include "sections-es/conclusion.typ"
#include "sections-es/future_work.typ"
