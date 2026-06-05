# Diseño de Casos de Prueba Funcionales del Sistema alf.io

## Índice
- [1. Propósito](#1-propósito)
- [2. Alcance de las Pruebas](#2-alcance-de-las-pruebas)
- [3. Referencias](#3-referencias)
- [4. Criterios de Entrada y Salida](#4-criterios-de-entrada-y-salida)
- [5. Entorno y Datos de Prueba](#5-entorno-y-datos-de-prueba)
- [6. Técnicas de Prueba](#6-técnicas-de-prueba)
- [7. Diseño de Casos de Prueba](#7-diseño-de-casos-de-prueba)
- [8. Matriz de Trazabilidad](#8-matriz-de-trazabilidad)
- [9. Métodos y Herramientas](#9-métodos-y-herramientas)
- [10. Conclusiones](#10-conclusiones)

## 1. Propósito

El propósito de este documento es definir y estructurar el diseño de casos de prueba funcionales para el sistema alf.io, con el fin de asegurar la calidad funcional del sistema mediante la aplicación de técnicas de prueba de caja negra.

Entre los objetivos específicos se incluyen: establecer casos de prueba basados en los requisitos funcionales, aplicar técnicas como partición por equivalencia, análisis de valores límite, pruebas de casos de uso y tablas de decisión y desarrollar una matriz de trazabilidad entre requisitos y pruebas.

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

### 4.2 Criterios de Salida

- Se han ejecutado todos los casos de prueba funcionales diseñados y documentados en la sección de la wiki correspondiente.
- Se han reportado todos los defectos encontrados durante la ejecución.

## 5. Entorno y Datos de Prueba

### 5.1 Entorno de Pruebas

- **Servidor / Hosting:** Entorno remoto de pruebas configurado con Kubernetes, replicando la arquitectura de producción.
- **Base de datos:** PostgreSQL en versión 16, compatible con los requerimientos actuales del proyecto, corriendo en un pod aislado.
- **Navegadores soportados:** Pruebas funcionales frontend orientadas a las últimas versiones estables de Google Chrome (148.0.7778.215) y Mozilla Firefox (151.0.2).

### 5.2 Datos de Prueba

- **Cuentas de usuario:** Cuenta de Administrador Global pre-configurada (datos de las variables de entorno de prueba).

## 6. Técnicas de Prueba

### 6.1 Técnicas de Caja Negra

Se aplicarán las siguientes técnicas de diseño de pruebas basadas en los requisitos funcionales del sistema:

- **Partición por equivalencia:** Los datos de entrada se agrupan en clases válidas e inválidas. Por ejemplo, los roles de usuario (administrador, organizador, operador de check-in) se prueban para verificar que cada uno tenga acceso exclusivo a las operaciones permitidas.
- **Análisis de valores límite:** Se prueban valores en los extremos de los rangos permitidos, como límites de caracteres en campos de texto, fechas próximas al evento, cupos mínimos y máximos de entradas, y montos de dinero en los límites de precisión de BigDecimal.
- **Pruebas de casos de uso:** Se recorren paso a paso los flujos principales del sistema: creación de un evento, configuración de categorías de entradas, proceso de compra, generación de entradas (incluyendo Apple Wallet), check-in y reporting.
- **Tablas de decisión:** Se aplican para validar reglas complejas de negocio, como el cálculo de precios con descuentos, impuestos (IVA), tarifas de servicio y promociones combinadas, donde múltiples condiciones booleanas determinan el resultado final.

## 7. Diseño de Casos de Prueba

En esta sección se documentarán los casos de prueba específicos organizados por funcionalidad, empleando la estructura definida anteriormente:

... (proceso de diseño de cada conjunto de pruebas)

## 8. Matriz de Trazabilidad

En esta sección se relacionan los requisitos funcionales con los casos de prueba que los verifican:

| Requisito Funcional | Casos de Prueba Asociados |
| :--- | :--- |

## 9. Métodos y Herramientas

### 9.1 Gestión y Planificación

- **Gestión de tareas, casos de prueba y defectos:** GitHub Issues (empleando templates estandarizados) y GitHub Projects para visualización en tablero Kanban.
- **Documentación de Pruebas:** GitHub Wiki del repositorio para el mantenimiento de este plan de pruebas y documentación relacionada.

### 9.2 Automatización e Integración Continua

- **CI/CD:** GitHub Actions para ejecución automática de pruebas unitarias en cada Pull Request.
- **Reportes de Cobertura:** Herramientas como JaCoCo (backend) y herramientas de cobertura de Vitest (frontend) integradas en el pipeline de GitHub Actions.

## 10. Conclusiones

Las técnicas de caja negra aplicadas al diseño de casos de prueba para alf.io permiten estructurar una validación eficiente y exhaustiva, orientada a descubrir errores de lógica en la gestión de eventos, reservas y pagos, así como defectos en la interacción con el usuario. Al combinar partición por equivalencia, análisis de valores límite, pruebas de casos de uso y tablas de decisión, se optimiza la selección de casos de prueba para cubrir amplios escenarios de entrada sin redundancias innecesarias.

La organización del diseño en casos de prueba estandarizados y la matriz de trazabilidad proporcionan una visión clara del cubrimiento funcional, facilitando la ejecución, el seguimiento y la mejora continua del proceso de pruebas.
