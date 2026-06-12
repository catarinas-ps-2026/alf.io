# Diseño de Casos de Prueba Funcionales - Reservaciones (Frontend Público)

## Índice
- [1. Introducción](#1-introducción)
- [2. Alcance de las Pruebas](#2-alcance-de-las-pruebas)
- [3. Referencias](#3-referencias)
- [4. Técnicas de Prueba Aplicadas](#4-técnicas-de-prueba-aplicadas)
- [5. Diseño de Casos de Prueba](#5-diseño-de-casos-de-prueba)
- [6. Matriz de Trazabilidad](#6-matriz-de-trazabilidad)
- [7. Métodos y Herramientas](#7-métodos-y-herramientas)

## 1. Introducción

Este documento describe el diseño de casos de prueba funcionales para el módulo de **reservaciones** del frontend público de alf.io. El flujo de reservación incluye: selección de eventos, elección de cantidad de tickets, llenado de formularios de asistente, aceptación de términos, countdown de expiración, confirmación de reserva y descarga de entradas.

## 2. Alcance de las Pruebas

### 2.1 Funcionalidades en Alcance

- **Selección de entradas (tickets):** Dropdown de cantidad con rangos 0-5 y 0-10, validación de que no se permita avanzar con 0 entradas.
- **Formulario de asistente:** Campos obligatorios (nombre, apellido, email, país opcional), límite de 255 caracteres, campos personalizados, manejo de múltiples asistentes.
- **Tiempo de expiración (countdown):** Contador de 24 minutos con transiciones de color (azul >5min, amarillo 1-5min, rojo <1min), modal de expiración a 0.
- **Aceptación de términos y condiciones:** Checkbox obligatorios (Condizioni di vendita, Privacy Policy, Informativa sulla privacy) para habilitar el botón de pago.
- **Reserva completada:** Barra de carga, página de confirmación, descarga de PDF con códigos QR, envío de email de confirmación.
- **Panel de administración:** Visualización de reserva, opción de imprimir recibo.
- **Escasez de entradas:** Mensajes de error por insuficiencia y sold out.

### 2.2 Funcionalidades Fuera de Alcance

- Integraciones reales con pasarelas de pago (solo modo test).
- Pruebas de estrés y rendimiento.
- Generación masiva de facturas PDF.

## 3. Referencias

1. **ISO/IEC/IEEE 29119:** Estándar internacional para pruebas de software.
2. **Informe de Pruebas Funcionales:** [[Informe-de-Casos-de-Prueba-Funcionales-Reservaciones]]
3. **Repositorio oficial:** [https://github.com/alfio-event/alf.io](https://github.com/alfio-event/alf.io)

## 4. Técnicas de Prueba Aplicadas

| Técnica | Descripción | Aplicación en Reservaciones |
|---------|-------------|------------------------------|
| **Partición por Equivalencia (PE)** | División del dominio de entrada en clases válidas e inválidas | Cantidad de tickets, campos de formulario, checkboxes |
| **Análisis de Valores Límite (AVL)** | Identificación de valores en los bordes de las particiones | 0 tickets, 255 caracteres, umbrales de tiempo (5min, 1min) |
| **Transición de Estados (TE)** | Modelado de cambios de estado del sistema | Countdown: azul → amarillo → rojo → expiración |
| **Tablas de Decisión (TD)** | Modelado de lógica condicional compleja | Visibilidad del botón de pago según aceptación de términos |
| **Pruebas de Casos de Uso** | Recorrido paso a paso de flujos principales | Flujo completo: selección → formulario → términos → pago → confirmación |

---

## 5. Diseño de Casos de Prueba

### 5.1 Selección de Entradas (Tickets)

#### CPF-RES-01-001: Avanzar con 0 entradas

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-01-001 |
| **Funcionalidad** | Validación de cantidad mínima de entradas |
| **Descripción** | Verificar que el sistema no permita avanzar cuando se seleccionan 0 entradas |
| **Precondiciones** | Evento público visible con al menos una categoría de tickets |
| **Prioridad** | Alta |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Cantidad de tickets | 1-5 o 1-10 (según categoría) | 0 entradas |

##### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición | Justificación | Resultado Esperado |
|----|-------|--------------|-----------|---------------|---------------------|
| VL1 | Cantidad | 0 | PE1 (no válido) | Mínimo absoluto | Rechazar: mostrar error |
| VL2 | Cantidad | 1 | PE1 (válido) | Primer valor válido | Permitir avanzar |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-01-001a | Cantidad: 0 | Error: "seleccione al menos una entrada", no avanza | PE1, VL1 |
| CP-RES-01-001b | Cantidad: 1 | Avanza al siguiente paso | PE1, VL2 |

---

#### CPF-RES-01-002/003: Selección con dropdown (rango 0-5 y 0-10)

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-01-002 |
| **Funcionalidad** | Selección de entradas con dropdown |
| **Descripción** | Verificar que el dropdown muestra valores predefinidos 0-5 o 0-10 sin permitir valores negativos ni superiores |
| **Precondiciones** | Evento público con categoría que use dropdown 0-5 o 0-10 |
| **Prioridad** | Alta |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Rango 0-5 | 0, 1, 2, 3, 4, 5 | Negativos, >5 |
| PE2 | Rango 0-10 | 0, 1, 2, ..., 10 | Negativos, >10 |

##### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición | Justificación | Resultado Esperado |
|----|-------|--------------|-----------|---------------|---------------------|
| VL1 | Cantidad | 0 | PE1/PE2 | Extremo inferior (no avanzar) | Dropdown muestra 0 |
| VL2 | Cantidad | 5 | PE1 | Extremo superior rango 0-5 | Dropdown muestra 5 |
| VL3 | Cantidad | 10 | PE2 | Extremo superior rango 0-10 | Dropdown muestra 10 |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-01-002a | Dropdown rango 0-5, seleccionar 0 | Dropdown muestra valores 0-5 | PE1, VL1 |
| CP-RES-01-002b | Dropdown rango 0-5, seleccionar 5 | Dropdown acepta 5 como máximo | PE1, VL2 |
| CP-RES-01-003a | Dropdown rango 0-10, seleccionar 0 | Dropdown muestra valores 0-10 | PE2, VL1 |
| CP-RES-01-003b | Dropdown rango 0-10, seleccionar 10 | Dropdown acepta 10 como máximo | PE2, VL3 |

---

### 5.2 Formulario de Asistente - Validación de Campos

#### CPF-RES-02-001: Campos obligatorios vacíos

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-02-001 |
| **Funcionalidad** | Validación de campos obligatorios |
| **Descripción** | Verificar que los campos nombre, apellido y email muestren error al estar vacíos |
| **Precondiciones** | Flujo de reserva en paso de formulario de asistente |
| **Prioridad** | Alta |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Nombre | Texto no vacío (1-255 chars) | Vacío |
| PE2 | Apellido | Texto no vacío (1-255 chars) | Vacío |
| PE3 | Email | Formato válido (user@domain.com) | Vacío |

##### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición | Justificación | Resultado Esperado |
|----|-------|--------------|-----------|---------------|---------------------|
| VL1 | Nombre | 0 chars | PE1 (no válido) | Vacío | Error: "Nombre obligatorio" |
| VL2 | Nombre | 1 char | PE1 (válido) | Mínimo válido | Aceptar |
| VL3 | Apellido | 0 chars | PE2 (no válido) | Vacío | Error: "Apellido obligatorio" |
| VL4 | Email | 0 chars | PE3 (no válido) | Vacío | Error: "Email obligatorio" |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-02-001a | Nombre vacío | Error: "Nombre obligatorio" | PE1, VL1 |
| CP-RES-02-001b | Apellido vacío | Error: "Apellido obligatorio" | PE2, VL3 |
| CP-RES-02-001c | Email vacío | Error: "Email obligatorio" | PE3, VL4 |
| CP-RES-02-001d | Todos vacíos | 3 errores mostrados | PE1-PE3, VL1-VL4 |

---

#### CPF-RES-02-002/004: Llenado correcto de datos

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-02-002 |
| **Funcionalidad** | Llenado válido de formulario |
| **Descripción** | Verificar que el sistema acepta datos válidos y permite continuar |
| **Precondiciones** | Flujo de reserva en paso de formulario |
| **Prioridad** | Alta |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-02-002a | Nombre: "Juan", Apellido: "Pérez", Email: "juan@test.com" | Formulario válido, permite continuar | PE1-PE3 |
| CP-RES-02-004a | Comprador: "Juan Pérez", Asistente: "María López" | Nombres diferentes aceptados | PE1-PE3 |

---

#### CPF-RES-02-003: Validación de formato de email

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-02-003 |
| **Funcionalidad** | Validación de formato de email |
| **Descripción** | Verificar que el email debe contener @, texto mínimo y un punto después del @ |
| **Precondiciones** | Flujo de reserva en paso de formulario |
| **Prioridad** | Alta |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Email | Formato estándar (user@domain.com) | Sin @, sin dominio, sin punto |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-02-003a | Email: "test@test.com" | Aceptar formato válido | PE1 |
| CP-RES-02-003b | Email: "test" | Rechazar: formato inválido | PE1 |
| CP-RES-02-003c | Email: "test@" | Rechazar: formato inválido | PE1 |

---

#### CPF-RES-02-005/006: Múltiples asistentes y ocultar campos

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-02-005 |
| **Funcionalidad** | Manejo de múltiples asistentes |
| **Descripción** | Verificar que todos los campos de asistentes adicionales son obligatorios |
| **Precondiciones** | Selección de más de 1 entrada |
| **Prioridad** | Media |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-02-005a | 2 entradas seleccionadas | Formularios para ambos asistentes visibles | PE1 |
| CP-RES-02-006a | Checkbox "ocultar asistentes" marcado | Campos de asistentes ocultos, permite continuar | PE1 |

---

#### CPF-RES-02-007/008: Límite de 255 caracteres

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-02-007 |
| **Funcionalidad** | Límite de caracteres en campos de texto |
| **Descripción** | Verificar que el sistema rechaza campos con más de 255 caracteres |
| **Precondiciones** | Flujo de reserva en paso de formulario |
| **Prioridad** | Alta |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Campos de texto | 1-255 caracteres | 0 chars, >255 chars |

##### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición | Justificación | Resultado Esperado |
|----|-------|--------------|-----------|---------------|---------------------|
| VL1 | Longitud | 0 chars | PE1 (no válido) | Vacío | Error de validación |
| VL2 | Longitud | 1 char | PE1 (válido) | Mínimo | Aceptar |
| VL3 | Longitud | 100 chars | PE1 (válido) | Intermedio | Aceptar |
| VL4 | Longitud | 255 chars | PE1 (válido) | Máximo permitido | Aceptar |
| VL5 | Longitud | 256 chars | PE1 (no válido) | Máximo + 1 | Rechazar: longitud excedida |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-02-007a | Campo con 256 caracteres | Error de validación | PE1, VL5 |
| CP-RES-02-008a | Campo con 100 caracteres | Aceptar y guardar | PE1, VL3 |
| CP-RES-02-008b | Campo con 255 caracteres | Aceptar y guardar | PE1, VL4 |

---

### 5.3 Tiempo de Expiración de Reserva (Countdown)

#### CPF-RES-03-001/002/003: Contador color azul (>5 minutos)

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-03-001 |
| **Funcionalidad** | Visualización del contador con color azul |
| **Descripción** | Verificar que el contador muestra color azul cuando el tiempo restante es mayor a 5 minutos |
| **Precondiciones** | Reserva activa con countdown en curso |
| **Prioridad** | Alta |

##### Transición de Estados (Countdown)

```
S0 (Inicio: 24min) --[tiempo > 5min]--> S1 (Azul)
S1 (Azul) --[tiempo ≤ 5min]--> S2 (Amarillo)
S2 (Amarillo) --[tiempo ≤ 1min]--> S3 (Rojo)
S3 (Rojo) --[tiempo = 0]--> S4 (Expirado: modal)
```

##### Análisis de Valores Límite

| ID | Campo | Valor Límite | Estado | Justificación | Resultado Esperado |
|----|-------|--------------|--------|---------------|---------------------|
| VL1 | Tiempo | 24:00 | S0→S1 | Inicio | Azul |
| VL2 | Tiempo | 15:00 | S1 | Intermedio | Azul |
| VL3 | Tiempo | 10:00 | S1 | Cerca del umbral | Azul |
| VL4 | Tiempo | 5:01 | S1 | Justo arriba del umbral | Azul |
| VL5 | Tiempo | 5:00 | S1→S2 | Umbral amarillo | Amarillo |
| VL6 | Tiempo | 1:01 | S2 | Justo arriba del umbral rojo | Amarillo |
| VL7 | Tiempo | 1:00 | S2→S3 | Umbral rojo | Rojo |
| VL8 | Tiempo | 0:00 | S3→S4 | Expiración | Modal expiración |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-03-001 | Tiempo: 24:00 | Contador azul | TE, VL1 |
| CP-RES-03-002 | Tiempo: 15:00 | Contador azul | TE, VL2 |
| CP-RES-03-003 | Tiempo: 10:52 | Contador azul | TE, VL3 |
| CP-RES-03-004 | Tiempo: ≤5:00 | Contador amarillo | TE, VL5 |
| CP-RES-03-005 | Tiempo: ≤1:00 | Contador rojo | TE, VL7 |
| CP-RES-03-006 | Tiempo: 0:00 | Modal "La sesión ha expirado" con opción volver al inicio | TE, VL8 |

---

### 5.4 Aceptación de Términos y Condiciones

#### CPF-RES-04-001/002/003: Habilitación del botón de pago

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-04-001 |
| **Funcionalidad** | Control de habilitación del botón de pago |
| **Descripción** | Verificar que el botón "Paga ahora" solo se habilita al aceptar los 3 checkboxes |
| **Precondiciones** | Flujo de reserva en paso de términos y condiciones |
| **Prioridad** | Alta |

##### Tabla de Decisión

| Condición | C1 | C2 | C3 | C4 |
|-----------|----|----|----|----|
| ¿Condizioni di vendita aceptada? | NO | SI | SI | SI |
| ¿Privacy Policy aceptada? | - | NO | SI | SI |
| ¿Informativa sulla privacy aceptada? | - | - | NO | SI |
| **Habilitar botón** | **NO** | **NO** | **NO** | **SI** |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Aceptación de términos | 3 checkboxes marcados | 0, 1 o 2 checkboxes marcados |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-04-001 | 0 checkboxes marcados | Botón deshabilitado | TD, PE1 |
| CP-RES-04-002 | 3 checkboxes marcados | Botón habilitado | TD, PE1 |
| CP-RES-04-003 | 0 checkboxes, evento gratuito | Error: aceptar términos requerido | TD, PE1 |

---

### 5.5 Reserva Completada - Confirmación y Descarga

#### CPF-RES-05-001/002: Procesamiento y confirmación

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-05-001 |
| **Funcionalidad** | Procesamiento de reserva |
| **Descripción** | Verificar que se muestra barra de carga y luego página de confirmación |
| **Precondiciones** | Pago procesado exitosamente |
| **Prioridad** | Alta |

##### Transición de Estados

```
S0 (Procesando) --[pago OK]--> S1 (Confirmación)
S0 (Procesando) --[error]--> S2 (Error)
```

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-05-001 | Pago procesándose | Barra de carga visible | TE |
| CP-RES-05-002 | Pago completado | Página "La riserva è stata completata" con datos | TE |

---

#### CPF-RES-05-004/005: Descarga de PDF

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-05-004 |
| **Funcionalidad** | Descarga de entradas en PDF |
| **Descripción** | Verificar que el PDF contiene códigos QR y datos de las entradas |
| **Precondiciones** | Reserva completada |
| **Prioridad** | Alta |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-05-004a | Reserva completada | Botón de descarga PDF visible | PE1 |
| CP-RES-05-005a | PDF descargado | Contiene códigos QR y datos completos | PE1 |

---

#### CPF-RES-05-006/007: Envío de email

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-05-006 |
| **Funcionalidad** | Envío de email de confirmación |
| **Descripción** | Verificar que se envía email con las entradas registradas |
| **Precondiciones** | Reserva completada |
| **Prioridad** | Media |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-05-006 | Reserva completada | Email enviado con confirmación | PE1 |
| CP-RES-05-007 | Email recibido | Contiene entradas con códigos QR | PE1 |

---

### 5.6 Panel de Administración - Gestión de Reservas

#### CPF-RES-06-001/002: Visualización e impresión

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-06-001 |
| **Funcionalidad** | Gestión de reservas en admin |
| **Descripción** | Verificar que la reserva aparece en el panel y se puede imprimir recibo |
| **Precondiciones** | Reserva completada, acceso admin al evento |
| **Prioridad** | Media |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-06-001 | Acceder al manager del evento | Reserva visible en el listado | PE1 |
| CP-RES-06-002 | Seleccionar opción imprimir | Boleta disponible para impresión | PE1 |

---

### 5.7 Escasez de Entradas y Campos Personalizados

#### CPF-RES-07-001/002: Mensajes de insuficiencia y sold out

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-07-001 |
| **Funcionalidad** | Control de disponibilidad |
| **Descripción** | Verificar mensajes de error cuando no hay suficientes entradas o están agotadas |
| **Precondiciones** | Evento con disponibilidad limitada o agotada |
| **Prioridad** | Alta |

##### Partición de Equivalencia

| ID | Campo | Clase Válida | Clases No Válidas |
|----|-------|--------------|-------------------|
| PE1 | Disponibilidad | Tickets > 0 | Tickets = 0 (sold out) |
| PE2 | Solicitud | Cantidad ≤ disponibles | Cantidad > disponibles |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-07-001 | Cantidad solicitada > disponibles | Error: "no hay suficientes entradas", bloquea selección | PE2 |
| CP-RES-07-002 | Disponibilidad = 0 | Mensaje: entradas agotadas (sold out) | PE1 |

---

#### CPF-RES-07-003: Campos personalizados

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-RES-07-003 |
| **Funcionalidad** | Campos personalizados en formulario |
| **Descripción** | Verificar que los campos adicionales aparecen según configuración regional |
| **Precondiciones** | Evento con campos personalizados configurados (ej: Perú) |
| **Prioridad** | Media |

##### Catálogo de Pruebas

| #CP | Datos de Entrada | Resultado Esperado | Técnica |
|-----|------------------|--------------------|---------| 
| CP-RES-07-003 | Evento con campos regionales | Campos personalizados visibles en formulario | PE1 |

---

## 6. Matriz de Trazabilidad

| Requisito Funcional | Casos de Prueba Asociados |
| :--- | :--- |
| **RF-RES-01:** Selección de entradas (dropdown) | CPF-RES-01-001 (001a-001b), CPF-RES-01-002 (002a-002b), CPF-RES-01-003 (003a-003b) |
| **RF-RES-02:** Formulario de asistente | CPF-RES-02-001 (001a-001d), CPF-RES-02-002, CPF-RES-02-003 (003a-003c), CPF-RES-02-004, CPF-RES-02-005, CPF-RES-02-006, CPF-RES-02-007, CPF-RES-02-008 |
| **RF-RES-03:** Tiempo de expiración (countdown) | CPF-RES-03-001, CPF-RES-03-002, CPF-RES-03-003, CPF-RES-03-004, CPF-RES-03-005, CPF-RES-03-006 |
| **RF-RES-04:** Aceptación de términos | CPF-RES-04-001, CPF-RES-04-002, CPF-RES-04-003 |
| **RF-RES-05:** Reserva completada | CPF-RES-05-001, CPF-RES-05-002, CPF-RES-05-003, CPF-RES-05-004, CPF-RES-05-005, CPF-RES-05-006, CPF-RES-05-007 |
| **RF-RES-06:** Panel de administración | CPF-RES-06-001, CPF-RES-06-002 |
| **RF-RES-07:** Escasez y campos personalizados | CPF-RES-07-001, CPF-RES-07-002, CPF-RES-07-003 |

## 7. Métodos y Herramientas

### 7.1 Gestión y Planificación

- **Gestión de casos de prueba:** GitHub Issues con templates estandarizados.
- **Documentación:** GitHub Wiki del repositorio.

### 7.2 Ejecución

- **Ejecución manual:** Pruebas funcionales realizadas en entorno de pruebas desplegado.
- **Evidencia:** Capturas de pantalla almacenadas en `wiki/images/functional-tests/run/reservation/`.

### 7.3 Automatización

- **CI/CD:** GitHub Actions para ejecución automática de pruebas unitarias en cada Pull Request.
- **Reportes de cobertura:** Vitest coverage integrado en el pipeline.
