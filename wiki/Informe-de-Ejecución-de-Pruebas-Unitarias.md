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
