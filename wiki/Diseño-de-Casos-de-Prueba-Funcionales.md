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
| Campo | Clase Válida | Clases No Válidas |
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



**Tabla de Decisión: Visibilidad del botón "Marcar como Completa"**
| Condición | C1 | C2 | C3 | C4 |
| :--- | :--- | :--- | :--- | :--- |
| ¿Se completó el llenado? | SI | SI | SI | NO |
| Tipo de Pago | Presencial | Offline | Proveedor | - |
| ¿Pago Aprobado? | NO | SI | NO | NO |
| **Mostrar Botón** | **NO** | **SI** | **NO** | **NO** |

**Transición de Estados**

![Diagrama de Transición de Estados](images/functional-tests/design/gestion-estados.png)

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
| **Datos de Entrada** |  |
| **Pasos de Ejecución** | |
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

### Check-in Online (Auto-check-in)
| ID | CPF-0012 |
| :--- | :--- |
| **Funcionalidad** | Proceso de auto-check-in por parte del usuario asistente |
| **Descripción** | Valida si un usuario asistente puede realizar el check-in digital de su ticket de manera autónoma desde la interfaz web. |
| **Requisito Asociado** | RF-005 (Auto-Check-in) |
| **Precondiciones** | El usuario posee un enlace válido a la página de su ticket personal. |
| **Datos de Entrada** |  |
| **Pasos de Ejecución** | |
| **Técnicas de Pruebas** | Tablas de Decisión. |
| **Prioridad** | Alta |

**Análisis de Técnicas**



**Tabla de Decisión: Habilitar botón de auto-check-in**
| Condición | C1 | C2 | C3 | C4 | C5 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ¿Auto-check-in habilitado en el evento? | NO | SI | SI | SI | SI |
| ¿Estado del ticket es pagado/aprobado? | - | NO | SI | SI | SI |
| ¿Está dentro del rango de tiempo permitido? | - | - | NO | SI | SI |
| ¿El ticket ya fue ingresado/usado? | - | - | - | SI | NO |
| **Habilitar Botón / Permitir Acción** | **NO** | **NO** | **NO** | **NO** | **SI** |


**Catálogo de Pruebas**
| #CP | Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-12-001 | Auto-check-in deshabilitado en evento | El botón no aparece | f- |
| CPF-12-002 | Con pago pendiente | El botón se muestra inactivo o bloqueado | f- |
| CPF-12-003 | Fuera de la ventana de tiempo (muy temprano/tarde) | El botón permanece deshabilitado o muestra un aviso con la hora exacta de habilitación. | f- |
| CPF-12-004 | Ticket ya ingresado | El botón se oculta o cambia a estado "Ingresado" | f- |
| CPF-12-005 | Condiciones válidas (Habilitado, pagado, a tiempo, sin usar) | Botón visible y funcional; al hacer clic cambia el estado a "Checked-In" | f+ |

### Validación de QR (Escaneo de Ticket en Puerta)
| ID | CPF-0013 |
| :--- | :--- |
| **Funcionalidad** | Validación y control de acceso mediante códigos QR |
| **Descripción** | Define el comportamiento e indicativo visual del lector de entrada según el estado y validez del QR escaneado. |
| **Requisito Asociado** | RF-006 (Control de Acceso) |
| **Precondiciones** | Dispositivo móvil de puerta logueado en la aplicación de check-in del evento. |
| **Datos de Entrada** |  |
| **Pasos de Ejecución** | |
| **Técnicas de Pruebas** | Tablas de Decisión. |
| **Prioridad** | Crítica |

**Análisis de Técnicas**



**Tabla de Decisión: Resultado visual del escaneo**
| Condición | C1 | C2 | C3 | C4 |
| :--- | :--- | :--- | :--- | :--- |
| ¿El ticket existe en el sistema? | NO | SI | SI | SI |
| ¿El estado del ticket es "Cancelado"? | - | SI | NO | NO |
| ¿El ticket ya figura como ingresado? | - | - | SI | NO |
| **Resultado de Escaneo** | **Rojo (Inexistente)** | **Rojo (Cancelado)** | **Amarillo (Duplicado)** | **Verde (Éxito)** |


**Catálogo de Pruebas**
| #CP | Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-13-001 | Escaneo de código QR inválido/inexistente | Pantalla roja indicando: "Ticket no encontrado" | f- |
| CPF-13-002 | Escaneo de ticket cancelado previamente | Pantalla roja indicando: "Acceso denegado - Ticket Cancelado" | f- |
| CPF-13-003 | Escaneo de ticket ya ingresado | Pantalla amarilla indicando: "Alerta - Ticket duplicado" (con fecha/hora del 1er ingreso) | f- |
| CPF-13-004 | Escaneo de ticket válido por primera vez | Pantalla verde indicando: "Acceso Permitido" y registra el ingreso | f+ |

### Generación de Acreditaciones (Badges)
| ID | CPF-0014 |
| :--- | :--- |
| **Funcionalidad** | Emisión e impresión de credenciales físicas |
| **Descripción** | Determina si el sistema permite la descarga/impresión del badge o carnet del asistente en PDF según las reglas del evento y del ticket. |
| **Requisito Asociado** | RF-007 (Generación de Acreditaciones) |
| **Precondiciones** | Diseño de badge cargado y asignado para el evento. |
| **Datos de Entrada** |  |
| **Pasos de Ejecución** | |
| **Técnicas de Pruebas** | Tablas de Decisión. |
| **Prioridad** | Media |

**Análisis de Técnicas**



**Tabla de Decisión: Permitir descarga e impresión de Badge**
| Condición | C1 | C2 | C3 | C4 | C5 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ¿Categoría de ticket incluye badge? | NO | SI | SI | SI | SI |
| ¿El ticket está confirmado y pagado? | - | NO | SI | SI | SI |
| ¿Evento requiere check-in previo para badge? | - | - | NO | SI | SI |
| ¿Asistente ya realizó el check-in físico? | - | - | - | NO | SI |
| **Permitir Descarga de PDF** | **NO** | **NO** | **SI** | **NO** | **SI** |


**Catálogo de Pruebas**
| #CP | Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-14-001 | Categoría sin derecho a badge (ej. Pase Virtual) | El botón o enlace de descarga de badge no está visible | f- |
| CPF-14-002 | Ticket con derecho a badge pero pago pendiente | Se muestra un aviso indicando que requiere pago completo para emitir | f- |
| CPF-14-003 | Ticket pagado, evento sin restricción de check-in previo | El botón es visible y permite descargar el PDF del badge antes del evento | f+ |
| CPF-14-004 | Ticket pagado, requiere check-in previo, pero no ha ingresado | El botón de badge permanece inactivo o ausente en el portal del usuario | f- |
| CPF-14-005 | Ticket pagado, requiere check-in y ya ingresó al evento | El botón se activa en el panel de puerta/usuario y descarga el PDF generado | f+ |

### Configuración de la Organización (CONF-01)
| ID | CPF-0005 |
| :--- | :--- |
| **Funcionalidad** | Configuración de la Organización |
| **Descripción** | Permite registrar y modificar la información de una organización, incluyendo el nombre, descripción, correo de contacto, y otros campos relacionados. |
| **Requisito Asociado** | RF-CONF-01 (Configuración de la Organización) |
| **Precondiciones** | El usuario debe estar autenticado con rol de Administrador Global. |
| **Datos de Entrada** | Nombre de la organización, descripción, correo electrónico de contacto. |
| **Pasos de Ejecución** | 1. Acceder al panel de administración. <br>Ir a la sección de Organizaciones. <br>Crear una nueva organización o seleccionar una existente para editar. <br>Completar/modificar los campos de información. <br>Guardar los cambios. |
| **Técnicas de Pruebas** | Partición de Equivalencia, Análisis de Valores Límite. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Nombre | Texto no vacío (1-255 caracteres) | Vacío, > 255 caracteres |
| Correo | Formato estándar de correo | Vacío, formato incorrecto (sin @) |

**Valores Límite**
| Campo | Límite Inf. Válido | Límite Inf. No Válido | Límite Sup. Válido | Límite Sup. No Válido |
| :--- | :--- | :--- | :--- | :--- |
| Nombre | 1 carácter | 0 caracteres (vacío) | 255 caracteres | 256 caracteres |



**Catálogo de Pruebas**
| #CP | Datos de Entrada / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-05-001 | Crear organización con datos válidos | Registro y redirección exitosa. | f+ |
| CPF-05-002 | Intentar crear organización con nombre vacío | Rechazar guardado indicando campo requerido. | f- |
| CPF-05-003 | Nombre con caracteres especiales permitidos | Registro y redirección exitosa. | f+ |
| CPF-05-004 | Correo de contacto inválido | Rechazar indicando formato de correo incorrecto. | f- |
| CPF-05-005 | Modificar datos de una organización existente de forma exitosa | Registro y redirección exitosa. | f+ |
| CPF-05-006 | Cambiar el nombre de una organización por uno ya existente | Rechazar indicando que el nombre ya existe. | f- |

### Configuración del Evento (CONF-02)
| ID | CPF-0006 |
| :--- | :--- |
| **Funcionalidad** | Configuración del Evento |
| **Descripción** | Permite crear y modificar las propiedades básicas de un evento, incluyendo fechas, descripción y códigos de acceso. |
| **Requisito Asociado** | RF-CONF-02 (Configuración del Evento) |
| **Precondiciones** | El usuario debe estar autenticado y tener permisos de edición sobre el evento. |
| **Datos de Entrada** | Nombre del evento, fecha de inicio, fecha de fin, descripción, códigos de acceso. |
| **Pasos de Ejecución** | 1. Acceder al panel de administración del evento. <br>Configurar las fechas e información básica. <br>Configurar disponibilidad y fin de venta para las categorías. <br>Guardar los cambios. |
| **Técnicas de Pruebas** | Partición de Equivalencia. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Fechas | Fecha de fin posterior a la de inicio | Fecha de inicio en el pasado, fecha de fin anterior a la de inicio |
| Códigos Ocultos | Código único por categoría | Códigos duplicados en categorías del mismo evento |




**Catálogo de Pruebas**
| #CP | Datos de Entrada / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-06-001 | Crear evento con datos válidos | Guardar exitosamente. | f+ |
| CPF-06-002 | Crear evento con fecha de inicio en el pasado | Rechazar indicando error en la fecha. | f- |
| CPF-06-003 | Crear evento con fecha de fin anterior a la de inicio | Rechazar indicando incoherencia en las fechas. | f- |
| CPF-06-004 | Modificar la descripción de un evento existente | Guardar los cambios de forma exitosa. | f+ |
| CPF-06-005 | Configurar disponibilidad de categoría después de inicio del evento | Guardar la configuración correctamente. | f+ |
| CPF-06-006 | Configurar fin de venta de categoría después del fin del evento | Guardar la configuración correctamente. | f+ |
| CPF-06-007 | Configurar códigos ocultos duplicados en diferentes categorías | Guardar el mismo código en múltiples categorías. | f+ |

### Configuración de Categorías de Tickets (CONF-03)
| ID | CPF-0007 |
| :--- | :--- |
| **Funcionalidad** | Configuración de Categorías de Tickets |
| **Descripción** | Permite configurar los tipos de tickets, sus precios, disponibilidad, si son internos u ocultos con código de acceso. |
| **Requisito Asociado** | RF-CONF-03 (Configuración de Categorías de Tickets) |
| **Precondiciones** | El evento debe estar creado y en estado de configuración. |
| **Datos de Entrada** | Nombre de la categoría, precio, cantidad de tickets, código de acceso. |
| **Pasos de Ejecución** | 1. Acceder a la sección de categorías de tickets en el evento. <br>Modificar el precio o crear una categoría nueva. <br>Configurar código de acceso si es una categoría oculta. <br>Guardar la configuración. |
| **Técnicas de Pruebas** | Tablas de Decisión, Partición de Equivalencia. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Precio | Valor decimal >= 0 | Valor decimal < 0 |


**Tabla de Decisión: Comportamiento por Categoría de Ticket**
| ID Caso de Prueba | Escenario / Columna Origen | Dato de Entrada: ¿Es Ticket Interno? | Dato de Entrada: Precio | Resultado Esperado: ¿Visible en página? | Resultado Esperado: ¿Requiere Pago? |
| :--- | :--- | :--- | :--- | :--- | :--- |
| CP-01 | C1 (Expansión del -) | SÍ | $15.00 (Precio > 0) | NO | NO |
| CP-02 | C2 (Expansión del -) | SÍ | $0.00 (Precio = 0) | NO | NO |
| CP-03 | C3 | NO | $25.50 (Precio > 0) | SÍ | SÍ (Validar pasarela Stripe/Offline) |
| CP-04 | C4 | NO | $0.00 (Precio = 0) | SÍ | NO (Debe dejar hacer checkout gratis) |


**Catálogo de Pruebas**
| #CP | Datos de Entrada / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-07-001 | Modificar el precio de una categoría existente | Guardar el cambio del precio exitosamente. | f+ |
| CPF-07-002 | Ingresar un precio negativo en una categoría | Rechazar indicando que el precio no puede ser negativo. | f- |
| CPF-07-003 | Configurar una categoría de tickets gratuitos (precio cero) | Guardar la categoría como gratuita y permitir checkout gratis. | f+ |
| CPF-07-004 | Configurar categoría VIP con precio diferenciado | Guardar la categoría VIP y reflejar su precio diferenciado. | f+ |
| CPF-07-005 | Crear categoría oculta con código de acceso | Crear la categoría oculta y requerir el código para su visualización. | f+ |
| CPF-07-006 | Eliminar categoría oculta como administrador | Remover exitosamente la categoría de la lista. | f+ |
| CPF-07-007 | Ticket interno, Precio > 0 (CP-01) | No visible en página y no requiere pago. | f- |
| CPF-07-008 | Ticket interno, Precio = 0 (CP-02) | No visible en página y no requiere pago. | f- |
| CPF-07-009 | Ticket no interno, Precio > 0 (CP-03) | Visible en página y requiere pago (Stripe/Offline). | f+ |
| CPF-07-010 | Ticket no interno, Precio = 0 (CP-04) | Visible en página y permite checkout gratis (no requiere pago). | f+ |

### Gestión de Capacidad (CONF-04)
| ID | CPF-0008 |
| :--- | :--- |
| **Funcionalidad** | Gestión de Capacidad |
| **Descripción** | Permite definir y controlar la capacidad máxima de asistentes del evento y de cada categoría de ticket individualmente. |
| **Requisito Asociado** | RF-CONF-04 (Gestión de Capacidad) |
| **Precondiciones** | El evento y las categorías deben estar configurados. |
| **Datos de Entrada** | Capacidad del evento, capacidad de la categoría, límite de tickets por transacción. |
| **Pasos de Ejecución** | 1. Configurar la capacidad máxima del evento y de las categorías. <br>Intentar registrar una compra superando los límites. <br>Verificar el comportamiento cuando se llega al límite. |
| **Técnicas de Pruebas** | Partición de Equivalencia, Análisis de Valores Límite. |
| **Prioridad** | Alta |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Capacidad | Total de categorías <= Capacidad del evento | Total de categorías > Capacidad del evento |
| Cantidad por Compra | Menor o igual al límite por transacción | Mayor al límite por transacción, cantidad <= 0 |




**Catálogo de Pruebas**
| #CP | Datos de Entrada / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-08-001 | Configurar categorías cuya capacidad supere el límite del evento | Impedir guardar o emitir una advertencia de capacidad. | f- |
| CPF-08-002 | Ingresar cantidad inválida o nula de tickets en una categoría | Rechazar indicando error en la capacidad. | f- |
| CPF-08-003 | Comprar el último ticket disponible de una categoría | Procesar la compra y actualizar la disponibilidad a cero. | f+ |
| CPF-08-004 | Comprar tickets respetando el límite máximo por transacción | Permitir la compra si está dentro del límite establecido. | f+ |
| CPF-08-005 | Verificar estado de categoría cuando se agotan los tickets | Deshabilitar la venta y mostrar la etiqueta "Sold out" (Agotado). | f- |

### Configuración de Impuestos (CONF-05)
| ID | CPF-0009 |
| :--- | :--- |
| **Funcionalidad** | Configuración de Impuestos |
| **Descripción** | Permite definir reglas de impuestos (VAT/IVA) y aplicarlas o eximirlas a categorías específicas. |
| **Requisito Asociado** | RF-CONF-05 (Configuración de Impuestos) |
| **Precondiciones** | La organización y el evento deben estar creados. |
| **Datos de Entrada** | Nombre del impuesto, tasa impositiva (porcentaje), categoría a aplicar. |
| **Pasos de Ejecución** | 1. Acceder al panel de administración del evento. <br>Configurar una nueva regla de impuestos. <br>Asociar o desvincular el impuesto a una categoría. |
| **Técnicas de Pruebas** | Partición de Equivalencia. |
| **Prioridad** | Media |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Tasa | Valor porcentual >= 0% | Valor porcentual < 0% |




**Catálogo de Pruebas**
| #CP | Datos de Entrada / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-09-001 | Configurar y aplicar un nuevo impuesto (VAT/IVA) | Guardar y aplicar el impuesto correctamente al precio de la categoría. | f+ |
| CPF-09-002 | Actualizar la tasa del impuesto configurado a un valor de 0% | Se actualiza la tasa a 0% de forma exitosa en el panel. | f+ |
| CPF-09-003 | Configurar y aplicar exención de impuestos (tax-free) a una categoría | Desvincular los impuestos del precio de la categoría. | f+ |

### Configuración de Localización y Moneda (CONF-06)
| ID | CPF-0010 |
| :--- | :--- |
| **Funcionalidad** | Configuración de Localización y Moneda |
| **Descripción** | Permite definir el idioma del sistema, la traducción de los detalles del evento, la zona horaria y la moneda por defecto del evento. |
| **Requisito Asociado** | RF-CONF-06 (Configuración de Localización y Moneda) |
| **Precondiciones** | El evento debe estar creado. |
| **Datos de Entrada** | Idioma principal, traducciones secundarias, zona horaria, moneda por defecto (EUR, PEN, etc.). |
| **Pasos de Ejecución** | 1. Ir al panel de configuración de localización del evento/organización. <br>Configurar el idioma de visualización y traducciones del evento. <br>Modificar la zona horaria y moneda por defecto. |
| **Técnicas de Pruebas** | Partición de Equivalencia. |
| **Prioridad** | Media |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Idioma | Al menos un idioma configurado en el sistema | Eliminar el último idioma restante |
| Zona Horaria | Zona horaria coherente con la del evento | Desfase de zona horaria detectado |




**Catálogo de Pruebas**
| #CP | Datos de Entrada / Escenario | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-10-001 | Seleccionar el idioma por defecto del sistema | Actualizar el idioma de visualización correctamente. | f+ |
| CPF-10-002 | Traducir los detalles del evento a un idioma secundario | Guardar traducciones y aplicarlas correctamente a los campos. | f+ |
| CPF-10-003 | Validar el límite mínimo de idiomas requeridos al intentar eliminar | Impedir la eliminación si solo queda un idioma configurado. | f- |
| CPF-10-004 | Validar advertencia por desfase de zona horaria del evento | Mostrar alerta explicativa sobre la discrepancia de zona horaria. | f- |
| CPF-10-005 | Cambiar la moneda por defecto del evento a Euros (EUR) | Actualizar la moneda a EUR y reflejarla en la tienda pública. | f+ |
| CPF-10-006 | Cambiar la moneda por defecto del evento a Soles (PEN) | Actualizar la moneda a PEN y reflejarla en la tienda pública. | f+ |

# Creación de Usuarios
| ID | CPF-011 |
| :--- | :--- |
| **Funcionalidad** | Creación de usuarios |
| **Descripción** | Permite a un administrador registrar nuevos usuarios asignándoles una organización y un rol dentro del sistema. |
| **Requisito Asociado** | RF-005 (Creación de Usuarios) |
| **Precondiciones** | El usuario debe estar autenticado con permisos de administrador. |
| **Datos de Entrada** | Organización, rol, nombre de usuario, nombre, apellido y correo electrónico. |
| **Pasos de Ejecución** | 1. Acceder al módulo de usuarios. 2. Seleccionar "add new". 3. Completar los campos obligatorios. 4. Presionar "Save". |
| **Técnicas de Pruebas** | Partición por Equivalencia, Análisis de Valores Límite, Tabla de Decisión. |
| **Prioridad** |  |

**Análisis de Técnicas**

**Partición de Equivalencia**
| Campo | Clase Válida | Clases No Válidas |
| :--- | :--- | :--- |
| Organización | Organización existente | Vacía |
| Rol | Rol existente del sistema | Vacío |
| Username | Alfanumérico único | Vacío, duplicado |
| Nombre | Texto alfabético válido | Vacío, contiene caracteres inválidos |
| Apellido | Texto alfabético válido | Vacío, contiene caracteres inválidos |
| E-mail | Formato válido (user@domain.com) | Vacío, formato incorrecto, duplicado |

**Valores Límite**
| Campo | Límite Inf. Válido | Límite Inf. No Válido | Límite Sup. Válido | Límite Sup. No Válido |
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
**Tabla de Decisión: Acciones**
| Acción | C1 | C2 | C3 | C4 | C5 | C6 | C7 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Permitir guardar usuario     | X |   |   |   |   |   |   |
| Mostrar error de validación |   | X | X | X | X | X | X |
| **** |  |


**Catálogo de Pruebas**
| #CP | Datos de Entrada | Resultado Esperado | Obs |
| :--- | :--- | :--- | :--- |
| CPF-11-001 | Username: "usuario_existente" | Error: Username ya registrado | f- |
| CPF-11-002 | E-mail: "anarodriguez.com" | Error: Formato de correo inválido | f- |
| CPF-11-003 | E-mail: "ana@" | Error: Formato de correo inválido | f- |
| CPF-11-004 | E-mail: "usuario.existente@techevents.com" | Error: Correo ya registrado | f- |
| CPF-11-005 | Username: "   " (solo espacios en blanco) | Error: Username obligatorio o inválido | f- |
| CPF-11-006 | Username: "aa" | Usuario creado exitosamente | f+ |
| CPF-11-007 | Username: (255 caracteres) | Usuario creado exitosamente | f+ |
| CPF-11-008 | Username: (256 caracteres) | Error: Username excede la longitud permitida | f- |
| CPF-11-009 | Nombre: "   " (solo espacios en blanco) | Error: Nombre obligatorio o inválido | f- |
| CPF-11-010 | Nombre: "AA" | Usuario creado exitosamente | f+ |
| CPF-11-011 | Nombre: "33" | Error: Nombre no puede ser un número | f- |
| CPF-11-012 | Nombre: "$$" | Error: Nombre no puede ser un símbolo | f- |
| CPF-11-013 | Nombre: (255 caracteres) | Usuario creado exitosamente | f+ |
| CPF-11-014 | Nombre: (256 caracteres) | Error: Nombre excede la longitud permitida | f- |
| CPF-11-015 | Apellido: "   " (solo espacios en blanco) | Error: Apellido obligatorio o inválido | f- |
| CPF-11-016 | Apellido: "RR" | Usuario creado exitosamente | f+ |
| CPF-11-017 | Apellido: "33" | Error: Apellido no puede ser un número | f- |
| CPF-11-018 | Apellido: "$$" | Error: Apellido no puede ser un símbolo | f- |
| CPF-11-019 | Apellido: (255 caracteres) | Usuario creado exitosamente | f+ |
| CPF-11-020 | Apellido: (256 caracteres) | Error: Apellido excede la longitud permitida | f- |
| CPF-11-021 | Organización: "" (vacía), Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-11-022 | Organización: "AA", Rol: "" (vacío), Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-11-023 | Organización: "AA", Rol: "Organization owner", Username: "" (vacío), Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-11-024 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "" (vacío), Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-11-025 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "" (vacío), E-mail: "carlos.valdez@techevents.com" | El sistema muestra error de validación y no permite guardar | f- |
| CPF-11-026 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "" (vacío) | El sistema muestra error de validación y no permite guardar | f- |
| CPF-11-027 | Organización: "AA", Rol: "Organization owner", Username: "cvaldez", Nombre: "Carlos", Apellido: "Valdez", E-mail: "carlos.valdez@techevents.com" | Usuario creado exitosamente | f+ |

## 8. Matriz de Trazabilidad

En esta sección se relacionan los requisitos funcionales con los casos de prueba que los verifican:

| Requisito Funcional | Casos de Prueba Asociados |
| :--- | :--- |
| **RF-001:** Gestión de información del asistente (Edición de Ticket) | CPF-0001 (001-011) |
| **RF-002:** Búsqueda administrativa de reservas | CPF-0002 (001-003) |
| **RF-003:** Gestión de estados y flujos de pago | CPF-0003 (001-006) |
| **RF-0004:** Emisión y visualización de entradas (PDF) | CPF-0004 (001-006) |
| **RF-005:** Creación de Usuarios | CPF-0011 (001-027) |
| **RF-CONF-01:** Configuración de la Organización | CPF-0005 (001-006) |
| **RF-CONF-02:** Configuración del Evento | CPF-0006 (001-007) |
| **RF-CONF-03:** Configuración de Categorías de Tickets | CPF-0007 (001-010) |
| **RF-CONF-04:** Gestión de Capacidad | CPF-0008 (001-005) |
| **RF-CONF-05:** Configuración de Impuestos | CPF-0009 (001-003) |
| **RF-CONF-06:** Configuración de Localización y Moneda | CPF-0010 (001-006) |
| **RF-005:** Auto-Check-in | CPF-0012 (001-005) |
| **RF-006:** Control de Acceso | CPF-0013 (001-004) |
| **RF-007:** Generación de Acreditaciones | CPF-0014 (001-005) |

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