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

### Edición de Tickets Adquiridos (Nombre, Apellido y Correo)
| ID | CPF-0001 |
| :--- | :--- |
| **Funcionalidad** | Edición de información del asistente post-compra |
| **Descripción** | Permite a un administrador modificar los datos críticos (nombre, apellido y correo) de un ticket que ya ha sido emitido, validando la integridad de los datos. |
| **Requisito Asociado** | RF-001 (Gestión de Tickets) |
| **Precondiciones** | El ticket debe estar en estado 'Purchased'. El usuario debe tener permisos de edición sobre la reserva. |
| **Datos de Entrada** | Nombre, Apellido, Correo Electrónico. |
| **Pasos de Ejecución** | 1. Acceder al detalle de la reserva. 2. Hacer clic en la opción de edición del ticket. 3. Modificar los campos de Nombre/Apellido o Correo. 4. Presionar el botón "Guardar". |
| **Técnicas de Pruebas** | Partición de Equivalencia, Análisis de Valores Límite. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Nombre/Apellido | Texto alfabético (1-255 caracteres) | Vacío, > 255 caracteres, incluye números. |
| Correo | Formato estándar (user@domain.com) | Vacío, formato incorrecto (sin @), longitud excesiva. |

**Valores Límite**
| Campo | Límite Inf. Válido | Límite Inf. No Válido | Límite Sup. Válido | Límite Sup. No Válido |
| :--- | :--- | :--- | :--- | :--- |
| Nombre/Apellido | 1 carácter | 0 caracteres (vacío) | 255 caracteres | 256 caracteres |
| Correo | Longitud mínima válida | 0 caracteres | Máximo soportado (64 chars local) | Excede límite (65 chars local) |

**Catálogo de Pruebas**
| #CP | Datos de Entrada | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-01-001 | Nombre: "" (vacío) | Rechazar: Error de campo obligatorio | f- |
| CPF-01-002 | Nombre: "A" (1 carácter) | Aceptar: Cambio guardado exitosamente | f+ |
| CPF-01-003 | Nombre: (Cadena de 254 caracteres) | Aceptar: Cambio guardado exitosamente | f+ |
| CPF-01-004 | Nombre: (Cadena de 255 caracteres) | Aceptar: Cambio guardado exitosamente | f+ |
| CPF-01-005 | Nombre: (Cadena de 256 caracteres) | Rechazar: Error de longitud excedida | f- |
| CPF-01-006 | Nombre: "Juan123" (con números) | Rechazar: Error de formato (solo letras) | f- |
| CPF-01-007 | Correo: "" (vacío) | Rechazar: Error de campo obligatorio | f- |
| CPF-01-008 | Correo: "aaaaa" (sin formato @) | Rechazar: Error de formato de correo | f- |
| CPF-01-009 | Correo con longitud de 63 caracteres | Aceptar: Cambio guardado exitosamente | f+ |
| CPF-01-010 | Correo con longitud de 64 caracteres | Aceptar: Cambio guardado exitosamente | f+ |
| CPF-01-011 | Correo con longitud de 65 caracteres | Rechazar: Error de longitud excedida | f- |

### Búsqueda de Reservas (Panel de Administración)
| ID | CPF-0002 |
| :--- | :--- |
| **Funcionalidad** | Búsqueda y filtrado de reservas |
| **Descripción** | Valida que el sistema de búsqueda administrativa recupere correctamente las reservas basándose en el ID único o el apellido del asistente. |
| **Requisito Asociado** | RF-002 (Búsqueda de Reservas) |
| **Precondiciones** | Deben existir reservas previas en el sistema. |
| **Datos de Entrada** | ID de reserva, Apellido. |
| **Pasos de Ejecución** | 1. Ingresar al listado de reservas. 2. Introducir el criterio en la barra de búsqueda. 3. Ejecutar la búsqueda. |
| **Técnicas de Pruebas** | Partición de Equivalencia. |
| **Prioridad** | Media |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Cod. | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| PE-B01 | ID de reserva existente en el sistema | ID inexistente o mal formado |
| PE-B02 | Apellido exacto de un comprador | Apellido que no figura en ninguna reserva |

**Catálogo de Pruebas**
| #CP | Criterio de Búsqueda | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-02-001 | ID: "ID-EXISTENTE" | El sistema muestra la reserva específica | f+ |
| CPF-02-002 | Apellido: "Pérez" | El sistema lista todas las reservas bajo ese apellido | f+ |
| CPF-02-003 | Valor: "ValorInexistente" | El sistema muestra mensaje "Sin resultados" | f- |

### Gestión de Estados de Reserva y Pagos
| ID | CPF-0003 |
| :--- | :--- |
| **Funcionalidad** | Transiciones de estado por pagos manuales |
| **Descripción** | Verifica que las reservas transicionen correctamente entre estados tras acciones manuales (aceptar/cancelar pago) y que la UI responda a las reglas de negocio. |
| **Requisito Asociado** | RF-003 (Gestión de Estados) |
| **Precondiciones** | Reserva en estado pendiente de pago. |
| **Datos de Entrada** | Interacción con botones de acción y modales. |
| **Pasos de Ejecución** | 1. Seleccionar una reserva pendiente. 2. Ejecutar acción de pago/cancelación. 3. Validar cambio de estado en la tabla. |
| **Técnicas de Pruebas** | Transición de Estados, Tablas de Decisión. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Transición de Estados**

![Diagrama de Transición de Estados](images/functional-tests/design/gestion-estados.png)

**Tabla de Decisión: Visibilidad del botón "Marcar como Completa"**
| Condición | C1 | C2 | C3 | C4 |
| :--- | :--- | :--- | :--- | :--- |
| ¿Se completó el llenado? | SI | SI | SI | NO |
| Tipo de Pago | Presencial | Offline | Proveedor | - |
| ¿Pago Aprobado? | NO | SI | NO | NO |
| **Mostrar Botón** | **NO** | **SI** | **NO** | **NO** |

**Catálogo de Pruebas**
| #CP | Acción / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-03-001 | Clic en "Aceptar Pago" -> Confirmar | La reserva cambia a estado COMPLETE | f+ |
| CPF-03-002 | Clic en "Cancelar Pago" -> Confirmar | La reserva cambia a estado CANCELLED | f+ |
| CPF-03-003 | Iniciar transición -> Clic fuera del modal | El modal se cierra y el estado se mantiene | f+ |
| CPF-03-004 | Llenado=SI, Pago=Offline, Aprobado=SI | Se visualiza el botón para marcar como completa | f+ |
| CPF-03-005 | Llenado=SI, Pago=Presencial, Aprobado=NO | El botón de marcar como completa se oculta | f+ |
| CPF-03-006 | Llenado=NO | El botón de marcar como completa se oculta | f+ |

### Disponibilidad de Descarga de Tickets
| ID | CPF-0004 |
| :--- | :--- |
| **Funcionalidad** | Lógica de emisión y descarga de entradas |
| **Descripción** | Valida las condiciones bajo las cuales un usuario puede descargar su ticket en PDF, basándose en la temporalidad, modalidad y estado financiero. |
| **Requisito Asociado** | RF-004 (Emisión de Entradas) |
| **Precondiciones** | Reserva realizada por el usuario. |
| **Técnicas de Pruebas** | Tablas de Decisión. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Tabla de Decisión: Mostrar botón de descarga de ticket**
| Condición | C1 | C2 | C3 | C4 | C5 | C6 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| ¿El evento ya pasó? | SI | NO | NO | NO | NO | NO |
| Modalidad del evento | - | Presencial | Presencial | Híbrido | Híbrido | Virtual |
| ¿Pago Aprobado? | - | SI | NO | SI | NO | - |
| **Mostrar Botón** | **NO** | **SI** | **NO** | **SI** | **NO** | **NO** |

**Catálogo de Pruebas**
| #CP | Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-04-001 | Fecha del evento en el pasado | El botón de descarga no se muestra | f- |
| CPF-04-002 | Futuro, Presencial, Pago aprobado | El botón de descarga es visible y funcional | f+ |
| CPF-04-003 | Futuro, Presencial, Pago pendiente | El botón de descarga permanece oculto | f- |
| CPF-04-004 | Futuro, Híbrido, Pago aprobado | El botón de descarga es visible y funcional | f+ |
| CPF-04-005 | Futuro, Híbrido, Pago pendiente | El botón de descarga permanece oculto | f- |
| CPF-04-006 | Futuro, Modalidad Virtual | El botón de descarga no se muestra (acceso digital) | f- |

Siguiendo exactamente el mismo estilo de tu documento, para **Creación de Usuarios** usaría:

* **Partición por Equivalencia** → validar nombres, correos, roles y username.
* **Valores Límite** → validar longitudes mínimas y máximas.
* **Tabla de Decisión** → verificar que todos los campos obligatorios estén presentes.

# Creación de Usuarios

| ID | CPF-0005 |
| :--- | :--- |
| **Funcionalidad** | Creación de usuarios |
| **Descripción** | Permite a un administrador registrar nuevos usuarios asignándoles una organización y un rol dentro del sistema. |
| **Requisito Asociado** | RF-006 (Creación de Usuarios) |
| **Precondiciones** | El usuario debe estar autenticado con permisos de administrador. |
| **Datos de Entrada** | Organización, rol, nombre de usuario, nombre, apellido y correo electrónico. |
| **Pasos de Ejecución** | 1. Acceder al módulo de usuarios. 2. Seleccionar "add new". 3. Completar los campos obligatorios. 4. Presionar "Save". |
| **Técnicas de Pruebas** | Partición por Equivalencia, Análisis de Valores Límite, Tabla de Decisión. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Partición de Equivalencia**

| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Organización | Organización existente | Vacía |
| Rol | Rol existente del sistema | Vacío |
| Username | Alfanumérico único | Vacío, duplicado |
| Nombre | Texto alfabético válido | Vacío, contiene caracteres inválidos |
| Apellido | Texto alfabético válido | Vacío, contiene caracteres inválidos |
| E-mail | Formato válido ([user@domain.com](mailto:user@domain.com)) | Vacío, formato incorrecto, duplicado |

**Valores Límite**

| Campo | Límite Inferior Válido | Límite Inferior No Válido | Límite Superior Válido | Límite Superior No Válido |
| :--- | :--- | :--- | :--- | :--- |
| Username | 1 carácter | 0 caracteres | 255 caracteres | 256 caracteres |
| Nombre | 1 carácter | 0 caracteres | 255 caracteres | 256 caracteres |
| Apellido | 1 carácter | 0 caracteres | 255 caracteres | 256 caracteres |
| E-mail | Longitud mínima válida | Vacío | Longitud máxima soportada | Excede límite |

**Tabla de Decisión: Validación de Campos Obligatorios para la Creación de Usuarios**

| Condición | C1 | C2 | C3 | C4 | C5 | C6 | C7 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Organización informada | SI | NO | SI | SI | SI | SI | SI |
| Rol informado | SI | SI | NO | SI | SI | SI | SI |
| Username informado | SI | SI | SI | NO | SI | SI | SI |
| Nombre informado | SI | SI | SI | SI | NO | SI | SI |
| Apellido informado | SI | SI | SI | SI | SI | NO | SI |
| E-mail informado | SI | SI | SI | SI | SI | SI | NO |
| **Crear Usuario** | **SI** | **NO** | **NO** | **NO** | **NO** | **NO** | **NO** |

**Acciones**

| Acción | C1 | C2 | C3 | C4 | C5 | C6 | C7 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Permitir guardar usuario    |  X  |     |     |     |     |     |     |
| Mostrar error de validación |     |  X  |  X  |  X  |  X  |  X  |  X  |

**Catálogo de Pruebas**

**Catálogo de Pruebas**

| #CP | Datos de Entrada | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-05-001 | Username: "usuario_existente" | Error: Username ya registrado | f- |
| CPF-05-002 | E-mail: "anarodriguez.com" | Error: Formato de correo inválido | f- |
| CPF-05-003 | E-mail: "ana@" | Error: Formato de correo inválido | f- |
| CPF-05-004 | E-mail: "usuario.existente@techevents.com" | Error: Correo ya registrado | f- |
| CPF-05-005 | Username: "   " (solo espacios en blanco) | Error: Username obligatorio o inválido | f- |
| CPF-05-006 | Username: "aa" | Usuario creado exitosamente | f+ |
| CPF-05-007 | Username: (255 caracteres) | Usuario creado exitosamente | f+ |
| CPF-05-008 | Username: (256 caracteres) | Error: Username excede la longitud permitida | f- |
| CPF-05-009 | Nombre: "   " (solo espacios en blanco) | Error: Nombre obligatorio o inválido | f- |
| CPF-05-010 | Nombre: "AA" | Usuario creado exitosamente | f+ |
| CPF-05-011 | Nombre: "33" | Error: Nombre no puede ser un número | f- |
| CPF-05-012 | Nombre: "$$" | Error: Nombre no puede ser un símbolo | f- |
| CPF-05-013 | Nombre: (255 caracteres) | Usuario creado exitosamente | f+ |
| CPF-05-014 | Nombre: (256 caracteres) | Error: Nombre excede la longitud permitida | f- |
| CPF-05-015 | Apellido: "   " (solo espacios en blanco) | Error: Apellido obligatorio o inválido | f- |
| CPF-05-016 | Apellido: "RR" | Usuario creado exitosamente | f+ |
| CPF-05-017 | Apellido: "33" | Error: Apellido no puede ser un número | f- |
| CPF-05-018 | Apellido: "$$" | Error: Apellido no puede ser un símbolo | f- |
| CPF-05-019 | Apellido: (255 caracteres) | Usuario creado exitosamente | f+ |
| CPF-05-020 | Apellido: (256 caracteres) | Error: Apellido excede la longitud permitida | f- |
| CPF-05-021 | Organización: "" (vacía), Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-05-022 | Organización: "AA", Rol: "" (vacío), Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-05-023 | Organización: "AA", Rol: "Organization owner", Username: "" (vacío), Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-05-024 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "" (vacío), Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-05-025 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "" (vacío), E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-05-026 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "" (vacío) | El sistema muestra error de validación y no permite guardar | f- |
| CPF-05-027 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | Usuario creado exitosamente | f+ |

## Matriz de Trazabilidad

En esta sección se relacionan los requisitos funcionales con los casos de prueba que los verifican:

| Requisito Funcional | Casos de Prueba Asociados |
| :--- | :--- |
| **RF-001:** Gestión de información del asistente (Edición de Ticket) | CPF-0001 (001-011) |
| **RF-002:** Búsqueda administrativa de reservas | CPF-0002 (001-003) |
| **RF-003:** Gestión de estados y flujos de pago | CPF-0003 (001-006) |
| **RF-004:** Emisión y visualización de entradas (PDF) | CPF-0004 (001-006) |
| **RF-005:** Creación de Usuarios | CPF-0005 (001-030) |

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
