# Informe de Casos de Prueba Funcionales del Sistema alf.io

## Índice
- [1. Introducción](#1-introducción)
- [2. Propósito](#2-propósito)
- [3. Alcance](#3-alcance)
- [4. Referencias](#4-referencias)
- [5. Entorno de pruebas](#5-entorno-de-pruebas)
- [6. Resultados de Pruebas Funcionales](#6-resultados-de-pruebas-funcionales)
- [7. Limitaciones](#7-limitaciones)
- [8. Estrategia y métodos de prueba aplicados](#8-estrategia-y-métodos-de-prueba-aplicados)
- [9. Conclusión](#9-conclusión)

## 1. Introducción

El presente informe documenta la estrategia, ejecución y resultados de las pruebas funcionales realizadas sobre alf.io, un sistema de gestión y venta de entradas para eventos de código abierto. Su propósito es evaluar la calidad del software, identificar defectos potenciales y verificar el cumplimiento de los requisitos del sistema, desde unidades de código hasta flujos completos de usuario.

## 2. Propósito

Este documento sirve como referencia para:

- Describir el enfoque de pruebas adoptado y los niveles de cobertura alcanzados.
- Detallar la configuración del entorno de pruebas.
- Proporcionar evidencia sobre el comportamiento del sistema en escenarios controlados.
- Facilitar la reproducibilidad de las pruebas por parte del equipo de desarrollo y QA.

## 3. Alcance

Las pruebas abarcan los siguiente componentes y funcionalidades de alf.io:

- **Backend (Java 17, Spring Boot 3.5, Jetty):** Lógica de negocio, repositorios de datos, flujos de reserva, pagos, generación de entradas, gestión de eventos y suscripciones, procesamiento de extensiones JavaScript (Rhino), y migraciones de base de datos.
- **Frontend público (Angular 17):** Interfaz de compra de entradas, visualización de eventos, formularios de registro.
- **Frontend de administración (Lit + Shoelace + Vite):** Panel de administración para organizadores de eventos.

No se incluyen en el alcance las pruebas de infraestructura subyacente, las pruebas de rendimiento ni las integraciones con medios externos en producción.

## 4. Referencias

- **ISO/IEC/IEEE 29119:** estándar internacional para pruebas de software.
- **Documentación oficial:** [https://alf.io](https://alf.io)
- **Repositorio oficial:** [https://github.com/alfio-event/alf.io](https://github.com/alfio-event/alf.io)

## 5. Entorno de pruebas

### 5.1 Configuración del entorno

Las pruebas se ejecutan completamente de manera remota, haciendo uso de una imagen en Github Container Registry que se utilizó para desplegar una misma aplicación para todo el equipo en un cluster de kubernetes en la nube. Esto garantiza que el entorno de pruebas sea idéntico para todos los desarrolladores y que las pruebas sean reproducibles.

Este proceso es controlado mediante Github actions, que:
- **En cada pull request:** Ejecuta el pipeline de pruebas unitarias, generando reportes de cobertura para el contribuidor.
- **En cada push a main:** Ejecuta el pipeline de pruebas unitarias, compila el proyecto y publica la imagen de pruebas en Github Container Registry.

## 6. Resultados de Pruebas Funcionales

### Edición de Tickets Adquiridos (Nombre, Apellido y Correo)

**CPF-01-001**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-001 | Verificar que el campo nombre no admita cadenas vacías. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Rechazar: Error de campo obligatorio |  | El sistema no permite guardar y marca el campo. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-field-len-0](images/functional-tests/run/ticket-edit-field-len-0.png) Se muestra que el sistema valida el campo obligatorio. |  |  |  |  |

**CPF-01-002**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-002 | Verificar que el campo nombre admita 1 carácter. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Aceptar: Cambio guardado exitosamente |  | Cambio guardado exitosamente |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-field-len-1](images/functional-tests/run/ticket-edit-field-len-1.png) Se muestra la edición exitosa con un solo carácter. |  |  |  |  |

**CPF-01-003**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-003 | Verificar que el campo nombre admita 254 caracteres. | Manual | Fallido | 500 Unexpected Exception |
| Resultado esperado |  | Resultado obtenido |  |  |
| Aceptar: Cambio guardado exitosamente |  | Error 500 del servidor al intentar procesar la solicitud. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-field-len-254](images/functional-tests/run/ticket-edit-field-len-254.png) Se observa una excepción inesperada al usar cadenas largas. |  |  |  |  |

**CPF-01-004**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-004 | Verificar que el campo nombre admita 255 caracteres. | Manual | Fallido | 500 Unexpected Exception |
| Resultado esperado |  | Resultado obtenido |  |  |
| Aceptar: Cambio guardado exitosamente |  | Error 500 del servidor al intentar procesar la solicitud. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-field-len-255](images/functional-tests/run/ticket-edit-field-len-255.png) El sistema falla con error 500 en el límite superior de la base de datos. |  |  |  |  |

**CPF-01-005**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-005 | Verificar que el campo nombre rechace 256 caracteres. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Rechazar: Error de longitud excedida |  | El sistema bloquea la entrada o rechaza por validación de frontend. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-field-len-256](images/functional-tests/run/ticket-edit-field-len-256.png) El sistema rechaza correctamente la longitud excedida. |  |  |  |  |

**CPF-01-006**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-006 | Verificar que el campo nombre no admita números. | Manual | Fallido | El sistema acepta caracteres numéricos. |
| Resultado esperado |  | Resultado obtenido |  |  |
| Rechazar: Error de formato (solo letras) |  | El sistema acepta y guarda el nombre con números. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-field-numbers](images/functional-tests/run/ticket-edit-field-numbers.png) Se observa que el sistema no valida el tipo de dato alfanumérico. |  |  |  |  |

**CPF-01-007**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-007 | Verificar que el campo correo no admita cadenas vacías. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Rechazar: Error de campo obligatorio |  | El sistema impide el guardado sin correo. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-email-len-0](images/functional-tests/run/ticket-edit-email-len-0.png) Validación de correo obligatorio exitosa. |  |  |  |  |

**CPF-01-008**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-008 | Verificar que el campo correo rechace formatos inválidos (sin @). | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Rechazar: Error de formato de correo |  | Rechazo por formato inválido. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-email-invalid](images/functional-tests/run/ticket-edit-email-invalid.png) El sistema detecta correctamente el formato de correo inválido. |  |  |  |  |

**CPF-01-009**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-009 | Verificar correo con longitud de 63 caracteres. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Aceptar: Cambio guardado exitosamente |  | Cambio guardado exitosamente. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-email-len-63](images/functional-tests/run/ticket-edit-email-len-63.png) Prueba positiva de longitud de correo exitosa. |  |  |  |  |

**CPF-01-010**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-010 | Verificar correo con longitud de 64 caracteres. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Aceptar: Cambio guardado exitosamente |  | Cambio guardado exitosamente. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-email-len-64](images/functional-tests/run/ticket-edit-email-len-64.png) Prueba positiva en el límite de 64 caracteres exitosa. |  |  |  |  |

**CPF-01-011**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-01-011 | Verificar que el campo correo rechace 65 caracteres. | Manual | Fallido | El sistema acepta correos de más de 64 caracteres. |
| Resultado esperado |  | Resultado obtenido |  |  |
| Rechazar: Error de longitud excedida |  | El sistema permite guardar el correo de 65 caracteres. |  |  |
| Evidencia |  |  |  |  |
| ![ticket-edit-email-len-65](images/functional-tests/run/ticket-edit-email-len-65.png) Se muestra que el sistema no aplica la restricción de longitud en el correo. |  |  |  |  |

### Búsqueda de Reservas (Panel de Administración)

**CPF-02-001**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-02-001 | Búsqueda por ID de reserva existente. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El sistema muestra la reserva específica |  | Se muestra la reserva correspondiente al ID. |  |  |
| Evidencia |  |  |  |  |
| ![reserve-search-id](images/functional-tests/run/reserve-search-id.png) Búsqueda exitosa por identificador único. |  |  |  |  |

**CPF-02-002**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-02-002 | Búsqueda por apellido de asistente. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El sistema lista todas las reservas bajo ese apellido |  | Resultados filtrados correctamente por apellido. |  |  |
| Evidencia |  |  |  |  |
| ![reserve-search-lastname](images/functional-tests/run/reserve-search-lastname.png) Búsqueda por criterio de texto exitosa. |  |  |  |  |

**CPF-02-003**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-02-003 | Búsqueda de valor inexistente. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El sistema muestra mensaje "Sin resultados" |  | Mensaje de "No results found" mostrado correctamente. |  |  |
| Evidencia |  |  |  |  |
| ![reserve-search-none](images/functional-tests/run/reserve-search-none.png) Comportamiento esperado ante búsqueda sin coincidencias. |  |  |  |  |

### Gestión de Estados de Reserva y Pagos

**CPF-03-001**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-03-001 | Transición de PENDING a COMPLETE tras aceptar pago. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| La reserva cambia a estado COMPLETE |  | Cambio de estado reflejado correctamente en la tabla. |  |  |
| Evidencia |  |  |  |  |
| ![approve-1](images/functional-tests/run/reservation-approve-payment-1.png) ![approve-2](images/functional-tests/run/reservation-approve-payment-2.png) ![approve-3](images/functional-tests/run/reservation-approve-payment-3.png) Flujo completo de aprobación de pago. |  |  |  |  |

**CPF-03-002**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-03-002 | Transición de PENDING a CANCELLED tras cancelar pago. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| La reserva cambia a estado CANCELLED |  | Cambio de estado reflejado correctamente en la tabla. |  |  |
| Evidencia |  |  |  |  |
| ![cancel-1](images/functional-tests/run/reservation-cancel-1.png) ![cancel-2](images/functional-tests/run/reservation-cancel-2.png) ![cancel-3](images/functional-tests/run/reservation-cancel-3.png) Flujo completo de cancelación de reserva. |  |  |  |  |

**CPF-03-003**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-03-003 | Clic fuera del modal para regresar al inicio. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El modal se cierra y el estado se mantiene |  | El modal se cierra y el estado se mantiene. |  |  |
| Evidencia |  |  |  |  |
| ![modal-open](images/functional-tests/run/reservation-approve-payment-2.png) ![state-result](images/functional-tests/run/reservation-approve-payment-1.png) Al cerrar el modal, se regresa al estado inicial de la lista. |  |  |  |  |

**CPF-03-004**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-03-004 | Botón "Marcar como completa" visible (Llenado=SI, Pago=Offline, Aprobado=SI). | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| Se visualiza el botón para marcar como completa |  | Botón visible en la interfaz de administración. |  |  |
| Evidencia |  |  |  |  |
| ![mark-complete-show](images/functional-tests/run/reservation-mark-as-complete-should-show.png) El botón aparece según las reglas de la tabla de decisión. |  |  |  |  |

**CPF-03-005**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-03-005 | Botón oculto (Llenado=SI, Pago=Presencial, Aprobado=NO). | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de marcar como completa se oculta |  | Botón no presente en la UI. |  |  |
| Evidencia |  |  |  |  |
| ![mark-complete-hide](images/functional-tests/run/reservation-mark-as-complete-shouldnt-show.png) El botón se oculta correctamente para pagos presenciales no aprobados. |  |  |  |  |

**CPF-03-006**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-03-006 | Botón oculto cuando no se completó el llenado. | Manual | Fallido | El botón se muestra aunque el llenado sea incompleto. |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de marcar como completa se oculta |  | El botón es visible. Al hacer clic, se realiza la petición, falla sin error visible y recarga la página. |  |  |
| Evidencia |  |  |  |  |
| ![mark-complete-bug](images/functional-tests/run/reservation-mark-as-complete-should-show.png) Se observa la presencia del botón a pesar de no cumplir con las condiciones de llenado. |  |  |  |  |

### Disponibilidad de Descarga de Tickets

**CPF-04-001**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-04-001 | No mostrar botón de descarga si el evento ya pasó. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de descarga no se muestra |  | Botón oculto para eventos pasados. |  |  |
| Evidencia |  |  |  |  |
| ![download-not-past](images/functional-tests/run/ticket-download-not-available.png) El sistema restringe la descarga post-evento. |  |  |  |  |

**CPF-04-002**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-04-002 | Descarga disponible para evento presencial con pago aprobado. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de descarga es visible y funcional |  | Botón visible y permite la descarga. |  |  |
| Evidencia |  |  |  |  |
| ![download-available](images/functional-tests/run/ticket-download-available.png) Escenario de éxito para descarga de ticket presencial. |  |  |  |  |

**CPF-04-003**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-04-003 | No mostrar botón si el pago está pendiente (Presencial). | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de descarga permanece oculto |  | Botón oculto por falta de pago. |  |  |
| Evidencia |  |  |  |  |
| ![download-not-unpaid](images/functional-tests/run/ticket-download-not-available.png) Restricción de descarga por estado de pago. |  |  |  |  |

**CPF-04-004**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-04-004 | Descarga disponible para evento híbrido con pago aprobado. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de descarga es visible y funcional |  | Botón visible y permite la descarga. |  |  |
| Evidencia |  |  |  |  |
| ![download-available-hybrid](images/functional-tests/run/ticket-download-available.png) Acceso a ticket en modalidad híbrida exitoso. |  |  |  |  |

**CPF-04-005**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-04-005 | No mostrar botón si el pago está pendiente (Híbrido). | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de descarga permanece oculto |  | Botón oculto por falta de pago. |  |  |
| Evidencia |  |  |  |  |
| ![download-not-hybrid-unpaid](images/functional-tests/run/ticket-download-not-available.png) Bloqueo de descarga en modalidad híbrida sin pago. |  |  |  |  |

**CPF-04-006**
| ID | Descripción | Tipo | Estado | Defectos |
| :--- | :--- | :--- | :--- | :--- |
| CPF-04-006 | No mostrar botón de descarga para modalidad Virtual. | Manual | Exitoso | No se encontraron defectos |
| Resultado esperado |  | Resultado obtenido |  |  |
| El botón de descarga no se muestra (acceso digital) |  | Botón oculto según lógica de negocio para eventos virtuales. |  |  |
| Evidencia |  |  |  |  |
| ![download-not-virtual](images/functional-tests/run/ticket-download-not-available.png) Verificación de lógica de negocio para accesos virtuales. |  |  |  |  |

## 7. Limitaciones

- No se realizan pruebas de carga ni de rendimiento bajo estrés.
- Las pruebas no cubren la validación de la infraestructura en la nube ni del entorno de producción.

## 8. Estrategia y métodos de prueba aplicados

### 8.1 Técnicas de diseño de pruebas

- **Partición por equivalencia:** Los datos de entrada se agrupan en clases válidas e inválidas. Por ejemplo, los roles de usuario (administrador, organizador, operador de check-in) se prueban para verificar que cada uno tenga acceso exclusivo a las operaciones permitidas.
- **Análisis de valores límite:** Se prueban valores en los extremos de los rangos permitidos, como límites de caracteres en campos de texto, fechas próximas al evento, cupos mínimos y máximos de entradas, y montos de dinero en los límites de precisión de BigDecimal.
- **Pruebas de casos de uso:** Se recorren paso a paso los flujos principales del sistema: creación de un evento, configuración de categorías de entradas, proceso de compra, generación de entradas (incluyendo Apple Wallet), check-in y reporting.
- **Tablas de decisión:** Se aplican para validar reglas complejas de negocio, como el cálculo de precios con descuentos, impuestos (IVA), tarifas de servicio y promociones combinadas, donde múltiples condiciones booleanas determinan el resultado final.

## 9. Conclusión

El enfoque de pruebas de alf.io combina múltiples niveles con un fuerte énfasis en la automatización para garantizar entornos de prueba reproducibles y fiables. La cobertura es extensa en la capa de negocio y los flujos críticos de pago y reserva, con 141 archivos de prueba que abarcan desde validaciones unitarias hasta escenarios concurrentes complejos. El pipeline de CI verifica cada cambio contra tres versiones de PostgreSQL, asegurando compatibilidad y calidad continua.
