# Informe de Ejecución de Pruebas Unitarias del Sistema alf.io

## Índice

- [1. Introducción](#1-introducción)
- [2. Propósito](#2-propósito)
- [3. Alcance](#3-alcance)
- [4. Referencias](#4-referencias)
- [5. Entorno de pruebas](#5-entorno-de-pruebas)
- [6. Configuración del entorno de ejecución](#6-configuración-del-entorno-de-ejecución)
- [7. Resultados de ejecución](#7-resultados-de-ejecución)
- [8. Cobertura de código](#8-cobertura-de-código)
- [9. Cumplimiento de criterios de finalización](#9-cumplimiento-de-criterios-de-finalización)
- [10. Métricas adicionales](#10-métricas-adicionales)
- [11. Commits con Correcciones Funcionales](#11-commits-con-correcciones-funcionales)
- [12. Conclusión](#12-conclusión)

## 1. Introducción

El presente informe documenta la ejecución y resultados de las pruebas unitarias realizadas sobre alf.io, un sistema de gestión y venta de entradas para eventos de código abierto. Se presentan las métricas de ejecución y cobertura alcanzadas por la suite de pruebas unitarias de backend (Java/Spring Boot) y frontend (Angular y Lit).

## 2. Propósito

Este documento sirve como referencia para:

- Presentar los resultados cuantitativos de la ejecución de pruebas unitarias por aplicación.
- Mostrar los niveles de cobertura de código alcanzados.
- Documentar la configuración utilizada para ejecutar las pruebas de manera reproducible.

## 3. Alcance

Las pruebas unitarias cubren los siguientes componentes de alf.io:

- **Backend (Java 17, Spring Boot 3.5.x, Jetty):** Managers de negocio (`ReservationManager`, `PaymentManager`, `SystemManager`, `UserManager`, `WalletManager`, `CheckInManager`, `TicketManager`, `EventManager`), controladores REST, repositorios de datos, utilidades y jobs.
- **Frontend público (Angular 17):** Componentes de compra de entradas, servicios de comunicación con la API, guards y validaciones de formularios.
- **Frontend de administración (Lit + Shoelace + Vite):** Componentes de administración de eventos, servicios compartidos y utilidades de UI.

Los elementos excluidos del alcance son: pruebas de integración con base de datos real, pruebas E2E, pruebas de rendimiento, pruebas de seguridad avanzadas, pruebas con pasarelas de pago reales y pruebas de aceptación del usuario (UAT).

## 4. Referencias

- **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software.
- **Documentación oficial:** [https://alf.io](https://alf.io)
- **Repositorio oficial:** [https://github.com/alfio-event/alf.io](https://github.com/alfio-event/alf.io)
- **Plan de Pruebas Unitarias:** [[Plan-de-Pruebas-Unitarias]]

## 5. Entorno de pruebas

Las pruebas se ejecutan de manera remota mediante GitHub Actions, que ejecuta las suites de pruebas unitarias de backend y frontend en cada Pull Request y en cada push a la rama `main`. Los reportes de cobertura se publican automáticamente en GitHub Pages para su revisión por el contribuidor y el equipo.

## 6. Configuración del entorno de ejecución

### 6.1 Infraestructura de CI/CD

La ejecución de pruebas unitarias está gestionada por GitHub Actions mediante dos workflows principales:

#### `test-pr.yml` — Ejecución en Pull Requests

Se ejecuta automáticamente ante la creación o actualización de un Pull Request hacia `main`. Realiza los siguientes pasos:

1. Configura JDK 17 (Temurin), pnpm 11.1.2 y Node.js 22.
2. Ejecuta las pruebas de backend con `./gradlew test jacocoTestReport -Dpgsql.version=16`.
3. Ejecuta las pruebas de frontend con `pnpm --prefix frontend run coverage` (admin y público).
4. Prepara los reportes de cobertura (JaCoCo y Vitest) y los despliega en GitHub Pages.
5. Publica un comentario en el PR con el enlace a los reportes de cobertura.

#### `test-push.yml` — Ejecución al hacer push a `main`

Se ejecuta tras un merge en `main`. Realiza los mismos pasos que el workflow de PR, pero además ejecuta las pruebas de backend contra múltiples versiones de PostgreSQL (10, 15 y 16) usando una matriz de estrategia para garantizar compatibilidad.

### 6.2 Herramientas de cobertura

- **Backend:** JaCoCo (integrado con Gradle vía el plugin `jacocoTestReport`).
- **Frontend:** Vitest con `@vitest/coverage-v8`, tanto para el frontend público (Angular) como para el frontend admin (Lit).

### 6.3 Comandos de ejecución

| Componente | Comando | Herramienta |
| :--- | :--- | :--- |
| Backend | `./gradlew test jacocoTestReport -Dpgsql.version=16` | JUnit 5 + JaCoCo |
| Frontend público | `pnpm --prefix frontend/projects/public test:run` | Vitest |
| Frontend admin | `pnpm --prefix frontend/admin test:run` | Vitest |
| Cobertura frontend | `pnpm --prefix frontend run coverage` | Vitest + @vitest/coverage-v8 |

## 7. Resultados de ejecución

### 7.1 Backend

| Métrica | Valor |
| :--- | :---: |
| Tests totales | 1 636 |
| Tests exitosos | 1 634 |
| Tests con fallo | 0 |
| Tests con error | 0 |
| Tests omitidos | 2 |
| Tasa de éxito | 100% |

### 7.2 Frontend público (Angular)

| Métrica | Valor |
| :--- | :---: |
| Archivos de prueba | 38 |
| Tests totales | 487 |
| Tests exitosos | 487 |
| Tests con fallo | 0 |
| Tasa de éxito | 100% |

### 7.3 Frontend de administración (Lit)

| Métrica | Valor |
| :--- | :---: |
| Archivos de prueba | 8 |
| Tests totales | 167 |
| Tests exitosos | 167 |
| Tests con fallo | 0 |
| Tasa de éxito | 100% |

### 7.4 Resumen

| Aplicación | Tests totales | Exitosos | Tasa de éxito |
| :--- | :---: | :---: | :---: |
| Backend | 1 636 | 1 634 | 100% |
| Frontend público | 487 | 487 | 100% |
| Frontend admin | 167 | 167 | 100% |
| **Total** | **2 290** | **2 288** | **100%** |

## 8. Cobertura de código

### 8.1 Backend (JaCoCo)

<img src="images/unit-tests/backend_cov.png" alt="Cobertura Backend JaCoCo">

| Métrica | Valor |
| :--- | :---: |
| Instrucciones cubiertas | 85% (61 987 / 72 349) |
| Ramas cubiertas | 68% (3 228 / 4 730) |
| Líneas cubiertas | 85% (5 707 / 7 570) |
| Métodos cubiertos | 81% (2 701 / 3 325) |
| Clases | 352 |
