# Diseño de Casos de Prueba Funcionales del Sistema alf.io

## Índice
- [1. Propósito](#1-propósito)
- [2. Alcance de las Pruebas](#2-alcance-de-las-pruebas)
- [3. Referencias](#3-referencias)
- [4. Análisis de Riesgo de Calidad](#4-análisis-de-riesgo-de-calidad)
- [5. Criterios de Entrada y Salida](#5-criterios-de-entrada-y-salida)
- [6. Entorno y Datos de Prueba](#6-entorno-y-datos-de-prueba)
- [7. Niveles de Prueba](#7-niveles-de-prueba)
- [8. Técnicas de Prueba](#8-técnicas-de-prueba)
- [9. Diseño de Casos de Prueba](#9-diseño-de-casos-de-prueba)
- [10. Matriz de Trazabilidad](#10-matriz-de-trazabilidad)
- [11. Métodos y Herramientas](#11-métodos-y-herramientas)
- [12. Conclusiones](#12-conclusiones)

## 1. Propósito

El propósito de este documento es definir y estructurar el diseño de casos de prueba funcionales para el sistema alf.io, con el fin de asegurar la calidad funcional del sistema mediante la aplicación de técnicas de prueba de caja negra.

Entre los objetivos específicos se incluyen: establecer casos de prueba basados en los requisitos funcionales, aplicar técnicas como partición de equivalencia, análisis de valores límite, tablas de decisión y transición de estados y desarrollar una matriz de trazabilidad entre requisitos y pruebas.

## 2. Alcance de las Pruebas

### 2.1 Funcionalidades en Alcance

- **Creación y configuración de eventos:** Validar la capacidad de los administradores y organizadores para crear eventos, configurar categorías de tickets, precios y cupos.
- **Proceso de reserva y compra de tickets:** Flujo completo desde la selección del ticket por parte del asistente hasta la confirmación de la reserva y generación de la entrada.
- **Autenticación y autorización:** Verificación de roles (administrador global, propietario del evento, personal del evento) y control de acceso a las rutas.
- **Integración de pagos (Modo Test):** Simulación de pagos exitosos y fallidos utilizando proveedores compatibles (ej. Stripe Test Mode o pagos offline).

### 2.2 Funcionalidades Fuera de Alcance

- **Integraciones reales con pasarelas de pago:** Para evitar transacciones financieras reales y costos asociados, todas las pruebas se realizarán en modo de pruebas (sandbox).
- **Pruebas de estrés y rendimiento:** El enfoque de este plan es puramente funcional. El rendimiento bajo carga será abordado en una fase o plan de pruebas independiente.
- **Generación masiva de facturas PDF:** Solo se probarán las facturas generadas unitariamente durante el flujo normal de reserva.

## 3. Referencias

1. Estándares de Ingeniería de Software y Pruebas
   - **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software (conceptos, procesos, documentación y técnicas de diseño de pruebas).
2. Documentación del Proyecto
   - **Documento de Plan de Pruebas Unitarias:** [[Plan de Pruebas Unitarias]]
   - **Repositorio oficial de alf.io:** [GitHub - alfio-event/alf.io](https://github.com/alfio-event/alf.io)
   - **Documentación de Arquitectura de alf.io:** [[Arquitectura]] del proyecto.

## 4. Criterios de Entrada y Salida

### 4.1 Criterios de Entrada

- El código de la aplicación (alf.io) debe estar compilado y desplegado correctamente en el entorno de pruebas.
- Las pruebas unitarias base deben pasar exitosamente en el entorno de CI (GitHub Actions).
- La base de datos de pruebas (PostgreSQL) debe estar inicializada con los esquemas actualizados.

### 5.2 Criterios de Salida

- Se han ejecutado todos los casos de prueba funcionales diseñados y documentados en la sección de la wiki correspondiente.
- Se han reportado todos los defectos encontrados durante la ejecución.

## 6. Entorno y Datos de Prueba

### 6.1 Entorno de Pruebas

- **Servidor / Hosting:** Entorno remoto de pruebas configurado con Kubernetes, replicando la arquitectura de producción.
- **Base de datos:** PostgreSQL en versión 16, compatible con los requerimientos actuales del proyecto, corriendo en un pod aislado.
- **Navegadores soportados:** Pruebas funcionales frontend orientadas a las últimas versiones estables de Google Chrome (148.0.7778.215) y Mozilla Firefox (151.0.2).

### 6.2 Datos de Prueba

- **Cuentas de usuario:** Cuenta de Administrador Global pre-configurada (datos de las variables de entorno de prueba).

## 8. Técnicas de Prueba

### 8.1 Técnicas de Caja Negra

Se utilizarán técnicas formales de diseño de pruebas basadas en los requisitos funcionales del sistema de reservas:

- **Partición de equivalencia y Valores límite:**
  - Se aplicará especialmente en los campos de entrada como la cantidad de tickets a comprar (ej: 0, 1, límite máximo de tickets permitidos por transacción, y por encima del límite).
  - Fechas de inicio y fin de eventos y validez de categorías de tickets.
  - Precios de tickets y aplicación de códigos de descuento.
- **Transición de estados:**
  - Verificación de los diferentes estados de un ticket (reservado, pagado, cancelado, verificado en puerta).

## 9. Diseño de Casos de Prueba

En esta sección se documentarán los casos de prueba específicos organizados por funcionalidad, empleando la estructura definida anteriormente:

... (proceso de diseño de cada conjunto de pruebas)

## 10. Matriz de Trazabilidad

En esta sección se relacionan los requisitos funcionales con los casos de prueba que los verifican:

| Requisito Funcional | Casos de Prueba Asociados |
| :--- | :--- |

## 11. Métodos y Herramientas

### 11.1 Gestión y Planificación

- **Gestión de tareas, casos de prueba y defectos:** GitHub Issues (empleando templates estandarizados) y GitHub Projects para visualización en tablero Kanban.
- **Documentación de Pruebas:** GitHub Wiki del repositorio para el mantenimiento de este plan de pruebas y documentación relacionada.

### 11.3 Automatización e Integración Continua

- **CI/CD:** GitHub Actions para ejecución automática de pruebas unitarias en cada Pull Request.
- **Reportes de Cobertura:** Herramientas como JaCoCo (backend) y herramientas de cobertura de Vitest (frontend) integradas en el pipeline de GitHub Actions.

## 12. Conclusiones

Las técnicas de caja negra aplicadas al diseño de casos de prueba para alf.io permiten estructurar una validación eficiente y exhaustiva, orientada a descubrir errores de lógica en la gestión de eventos, reservas y pagos, así como defectos en la interacción con el usuario. Al combinar partición de equivalencia, análisis de valores límite y transición de estados, se optimiza la selección de casos de prueba para cubrir amplios escenarios de entrada sin redundancias innecesarias.

La organización del diseño en casos de prueba estandarizados y la matriz de trazabilidad proporcionan una visión clara del cubrimiento funcional, facilitando la ejecución, el seguimiento y la mejora continua del proceso de pruebas.
