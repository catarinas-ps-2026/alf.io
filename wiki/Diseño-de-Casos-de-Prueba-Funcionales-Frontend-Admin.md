# Diseño de Casos de Prueba Funcionales - Frontend Admin

## 1. Introducción

### 1.1 Alcance

Este documento describe el diseño de casos de prueba funcionales para la aplicación **Admin Frontend** del proyecto alf.io. La aplicación está construida con **Lit 3** (Web Components) y **TypeScript**, utilizando **Vite** como bundler y **Vitest** como framework de pruebas.

El alcance incluye:
- Funciones puras de utilidad (`service/helpers.ts`)
- Funciones HTTP para comunicación con el servidor
- Servicios de negocio (campos adicionales, items adicionales, pagos, eventos, etc.)
- Modelos y funciones auxiliares
- Utilidades de testing (factories y mocks)

### 1.2 Técnicas de Prueba Aplicadas

| Técnica | Descripción | Aplicación |
|---------|-------------|------------|
| **Particiones de Equivalencia (PE)** | División del dominio de entrada en clases donde se espera el mismo comportamiento | Todas las funciones con parámetros |
| **Análisis de Valores Límite (AVL)** | Identificación de valores en los bordes de las particiones | Funciones con rangos numéricos, strings, fechas |
| **Tablas de Decisión (TD)** | Modelado de lógica condicional compleja | Funciones con múltiples condiciones (if/else) |
| **Transición de Estados (TE)** | Modelado de cambios de estado del sistema | Servicios con estados (carga, éxito, error) |

---

## 2. Utilidades de Testing

### 2.1 Factories de datos de prueba

**Propósito:** Proporcionar funciones factory para crear objetos de prueba con valores por defecto, permitiendo personalización mediante overrides.

**Archivo:** `src/test-utils/factories.ts`

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-UTL-01 |
| **Funcionalidad** | Generación de datos de prueba |
| **Descripción** | Verificar que las funciones factory crean objetos con la estructura correcta y valores por defecto válidos, y que aceptan overrides para personalización |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

#### Catálogo de Pruebas

| ID | Factory | Descripción | Datos de Entrada | Resultado Esperado | Técnica |
|----|---------|-------------|------------------|-------------------|---------|
| CP-UTL-01 | `createContentLanguage()` | Crea idioma con valores por defecto | Sin parámetros | Objeto con `locale: 'en'`, `value: 1`, `language: 'English'` | PE1 |
| CP-UTL-02 | `createContentLanguage()` | Crea idioma con override | `{ locale: 'es' }` | Objeto con `locale: 'es'`, `value: 1` | PE1 |
| CP-UTL-03 | `createPurchaseContext()` | Crea contexto de compra válido | Sin parámetros | Objeto con `type: 'event'`, `publicIdentifier: 'test-event-123'` | PE2 |
| CP-UTL-04 | `createPurchaseContext()` | Crea contexto tipo suscripción | `{ type: 'subscription' }` | Objeto con `type: 'subscription'` | PE2 |
| CP-UTL-05 | `createDateTimeModification()` | Crea fecha/hora válida | Sin parámetros | Objeto con `date: '2025-01-15'`, `time: '10:00'` | PE3 |
| CP-UTL-06 | `createAdditionalField()` | Crea campo adicional completo | Sin parámetros | Objeto con `name: 'attendee-name'`, `type: 'input:text'`, `required: true` | PE4 |
| CP-UTL-07 | `createAdditionalField()` | Crea campo con tipo checkbox | `{ type: 'checkbox' }` | Objeto con `type: 'checkbox'` | PE4 |
| CP-UTL-08 | `createAdditionalItem()` | Crea item adicional válido | Sin parámetros | Objeto con `id: 1`, `price: 1000`, `type: 'SUPPLEMENT'` | PE5 |
| CP-UTL-09 | `createAdditionalItem()` | Crea item tipo donación | `{ type: 'DONATION' }` | Objeto con `type: 'DONATION'` | PE5 |
| CP-UTL-10 | `createAlfioEvent()` | Crea evento completo | Sin parámetros | Objeto con `id: 1`, `displayName: 'Test Event'`, `currency: 'EUR'` | PE6 |
| CP-UTL-11 | `createOrganization()` | Crea organización válida | Sin parámetros | Objeto con `id: 1`, `name: 'Test Org'` | PE7 |
| CP-UTL-12 | `createEventWithOrganization()` | Crea evento con organización | Sin parámetros | Objeto con `event` y `organization` definidos | PE8 |
| CP-UTL-13 | `createValidatedResponse()` | Crea respuesta validada exitosa | `{ name: 'test' }` | Objeto con `success: true`, `value: { name: 'test' }` | PE9 |
| CP-UTL-14 | `createValidatedResponse()` | Crea respuesta con errores | `{ name: 'test' }, { success: false, errorCount: 1 }` | Objeto con `success: false`, `errorCount: 1` | PE9 |

---

### 2.2 Helpers de mocking

**Propósito:** Facilitar el mockeo de `fetch` global y meta tags CSRF para tests de servicios HTTP.

**Archivo:** `src/test-utils/mocks.ts`

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-UTL-02 |
| **Funcionalidad** | Mocking de dependencias HTTP |
| **Descripción** | Verificar que los helpers de mocking mockean correctamente `global.fetch`, inyectan meta tags CSRF y permiten verificar llamadas |
| **Precondiciones** | Entorno de pruebas Vitest con jsdom |
| **Prioridad** | Alta |

#### Técnicas de Prueba Aplicadas

##### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Comportamiento Esperado |
|----|-------|-----------|-----------|------------------------|
| PE1 | `mockFetchJson()` | Respuesta exitosa | `data` proporcionado | `fetch` retorna `{ ok: true, json: () => data }` |
| PE2 | `mockFetchJsonOnce()` | Respuesta única | `data` proporcionado | `fetch` retorna dato una sola vez |
| PE3 | `mockFetchResponse()` | Respuesta personalizada | `options` con `ok`, `json`, `status` | `fetch` retorna respuesta personalizada |
| PE4 | `mockCsrfMeta()` | Meta tags CSRF | Sin parámetros | Inyecta `_csrf_header` y `_csrf` en `<head>` |
| PE5 | `clearCsrfMeta()` | Limpieza | Sin parámetros | Elimina meta tags CSRF del DOM |
| PE6 | `getFetchMock()` | Obtener mock | Sin parámetros | Retorna referencia a `global.fetch` mockeado |
| PE7 | `resetFetchMock()` | Restaurar | Sin parámetros | Restaura `fetch` original y limpia globals |

##### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Resultado Esperado |
|----|-------|--------------|-------------------|---------------|-------------------|
| VL1 | `mockFetchJson()` | `data = {}` | PE1 | Objeto vacío | `fetch` retorna `{}` |
| VL2 | `mockFetchJson()` | `data = null` | PE1 | Null | `fetch` retorna `null` |
| VL3 | `mockCsrfMeta()` | Sin meta tags previas | PE4 | Primera llamada | Meta tags creadas |
| VL4 | `mockCsrfMeta()` | Con meta tags previas | PE4 | Llamada duplicada | Meta tags sobrescritas |
| VL5 | `mockFetchResponse()` | `ok = false` | PE3 | Respuesta de error | `fetch` retorna `{ ok: false }` |

#### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-MCK-01 | mockFetchJson mockea fetch con datos | `data = { id: 1 }` | `fetch` retorna `{ ok: true, json: { id: 1 } }` | 1. Ejecutar `mockFetchJson({ id: 1 })`<br>2. Llamar `await fetch('/api')`<br>3. Verificar `json()` retorna `{ id: 1 }` | PE1, VL1 |
| CP-MCK-02 | mockFetchJsonOnce permite múltiples mockeos | `data1 = { a: 1 }`, `data2 = { b: 2 }` | Primera llamada: `{ a: 1 }`, Segunda: `{ b: 2 }` | 1. Ejecutar `mockFetchJsonOnce({ a: 1 })`<br>2. Ejecutar `mockFetchJsonOnce({ b: 2 })`<br>3. Verificar primera y segunda llamada | PE2 |
| CP-MCK-03 | mockFetchResponse crea respuesta con error | `{ ok: false, status: 404 }` | `fetch` retorna `{ ok: false, status: 404 }` | 1. Ejecutar `mockFetchResponse({ ok: false, status: 404 })`<br>2. Llamar `fetch`<br>3. Verificar `response.ok === false` | PE3, VL5 |
| CP-MCK-04 | mockCsrfMeta inyecta meta tags | Sin parámetros | `<meta name="_csrf_header">` y `<meta name="_csrf">` en `<head>` | 1. Ejecutar `mockCsrfMeta()`<br>2. Verificar `document.querySelector('meta[name="_csrf_header"]')`<br>3. Verificar `document.querySelector('meta[name="_csrf"]')` | PE4, VL3 |
| CP-MCK-05 | clearCsrfMeta elimina meta tags | Sin parámetros | `<head>` sin meta tags CSRF | 1. Ejecutar `mockCsrfMeta()`<br>2. Ejecutar `clearCsrfMeta()`<br>3. Verificar que no existen meta tags CSRF | PE5 |
| CP-MCK-06 | getFetchMock retorna función mockeada | Sin parámetros | Función mockeada de `fetch` | 1. Ejecutar `mockFetchJson({})`<br>2. Ejecutar `getFetchMock()`<br>3. Verificar que es función mockeada | PE6 |
| CP-MCK-07 | resetFetchMock restaura estado original | Sin parámetros | `fetch` restaurado a valor original | 1. Ejecutar `mockFetchJson({})`<br>2. Ejecutar `resetFetchMock()`<br>3. Verificar que `fetch` ya no está mockeado | PE7 |

---

## 3. Funciones Puras (service/helpers.ts)

### 3.1 Conversión de valores a cadena de texto (asString)

**Función:** `asString(value: any): string | null`
**Ubicación:** `src/service/helpers.ts:109-114`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-01 |
| **Funcionalidad** | Conversión de valores a cadena de texto |
| **Descripción** | Verificar que la función convierte diferentes tipos de datos a string, manteniendo null/undefined sin cambios |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `value` | Null | `value === null` | `null` |
| PE2 | `value` | Undefined | `value === undefined` | `undefined` |
| PE3 | `value` | Number | `typeof value === 'number'` | `String(value)` |
| PE4 | `value` | String | `typeof value === 'string'` | `value` |
| PE5 | `value` | Boolean | `typeof value === 'boolean'` | `String(value)` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `value` | `null` | PE1 | Extremo inferior | `null` |
| VL2 | `value` | `undefined` | PE2 | Extremo inferior | `undefined` |
| VL3 | `value` | `0` | PE3 | Falsy pero `!= null` | `'0'` |
| VL4 | `value` | `''` | PE4 | Falsy pero `!= null` | `''` |
| VL5 | `value` | `false` | PE5 | Falsy pero `!= null` | `'false'` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-STR-01 | Null se mantiene como null | `value = null` | `null` | 1. Importar `asString`<br>2. Ejecutar `asString(null)`<br>3. Verificar retorno `null` | PE1, VL1 |
| CP-STR-02 | Undefined se mantiene como undefined | `value = undefined` | `undefined` | 1. Ejecutar `asString(undefined)`<br>2. Verificar retorno `undefined` | PE2, VL2 |
| CP-STR-03 | Number positivo se convierte a string | `value = 42` | `'42'` | 1. Ejecutar `asString(42)`<br>2. Verificar retorno `'42'` | PE3 |
| CP-STR-04 | Number cero se convierte a string | `value = 0` | `'0'` | 1. Ejecutar `asString(0)`<br>2. Verificar retorno `'0'` | PE3, VL3 |
| CP-STR-05 | Number negativo se convierte a string | `value = -1` | `'-1'` | 1. Ejecutar `asString(-1)`<br>2. Verificar retorno `'-1'` | PE3 |
| CP-STR-06 | String vacío se retorna sin cambios | `value = ''` | `''` | 1. Ejecutar `asString('')`<br>2. Verificar retorno `''` | PE4, VL4 |
| CP-STR-07 | String no vacío se retorna sin cambios | `value = 'hello'` | `'hello'` | 1. Ejecutar `asString('hello')`<br>2. Verificar retorno `'hello'` | PE4 |
| CP-STR-08 | Boolean true se convierte a string | `value = true` | `'true'` | 1. Ejecutar `asString(true)`<br>2. Verificar retorno `'true'` | PE5 |
| CP-STR-09 | Boolean false se convierte a string | `value = false` | `'false'` | 1. Ejecutar `asString(false)`<br>2. Verificar retorno `'false'` | PE5, VL5 |

---

### 3.2 Conversión de cadenas a números (asNumber)

**Función:** `asNumber(value?: string): number | null`
**Ubicación:** `src/service/helpers.ts:116-121`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-02 |
| **Funcionalidad** | Conversión de cadenas a números enteros |
| **Descripción** | Verificar que la función convierte strings numéricos a enteros usando `parseInt` con radix 10, retornando null para inputs nulos y `NaN` para strings no numéricos |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `value` | Null/Undefined | `value == null` | `null` |
| PE2 | `value` | Entero positivo válido | `parseInt(value, 10) > 0 && !isNaN` | Número parseado |
| PE3 | `value` | Entero cero | `parseInt(value, 10) === 0` | `0` |
| PE4 | `value` | Entero negativo válido | `parseInt(value, 10) < 0 && !isNaN` | Número parseado |
| PE5 | `value` | Decimal | `value` contiene `.` | Entero truncado |
| PE6 | `value` | No numérico | `parseInt` retorna `NaN` | `NaN` |
| PE7 | `value` | String vacío | `parseInt('', 10)` | `NaN` |
| PE8 | `value` | Notación científica | `'1e3'` | `1` (trunca en `e`) |
| PE9 | `value` | Hexadecimal | `'0x1A'` | `0` (trunca en `x`) |
| PE10 | `value` | Con espacios | `'  42  '` | `42` (ignora espacios) |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `value` | `undefined` | PE1 | Extremo inferior | `null` |
| VL2 | `value` | `null` | PE1 | Extremo inferior | `null` |
| VL3 | `value` | `'0'` | PE3 | Falsy pero `!= null` | `0` |
| VL4 | `value` | `''` | PE7 | String vacío | `NaN` |
| VL5 | `value` | `'-1'` | PE4 | Primer negativo | `-1` |
| VL6 | `value` | `'999999999'` | PE2 | Número grande | `999999999` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-NUM-01 | Retorna null para undefined | `value = undefined` | `null` | 1. Importar `asNumber`<br>2. Ejecutar `asNumber(undefined)`<br>3. Verificar retorno `null` | PE1, VL1 |
| CP-NUM-02 | Retorna null para null | `value = null` | `null` | 1. Ejecutar `asNumber(null)`<br>2. Verificar retorno `null` | PE1, VL2 |
| CP-NUM-03 | Parsea entero positivo | `value = '42'` | `42` | 1. Ejecutar `asNumber('42')`<br>2. Verificar retorno `42` | PE2 |
| CP-NUM-04 | Parsea entero cero | `value = '0'` | `0` | 1. Ejecutar `asNumber('0')`<br>2. Verificar retorno `0` | PE3, VL3 |
| CP-NUM-05 | Parsea entero negativo | `value = '-5'` | `-5` | 1. Ejecutar `asNumber('-5')`<br>2. Verificar retorno `-5` | PE4 |
| CP-NUM-06 | Trunca decimal | `value = '3.14'` | `3` | 1. Ejecutar `asNumber('3.14')`<br>2. Verificar retorno `3` | PE5 |
| CP-NUM-07 | Retorna NaN para no numérico | `value = 'abc'` | `NaN` | 1. Ejecutar `asNumber('abc')`<br>2. Verificar retorno `NaN` | PE6 |
| CP-NUM-08 | Retorna NaN para vacío | `value = ''` | `NaN` | 1. Ejecutar `asNumber('')`<br>2. Verificar retorno `NaN` | PE7, VL4 |
| CP-NUM-09 | Trunca notación científica | `value = '1e3'` | `1` | 1. Ejecutar `asNumber('1e3')`<br>2. Verificar retorno `1` | PE8 |
| CP-NUM-10 | Trunca hexadecimal | `value = '0x1A'` | `0` | 1. Ejecutar `asNumber('0x1A')`<br>2. Verificar retorno `0` | PE9 |
| CP-NUM-11 | Ignora espacios | `value = '  42  '` | `42` | 1. Ejecutar `asNumber('  42  ')`<br>2. Verificar retorno `42` | PE10 |
| CP-NUM-12 | Parsea número grande | `value = '999999999'` | `999999999` | 1. Ejecutar `asNumber('999999999')`<br>2. Verificar retorno `999999999` | PE2, VL6 |

---

### 3.3 Conversión de fechas ISO a DateTimeModification (toDateTimeModification)

**Función:** `toDateTimeModification(isoString: string): DateTimeModification`
**Ubicación:** `src/service/helpers.ts:75-82`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-03 |
| **Funcionalidad** | Conversión de fechas ISO a formato DateTimeModification |
| **Descripción** | Verificar que la función extrae date y time de strings ISO usando substring |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `isoString` | ISO datetime completo | `length >= 16` | `{date, time}` |
| PE2 | `isoString` | ISO date solo | `length == 10` | `{date, time: ''}` |
| PE3 | `isoString` | String vacío | `length == 0` | `{date: '', time: ''}` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `isoString` | `''` | PE3 | Longitud mínima | `{date: '', time: ''}` |
| VL2 | `isoString` | `'2025-01-15'` | PE2 | 10 chars | `{date: '2025-01-15', time: ''}` |
| VL3 | `isoString` | `'2025-01-15T10:00'` | PE1 | 16 chars (mínimo para time) | `{date: '2025-01-15', time: '10:00'}` |
| VL4 | `isoString` | `'2025-01-15T10:00:00'` | PE1 | 19 chars | `{date: '2025-01-15', time: '10:00'}` |
| VL5 | `isoString` | `'2025-01-15T10:00:00+02:00'` | PE1 | 25 chars con timezone | `{date: '2025-01-15', time: '10:00'}` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-DTM-01 | Parsea ISO datetime completo | `'2025-01-15T10:00:00'` | `{date: '2025-01-15', time: '10:00'}` | 1. Importar `toDateTimeModification`<br>2. Ejecutar con ISO string<br>3. Verificar date y time | PE1, VL4 |
| CP-DTM-02 | Parsea ISO con timezone | `'2025-12-31T23:59:00+02:00'` | `{date: '2025-12-31', time: '23:59'}` | 1. Ejecutar con timezone<br>2. Verificar extracción correcta | PE1, VL5 |
| CP-DTM-03 | Parsea fecha mínima | `'2000-01-01T00:00:00'` | `{date: '2000-01-01', time: '00:00'}` | 1. Ejecutar con fecha mínima<br>2. Verificar retorno | PE1 |
| CP-DTM-04 | Solo fecha retorna time vacío | `'2025-01-15'` | `{date: '2025-01-15', time: ''}` | 1. Ejecutar con solo fecha<br>2. Verificar time vacío | PE2, VL2 |
| CP-DTM-05 | String vacío retorna vacío | `''` | `{date: '', time: ''}` | 1. Ejecutar con string vacío<br>2. Verificar ambos vacíos | PE3, VL1 |
| CP-DTM-06 | Exactamente 16 chars funciona | `'2025-01-15T10:00'` | `{date: '2025-01-15', time: '10:00'}` | 1. Ejecutar con 16 chars<br>2. Verificar retorno | PE1, VL3 |

---

### 3.4 Extracción de fecha-hora de strings ISO (extractDateTime)

**Función:** `extractDateTime(isoString?: string): string`
**Ubicación:** `src/service/helpers.ts:84-89`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-04 |
| **Funcionalidad** | Extracción de fecha-hora de strings |
| **Descripción** | Verificar que la función retorna los primeros 16 caracteres o string vacío si es null/undefined |
| **Precondiciones** | Ninguna |
| **Prioridad** | Media |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `isoString` | Undefined | `isoString == null` | `''` |
| PE2 | `isoString` | Null | `isoString == null` | `''` |
| PE3 | `isoString` | String vacío | `isoString != null` | `''` |
| PE4 | `isoString` | ISO string válido | `length >= 16` | Primeros 16 chars |
| PE5 | `isoString` | ISO string corto | `length < 16` | String completo |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `isoString` | `undefined` | PE1 | Extremo inferior | `''` |
| VL2 | `isoString` | `null` | PE2 | Extremo inferior | `''` |
| VL3 | `isoString` | `''` | PE3 | String vacío | `''` |
| VL4 | `isoString` | `'2025-01-15T10:00'` | PE4 | Exactly 16 chars | `'2025-01-15T10:00'` |
| VL5 | `isoString` | `'2025-01-15T10:00:00'` | PE4 | 19 chars | `'2025-01-15T10:00'` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-EXT-01 | Retorna vacío para undefined | `undefined` | `''` | 1. Importar `extractDateTime`<br>2. Ejecutar `extractDateTime(undefined)`<br>3. Verificar retorno `''` | PE1, VL1 |
| CP-EXT-02 | Retorna vacío para null | `null` | `''` | 1. Ejecutar `extractDateTime(null)`<br>2. Verificar retorno `''` | PE2, VL2 |
| CP-EXT-03 | Retorna vacío para string vacío | `''` | `''` | 1. Ejecutar `extractDateTime('')`<br>2. Verificar retorno `''` | PE3, VL3 |
| CP-EXT-04 | Extrae 16 chars de ISO válido | `'2025-01-15T10:00:00'` | `'2025-01-15T10:00'` | 1. Ejecutar con ISO string<br>2. Verificar primeros 16 chars | PE4, VL5 |
| CP-EXT-05 | Retorna string completo si es corto | `'2025-01-15T'` | `'2025-01-15T'` | 1. Ejecutar con 11 chars<br>2. Verificar retorno completo | PE5 |

---

### 3.5 Escape de caracteres HTML (escapeHtml)

**Función:** `escapeHtml(message: string): string`
**Ubicación:** `src/service/helpers.ts:103-107`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-05 |
| **Funcionalidad** | Escape de caracteres HTML |
| **Descripción** | Verificar que la función escapa correctamente caracteres especiales de HTML usando textContent/innerHTML |
| **Precondiciones** | Entorno jsdom |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `message` | Sin caracteres especiales | Solo alfanuméricos | Sin cambios |
| PE2 | `message` | Ampersand | Contiene `&` | `'&amp;'` |
| PE3 | `message` | Less-than | Contiene `<` | `'&lt;'` |
| PE4 | `message` | Greater-than | Contiene `>` | `'&gt;'` |
| PE5 | `message` | Double quote | Contiene `"` | `'&quot;'` |
| PE6 | `message` | Script injection | Contiene `<script>` | Escapado completamente |
| PE7 | `message` | String vacío | `length == 0` | `''` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `message` | `''` | PE7 | String vacío | `''` |
| VL2 | `message` | `'&'` | PE2 | Solo ampersand | `'&amp;'` |
| VL3 | `message` | `'<'` | PE3 | Solo less-than | `'&lt;'` |
| VL4 | `message` | `'>'` | PE4 | Solo greater-than | `'&gt;'` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-ESC-01 | String sin especiales sin cambios | `'hello'` | `'hello'` | 1. Importar `escapeHtml`<br>2. Ejecutar `escapeHtml('hello')`<br>3. Verificar retorno `'hello'` | PE1 |
| CP-ESC-02 | Escapa ampersand | `'&'` | `'&amp;'` | 1. Ejecutar `escapeHtml('&')`<br>2. Verificar retorno `'&amp;'` | PE2, VL2 |
| CP-ESC-03 | Escapa less-than | `'<'` | `'&lt;'` | 1. Ejecutar `escapeHtml('<')`<br>2. Verificar retorno `'&lt;'` | PE3, VL3 |
| CP-ESC-04 | Escapa greater-than | `'>'` | `'&gt;'` | 1. Ejecutar `escapeHtml('>')`<br>2. Verificar retorno `'&gt;'` | PE4, VL4 |
| CP-ESC-05 | Escapa double quote | `'"'` | `'&quot;'` | 1. Ejecutar `escapeHtml('"')`<br>2. Verificar retorno `'&quot;'` | PE5 |
| CP-ESC-06 | Escapa script injection | `'<script>alert(1)</script>'` | `'&lt;script&gt;alert(1)&lt;/script&gt;'` | 1. Ejecutar con script tag<br>2. Verificar completamente escapado | PE6 |
| CP-ESC-07 | String vacío retorna vacío | `''` | `''` | 1. Ejecutar `escapeHtml('')`<br>2. Verificar retorno `''` | PE7, VL1 |
| CP-ESC-08 | Múltiples caracteres especiales | `'Tom & Jerry "say" <hi>'` | `'Tom &amp; Jerry &quot;say&quot; &lt;hi&gt;'` | 1. Ejecutar con múltiples especiales<br>2. Verificar todos escapados | PE2-PE5 |

---

### 3.6 Obtención de idiomas soportados (supportedLanguages)

**Función:** `supportedLanguages(): ContentLanguage[]`
**Ubicación:** `src/service/helpers.ts:68-73`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-06 |
| **Funcionalidad** | Obtención de idiomas soportados |
| **Descripción** | Verificar que la función lee `window.SUPPORTED_LANGUAGES` y retorna array parseado, o array vacío si es null |
| **Precondiciones** | Entorno jsdom con `window` |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `window.SUPPORTED_LANGUAGES` | Null | `=== null` | `[]` |
| PE2 | `window.SUPPORTED_LANGUAGES` | Undefined | `== null` (no existe) | `[]` |
| PE3 | `window.SUPPORTED_LANGUAGES` | JSON válido array vacío | `JSON.parse('[]')` | `[]` |
| PE4 | `window.SUPPORTED_LANGUAGES` | JSON válido con datos | `JSON.parse('[...]')` | `ContentLanguage[]` |
| PE5 | `window.SUPPORTED_LANGUAGES` | JSON inválido | `JSON.parse` falla | `SyntaxError` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `window.SUPPORTED_LANGUAGES` | `null` | PE1 | Extremo inferior | `[]` |
| VL2 | `window.SUPPORTED_LANGUAGES` | no existe | PE2 | Propiedad no definida | `[]` |
| VL3 | `window.SUPPORTED_LANGUAGES` | `'[]'` | PE3 | Array válido vacío | `[]` |
| VL4 | `window.SUPPORTED_LANGUAGES` | `'not-json'` | PE5 | String no JSON | `SyntaxError` |

###### Transición de Estados

| ID | Estado Actual | Condición | Acción | Estado Siguiente | Salida |
|----|---------------|-----------|--------|------------------|--------|
| TE1 | S0: Inicio | `window.SUPPORTED_LANGUAGES` no existe | Acceder a propiedad | S1: Undefined | `[]` |
| TE2 | S1: Undefined | `== null` | Retornar array vacío | S1 | `[]` |
| TE3 | S2: Null | `=== null` | Retornar array vacío | S2 | `[]` |
| TE4 | S3: JSON válido | `!= null` y parse exitoso | Retornar array parseado | S4: Datos cargados | `ContentLanguage[]` |
| TE5 | S3: JSON válido | `!= null` y parse falla | Lanzar `SyntaxError` | S3 (error) | Excepción |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-LANG-01 | Retorna array vacío cuando no está definido | `window.SUPPORTED_LANGUAGES` no existe | `[]` | 1. Eliminar `window.SUPPORTED_LANGUAGES`<br>2. Ejecutar `supportedLanguages()`<br>3. Verificar retorno `[]` | PE2, TE1, VL2 |
| CP-LANG-02 | Retorna array vacío cuando es null | `window.SUPPORTED_LANGUAGES = null` | `[]` | 1. Asignar `null`<br>2. Ejecutar `supportedLanguages()`<br>3. Verificar retorno `[]` | PE1, TE2, VL1 |
| CP-LANG-03 | Retorna array vacío para JSON '[]' | `window.SUPPORTED_LANGUAGES = '[]'` | `[]` | 1. Asignar `'[]'`<br>2. Ejecutar `supportedLanguages()`<br>3. Verificar retorno `[]` | PE3, TE4, VL3 |
| CP-LANG-04 | Retorna array con datos | `window.SUPPORTED_LANGUAGES = '[{"locale":"en"}]'` | `[{locale:"en",...}]` | 1. Asignar JSON con 1 elemento<br>2. Ejecutar `supportedLanguages()`<br>3. Verificar retorno con datos | PE4, TE4 |
| CP-LANG-05 | Lanza SyntaxError con JSON inválido | `window.SUPPORTED_LANGUAGES = 'not-json'` | `SyntaxError` | 1. Asignar string no JSON<br>2. Ejecutar `supportedLanguages()`<br>3. Verificar que lanza `SyntaxError` | PE5, TE5, VL4 |

---

### 3.7 Notificación de cambios en formularios (notifyChange)

**Función:** `notifyChange(event: InputEvent, field: { handleChange: (m: any) => void }, valueTransformer?: (v: string) => any): void`
**Ubicación:** `src/service/helpers.ts:91-101`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-07 |
| **Funcionalidad** | Notificación de cambios en formularios |
| **Descripción** | Verificar que la función extrae el valor del input y llama a handleChange con el valor transformado |
| **Precondiciones** | Ninguna |
| **Prioridad** | Media |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `event.currentTarget` | Null | `target == null` | Sin llamada a `handleChange` |
| PE2 | `event.currentTarget` | Elemento válido | `target != null` | Llama a `handleChange(valor)` |
| PE3 | `valueTransformer` | Default | No proporcionado | Identity function `(s) => s` |
| PE4 | `valueTransformer` | Custom | Proporcionado | Aplica transformación |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `event.currentTarget` | `null` | PE1 | Extremo inferior | Sin llamada |
| VL2 | `event.currentTarget` | Elemento mock | PE2 | Caso normal | Llamada con valor |
| VL3 | `target.value` | `''` | PE2 | String vacío | `handleChange('')` |
| VL4 | `target.value` | `'123'` | PE2 | String numérico | `handleChange('123')` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-NCF-01 | No llama handleChange si target es null | `currentTarget = null` | Sin llamada | 1. Importar `notifyChange`<br>2. Crear event con `currentTarget: null`<br>3. Ejecutar `notifyChange(event, field)`<br>4. Verificar que `handleChange` no fue llamado | PE1, VL1 |
| CP-NCF-02 | Llama handleChange con valor del input | `currentTarget.value = 'hello'` | `handleChange('hello')` | 1. Crear event con target mock<br>2. Ejecutar `notifyChange(event, field)`<br>3. Verificar llamada con `'hello'` | PE2, VL2 |
| CP-NCF-03 | Llama handleChange con transformer custom | `value = '123'`, transformer = `Number` | `handleChange(123)` | 1. Ejecutar con transformer numérico<br>2. Verificar que aplica transformación | PE4 |
| CP-NCF-04 | Llama handleChange con valor vacío | `currentTarget.value = ''` | `handleChange('')` | 1. Ejecutar con value vacío<br>2. Verificar llamada con `''` | PE2, VL3 |

---

## 4. Funciones HTTP (service/helpers.ts)

### 4.1 Realización de peticiones HTTP (performRequest)

**Funciones:** `postJson(url, payload)`, `putJson(url, payload)`, `callDelete(url)`
**Función interna:** `performRequest(url, method, payload)`
**Ubicación:** `src/service/helpers.ts:6-52`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-08 |
| **Funcionalidad** | Realización de peticiones HTTP con CSRF |
| **Descripción** | Verificar que las funciones construyen requests correctos con headers CSRF, Content-Type apropiado y body serializado |
| **Precondiciones** | Meta tags CSRF en DOM |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Tabla de Decisión

| ID | C1: `payload instanceof URLSearchParams` | C2: `payload != null` | Acción: `body` | Acción: `Content-Type` |
|----|------------------------------------------|----------------------|----------------|------------------------|
| TD1 | Verdadero | - | `payload` | `application/x-www-form-urlencoded` |
| TD2 | Falso | Verdadero | `JSON.stringify(payload)` | `application/json` |
| TD3 | Falso | Falso | `null` | `application/json` |

**Condiciones compartidas (siempre aplican):**
- `credentials: 'include'`
- `Accept: application/json`
- `X-Requested-With: XMLHttpRequest`
- Headers CSRF leídos de `<meta>` tags

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `payload` | URLSearchParams | `instanceof URLSearchParams` | Content-Type: `application/x-www-form-urlencoded` |
| PE2 | `payload` | Objeto JSON | `!= null && !(instanceof URLSearchParams)` | Content-Type: `application/json` |
| PE3 | `payload` | Null | `=== null` | Content-Type: `application/json`, body: `null` |
| PE4 | `payload` | Undefined | `=== undefined` | Content-Type: `application/json`, body: `null` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `payload` | `new URLSearchParams()` | PE1 | URLSearchParams vacío | Body: URLSearchParams |
| VL2 | `payload` | `{}` | PE2 | Objeto vacío | Body: `'{}'` |
| VL3 | `payload` | `null` | PE3 | Null explícito | Body: `null` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-HTTP-01 | postJson envía URLSearchParams | `url='/api'`, `payload=URLSearchParams({key:'val'})` | Content-Type: `application/x-www-form-urlencoded` | 1. Mockear fetch y CSRF<br>2. Ejecutar `postJson` con URLSearchParams<br>3. Verificar Content-Type | PE1, TD1, VL1 |
| CP-HTTP-02 | postJson envía objeto JSON | `url='/api'`, `payload={foo:'bar'}` | Content-Type: `application/json`, body: `'{"foo":"bar"}'` | 1. Ejecutar `postJson` con objeto<br>2. Verificar Content-Type y body serializado | PE2, TD2 |
| CP-HTTP-03 | callDelete envía null body | `url='/api'` | Method: `DELETE`, body: `null` | 1. Ejecutar `callDelete`<br>2. Verificar method y body | PE3, TD3 |
| CP-HTTP-04 | postJson con undefined envía null | `url='/api'`, `payload=undefined` | Body: `null` | 1. Ejecutar `postJson` con undefined<br>2. Verificar body es `null` | PE4, TD3 |
| CP-HTTP-05 | Incluye headers CSRF | Cualquier payload | Headers incluyen CSRF key/value | 1. Ejecutar cualquier función HTTP<br>2. Verificar headers CSRF | TD1/TD2/TD3 |
| CP-HTTP-06 | putJson envía method PUT | `url='/api'`, `payload={x:1}` | Method: `PUT` | 1. Ejecutar `putJson`<br>2. Verificar method es `PUT` | PE2, TD2 |
| CP-HTTP-07 | putJson con URLSearchParams | `url='/api'`, `payload=URLSearchParams()` | Content-Type: `application/x-www-form-urlencoded` | 1. Ejecutar `putJson` con URLSearchParams<br>2. Verificar Content-Type | PE1, TD1 |

---

### 4.2 Obtención de datos JSON (fetchJson)

**Función:** `fetchJson<T>(url: string): Promise<T>`
**Ubicación:** `src/service/helpers.ts:54-59`

---

#### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-HEL-09 |
| **Funcionalidad** | Obtención de datos JSON |
| **Descripción** | Verificar que la función realiza GET con credentials y retorna JSON parseado |
| **Precondiciones** | Mock de fetch |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | Response | 200 OK con JSON | `ok: true` | JSON parseado |
| PE2 | Response | Error de red | Excepción | Promise rechazada |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | Response | `{ok: true, json: {}}` | PE1 | Respuesta vacía | `{}` |
| VL2 | Response | `{ok: true, json: {data: 1}}` | PE1 | Respuesta con datos | `{data: 1}` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-FJ-01 | Retorna JSON parseado | `url='/api'`, response: `{data: 1}` | `{data: 1}` | 1. Mockear fetch con response<br>2. Ejecutar `fetchJson('/api')`<br>3. Verificar retorno es `{data: 1}` | PE1, VL2 |
| CP-FJ-02 | Usa GET method y credentials | `url='/api'` | Method: `GET`, credentials: `include` | 1. Mockear fetch<br>2. Ejecutar `fetchJson`<br>3. Verificar options del fetch | PE1 |

---

## 5. Servicios

*(Pendiente de documentación)*

---

## 6. Modelos

### 6.1 Funciones auxiliares de AdditionalField

**Archivo:** `src/model/additional-field.ts`

---

#### Verificación de soporte de placeholder (supportsPlaceholder)

**Función:** `supportsPlaceholder(fieldType: AdditionalFieldType): boolean`

##### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-MOD-01 |
| **Funcionalidad** | Verificación de compatibilidad de placeholder |
| **Descripción** | Verificar que la función retorna `true` solo para tipos de campo que soportan placeholder |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `fieldType` | Tipos con placeholder | `'input:text'`, `'input:tel'`, `'vat:eu'`, `'textarea'`, `'input:dateOfBirth'` | `true` |
| PE2 | `fieldType` | Tipos sin placeholder | `'country'`, `'select'`, `'checkbox'`, `'radio'` | `false` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `fieldType` | `'input:text'` | PE1 | Primer tipo con placeholder | `true` |
| VL2 | `fieldType` | `'input:dateOfBirth'` | PE1 | Último tipo con placeholder | `true` |
| VL3 | `fieldType` | `'country'` | PE2 | Primer tipo sin placeholder | `false` |
| VL4 | `fieldType` | `'radio'` | PE2 | Último tipo sin placeholder | `false` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-PH-01 | Retorna true para input:text | `'input:text'` | `true` | 1. Importar `supportsPlaceholder`<br>2. Ejecutar `supportsPlaceholder('input:text')`<br>3. Verificar retorno `true` | PE1, VL1 |
| CP-PH-02 | Retorna true para input:tel | `'input:tel'` | `true` | 1. Ejecutar `supportsPlaceholder('input:tel')`<br>2. Verificar retorno `true` | PE1 |
| CP-PH-03 | Retorna true para vat:eu | `'vat:eu'` | `true` | 1. Ejecutar `supportsPlaceholder('vat:eu')`<br>2. Verificar retorno `true` | PE1 |
| CP-PH-04 | Retorna true para textarea | `'textarea'` | `true` | 1. Ejecutar `supportsPlaceholder('textarea')`<br>2. Verificar retorno `true` | PE1 |
| CP-PH-05 | Retorna true para input:dateOfBirth | `'input:dateOfBirth'` | `true` | 1. Ejecutar `supportsPlaceholder('input:dateOfBirth')`<br>2. Verificar retorno `true` | PE1, VL2 |
| CP-PH-06 | Retorna false para country | `'country'` | `false` | 1. Ejecutar `supportsPlaceholder('country')`<br>2. Verificar retorno `false` | PE2, VL3 |
| CP-PH-07 | Retorna false para select | `'select'` | `false` | 1. Ejecutar `supportsPlaceholder('select')`<br>2. Verificar retorno `false` | PE2 |
| CP-PH-08 | Retorna false para checkbox | `'checkbox'` | `false` | 1. Ejecutar `supportsPlaceholder('checkbox')`<br>2. Verificar retorno `false` | PE2 |
| CP-PH-09 | Retorna false para radio | `'radio'` | `false` | 1. Ejecutar `supportsPlaceholder('radio')`<br>2. Verificar retorno `false` | PE2, VL4 |

---

#### Verificación de soporte de valores restringidos (supportsRestrictedValues)

**Función:** `supportsRestrictedValues(fieldType: AdditionalFieldType): boolean`

##### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-MOD-02 |
| **Funcionalidad** | Verificación de compatibilidad de valores restringidos |
| **Descripción** | Verificar que la función retorna `true` solo para tipos de campo que soportan valores restringidos (checkbox, radio, select) |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `fieldType` | Tipos con valores restringidos | `'checkbox'`, `'radio'`, `'select'` | `true` |
| PE2 | `fieldType` | Tipos sin valores restringidos | `'input:text'`, `'input:tel'`, `'vat:eu'`, `'textarea'`, `'country'`, `'input:dateOfBirth'` | `false` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `fieldType` | `'checkbox'` | PE1 | Primer tipo con valores restringidos | `true` |
| VL2 | `fieldType` | `'select'` | PE1 | Último tipo con valores restringidos | `true` |
| VL3 | `fieldType` | `'input:text'` | PE2 | Primer tipo sin valores restringidos | `false` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-RV-01 | Retorna true para checkbox | `'checkbox'` | `true` | 1. Importar `supportsRestrictedValues`<br>2. Ejecutar `supportsRestrictedValues('checkbox')`<br>3. Verificar retorno `true` | PE1, VL1 |
| CP-RV-02 | Retorna true para radio | `'radio'` | `true` | 1. Ejecutar `supportsRestrictedValues('radio')`<br>2. Verificar retorno `true` | PE1 |
| CP-RV-03 | Retorna true para select | `'select'` | `true` | 1. Ejecutar `supportsRestrictedValues('select')`<br>2. Verificar retorno `true` | PE1, VL2 |
| CP-RV-04 | Retorna false para input:text | `'input:text'` | `false` | 1. Ejecutar `supportsRestrictedValues('input:text')`<br>2. Verificar retorno `false` | PE2, VL3 |
| CP-RV-05 | Retorna false para input:tel | `'input:tel'` | `false` | 1. Ejecutar `supportsRestrictedValues('input:tel')`<br>2. Verificar retorno `false` | PE2 |
| CP-RV-06 | Retorna false para vat:eu | `'vat:eu'` | `false` | 1. Ejecutar `supportsRestrictedValues('vat:eu')`<br>2. Verificar retorno `false` | PE2 |
| CP-RV-07 | Retorna false para textarea | `'textarea'` | `false` | 1. Ejecutar `supportsRestrictedValues('textarea')`<br>2. Verificar retorno `false` | PE2 |
| CP-RV-08 | Retorna false para country | `'country'` | `false` | 1. Ejecutar `supportsRestrictedValues('country')`<br>2. Verificar retorno `false` | PE2 |
| CP-RV-09 | Retorna false para input:dateOfBirth | `'input:dateOfBirth'` | `false` | 1. Ejecutar `supportsRestrictedValues('input:dateOfBirth')`<br>2. Verificar retorno `false` | PE2 |

---

#### Renderizado de tipo de campo (renderAdditionalFieldType)

**Función:** `renderAdditionalFieldType(type: AdditionalFieldType): string`

##### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-MOD-03 |
| **Funcionalidad** | Renderizado de descripción de tipo |
| **Descripción** | Verificar que la función retorna la descripción legible para cada tipo de campo, y 'unknown' para tipos inválidos |
| **Precondiciones** | Ninguna |
| **Prioridad** | Media |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `type` | Tipo válido | Tipo en `additionalFieldTypesWithDescription` | Descripción correspondiente |
| PE2 | `type` | Tipo inválido | Tipo no existente | `'unknown'` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `type` | `'input:text'` | PE1 | Primer tipo del diccionario | `'Single-line text input'` |
| VL2 | `type` | `'input:dateOfBirth'` | PE1 | Último tipo del diccionario | `'Date of birth input'` |
| VL3 | `type` | `'invalid:type'` | PE2 | Tipo inexistente | `'unknown'` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-RT-01 | Retorna descripción para input:text | `'input:text'` | `'Single-line text input'` | 1. Importar `renderAdditionalFieldType`<br>2. Ejecutar con `'input:text'`<br>3. Verificar retorno | PE1, VL1 |
| CP-RT-02 | Retorna descripción para input:tel | `'input:tel'` | `'Phone number input'` | 1. Ejecutar con `'input:tel'`<br>2. Verificar retorno | PE1 |
| CP-RT-03 | Retorna descripción para vat:eu | `'vat:eu'` | `'European VAT number input'` | 1. Ejecutar con `'vat:eu'`<br>2. Verificar retorno | PE1 |
| CP-RT-04 | Retorna descripción para textarea | `'textarea'` | `'Multi-line text input'` | 1. Ejecutar con `'textarea'`<br>2. Verificar retorno | PE1 |
| CP-RT-05 | Retorna descripción para country | `'country'` | `'Country selection drop-down'` | 1. Ejecutar con `'country'`<br>2. Verificar retorno | PE1 |
| CP-RT-06 | Retorna descripción para select | `'select'` | `'Single-choice drop-down'` | 1. Ejecutar con `'select'`<br>2. Verificar retorno | PE1 |
| CP-RT-07 | Retorna descripción para radio | `'radio'` | `'Single-choice radio buttons'` | 1. Ejecutar con `'radio'`<br>2. Verificar retorno | PE1 |
| CP-RT-08 | Retorna descripción para checkbox | `'checkbox'` | `'Multiple-choice checkboxes'` | 1. Ejecutar con `'checkbox'`<br>2. Verificar retorno | PE1 |
| CP-RT-09 | Retorna descripción para input:dateOfBirth | `'input:dateOfBirth'` | `'Date of birth input'` | 1. Ejecutar con `'input:dateOfBirth'`<br>2. Verificar retorno | PE1, VL2 |
| CP-RT-10 | Retorna 'unknown' para tipo inválido | `'invalid:type'` | `'unknown'` | 1. Ejecutar con tipo inexistente<br>2. Verificar retorno `'unknown'` | PE2, VL3 |

---

#### Verificación de soporte de min/max length (supportsMinMaxLength)

**Función:** `supportsMinMaxLength(fieldType: AdditionalFieldType): boolean`

##### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-MOD-04 |
| **Funcionalidad** | Verificación de compatibilidad de min/max length |
| **Descripción** | Verificar que la función retorna `true` para tipos de campo que soportan restricciones de longitud |
| **Precondiciones** | Ninguna |
| **Prioridad** | Media |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `fieldType` | Tipos con min/max length | `'input:text'`, `'input:tel'`, `'textarea'`, `'input:dateOfBirth'` | `true` |
| PE2 | `fieldType` | Tipos sin min/max length | `'vat:eu'`, `'country'`, `'select'`, `'checkbox'`, `'radio'` | `false` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `fieldType` | `'input:text'` | PE1 | Primer tipo con min/max | `true` |
| VL2 | `fieldType` | `'input:dateOfBirth'` | PE1 | Último tipo con min/max | `true` |
| VL3 | `fieldType` | `'vat:eu'` | PE2 | Primer tipo sin min/max | `false` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-MM-01 | Retorna true para input:text | `'input:text'` | `true` | 1. Importar `supportsMinMaxLength`<br>2. Ejecutar con `'input:text'`<br>3. Verificar retorno `true` | PE1, VL1 |
| CP-MM-02 | Retorna true para input:tel | `'input:tel'` | `true` | 1. Ejecutar con `'input:tel'`<br>2. Verificar retorno `true` | PE1 |
| CP-MM-03 | Retorna true para textarea | `'textarea'` | `true` | 1. Ejecutar con `'textarea'`<br>2. Verificar retorno `true` | PE1 |
| CP-MM-04 | Retorna true para input:dateOfBirth | `'input:dateOfBirth'` | `true` | 1. Ejecutar con `'input:dateOfBirth'`<br>2. Verificar retorno `true` | PE1, VL2 |
| CP-MM-05 | Retorna false para vat:eu | `'vat:eu'` | `false` | 1. Ejecutar con `'vat:eu'`<br>2. Verificar retorno `false` | PE2, VL3 |
| CP-MM-06 | Retorna false para country | `'country'` | `false` | 1. Ejecutar con `'country'`<br>2. Verificar retorno `false` | PE2 |
| CP-MM-07 | Retorna false para select | `'select'` | `false` | 1. Ejecutar con `'select'`<br>2. Verificar retorno `false` | PE2 |
| CP-MM-08 | Retorna false para checkbox | `'checkbox'` | `false` | 1. Ejecutar con `'checkbox'`<br>2. Verificar retorno `false` | PE2 |
| CP-MM-09 | Retorna false para radio | `'radio'` | `false` | 1. Ejecutar con `'radio'`<br>2. Verificar retorno `false` | PE2 |

---

### 6.2 Funciones auxiliares de AdditionalItem

**Archivo:** `src/model/additional-item.ts`

---

#### Verificación de obligatoriedad (isMandatory)

**Función:** `isMandatory(supplementPolicy: SupplementPolicy): boolean`

##### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-MOD-05 |
| **Funcionalidad** | Verificación de política obligatoria |
| **Descripción** | Verificar que la función retorna `true` para políticas de suplemento obligatorias |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `supplementPolicy` | Políticas obligatorias | `'MANDATORY_ONE_FOR_TICKET'`, `'MANDATORY_PERCENTAGE_FOR_TICKET'`, `'MANDATORY_PERCENTAGE_RESERVATION'` | `true` |
| PE2 | `supplementPolicy` | Políticas opcionales | `'OPTIONAL_UNLIMITED_AMOUNT'`, `'OPTIONAL_MAX_AMOUNT_PER_TICKET'`, `'OPTIONAL_MAX_AMOUNT_PER_RESERVATION'` | `false` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `supplementPolicy` | `'MANDATORY_ONE_FOR_TICKET'` | PE1 | Primera política obligatoria | `true` |
| VL2 | `supplementPolicy` | `'MANDATORY_PERCENTAGE_RESERVATION'` | PE1 | Última política obligatoria | `true` |
| VL3 | `supplementPolicy` | `'OPTIONAL_UNLIMITED_AMOUNT'` | PE2 | Primera política opcional | `false` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-MAN-01 | Retorna true para MANDATORY_ONE_FOR_TICKET | `'MANDATORY_ONE_FOR_TICKET'` | `true` | 1. Importar `isMandatory`<br>2. Ejecutar con valor<br>3. Verificar retorno `true` | PE1, VL1 |
| CP-MAN-02 | Retorna true para MANDATORY_PERCENTAGE_FOR_TICKET | `'MANDATORY_PERCENTAGE_FOR_TICKET'` | `true` | 1. Ejecutar con valor<br>2. Verificar retorno `true` | PE1 |
| CP-MAN-03 | Retorna true para MANDATORY_PERCENTAGE_RESERVATION | `'MANDATORY_PERCENTAGE_RESERVATION'` | `true` | 1. Ejecutar con valor<br>2. Verificar retorno `true` | PE1, VL2 |
| CP-MAN-04 | Retorna false para OPTIONAL_UNLIMITED_AMOUNT | `'OPTIONAL_UNLIMITED_AMOUNT'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2, VL3 |
| CP-MAN-05 | Retorna false para OPTIONAL_MAX_AMOUNT_PER_TICKET | `'OPTIONAL_MAX_AMOUNT_PER_TICKET'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2 |
| CP-MAN-06 | Retorna false para OPTIONAL_MAX_AMOUNT_PER_RESERVATION | `'OPTIONAL_MAX_AMOUNT_PER_RESERVATION'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2 |

---

#### Verificación de porcentaje obligatorio (isMandatoryPercentage)

**Función:** `isMandatoryPercentage(supplementPolicy: SupplementPolicy): boolean`

##### Caso de Prueba Funcional

| Campo | Descripción |
|-------|-------------|
| **ID** | CPF-MOD-06 |
| **Funcionalidad** | Verificación de política de porcentaje obligatorio |
| **Descripción** | Verificar que la función retorna `true` solo para políticas de porcentaje obligatorio |
| **Precondiciones** | Ninguna |
| **Prioridad** | Alta |

##### Técnicas de Prueba Aplicadas

###### Particiones de Equivalencia

| ID | Campo | Partición | Condición | Salida Esperada |
|----|-------|-----------|-----------|-----------------|
| PE1 | `supplementPolicy` | Políticas de porcentaje obligatorio | `'MANDATORY_PERCENTAGE_FOR_TICKET'`, `'MANDATORY_PERCENTAGE_RESERVATION'` | `true` |
| PE2 | `supplementPolicy` | Otras políticas | `'MANDATORY_ONE_FOR_TICKET'`, `'OPTIONAL_UNLIMITED_AMOUNT'`, `'OPTIONAL_MAX_AMOUNT_PER_TICKET'`, `'OPTIONAL_MAX_AMOUNT_PER_RESERVATION'` | `false` |

###### Análisis de Valores Límite

| ID | Campo | Valor Límite | Partición Asociada | Justificación | Salida Esperada |
|----|-------|--------------|-------------------|---------------|-----------------|
| VL1 | `supplementPolicy` | `'MANDATORY_PERCENTAGE_FOR_TICKET'` | PE1 | Primera política de porcentaje | `true` |
| VL2 | `supplementPolicy` | `'MANDATORY_PERCENTAGE_RESERVATION'` | PE1 | Segunda política de porcentaje | `true` |
| VL3 | `supplementPolicy` | `'MANDATORY_ONE_FOR_TICKET'` | PE2 | Política obligatoria pero no porcentaje | `false` |

##### Catálogo de Pruebas

| ID | Descripción | Datos de Entrada | Resultado Esperado | Pasos de Ejecución | Técnica |
|----|-------------|------------------|-------------------|---------------------|---------|
| CP-MP-01 | Retorna true para MANDATORY_PERCENTAGE_FOR_TICKET | `'MANDATORY_PERCENTAGE_FOR_TICKET'` | `true` | 1. Importar `isMandatoryPercentage`<br>2. Ejecutar con valor<br>3. Verificar retorno `true` | PE1, VL1 |
| CP-MP-02 | Retorna true para MANDATORY_PERCENTAGE_RESERVATION | `'MANDATORY_PERCENTAGE_RESERVATION'` | `true` | 1. Ejecutar con valor<br>2. Verificar retorno `true` | PE1, VL2 |
| CP-MP-03 | Retorna false para MANDATORY_ONE_FOR_TICKET | `'MANDATORY_ONE_FOR_TICKET'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2, VL3 |
| CP-MP-04 | Retorna false para OPTIONAL_UNLIMITED_AMOUNT | `'OPTIONAL_UNLIMITED_AMOUNT'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2 |
| CP-MP-05 | Retorna false para OPTIONAL_MAX_AMOUNT_PER_TICKET | `'OPTIONAL_MAX_AMOUNT_PER_TICKET'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2 |
| CP-MP-06 | Retorna false para OPTIONAL_MAX_AMOUNT_PER_RESERVATION | `'OPTIONAL_MAX_AMOUNT_PER_RESERVATION'` | `false` | 1. Ejecutar con valor<br>2. Verificar retorno `false` | PE2 |

---

## 7. Matriz de Trazabilidad

| Sección | Función/Servicio | PE | AVL | TD | TE | Tests | Estado |
|---------|------------------|:--:|:---:|:--:|:--:|:-----:|--------|
| 2.1 | Factories (test-utils) | ✅ | - | - | - | 14 | Documentado |
| 2.2 | Mocks (test-utils) | ✅ | ✅ | - | - | 7 | Documentado |
| 3.1 | `asString()` | ✅ | ✅ | - | - | 9 | Documentado |
| 3.2 | `asNumber()` | ✅ | ✅ | - | - | 12 | Documentado |
| 3.3 | `toDateTimeModification()` | ✅ | ✅ | - | - | 6 | Documentado |
| 3.4 | `extractDateTime()` | ✅ | ✅ | - | - | 5 | Documentado |
| 3.5 | `escapeHtml()` | ✅ | ✅ | - | - | 8 | Documentado |
| 3.6 | `supportedLanguages()` | ✅ | ✅ | - | ✅ | 5 | Documentado |
| 3.7 | `notifyChange()` | ✅ | ✅ | - | - | 4 | Documentado |
| 4.1 | `performRequest()` | ✅ | ✅ | ✅ | - | 7 | Documentado |
| 4.2 | `fetchJson()` | ✅ | ✅ | - | - | 2 | Documentado |
| 5.1 | `AdditionalFieldService` | | | | | | Pendiente |
| 5.2 | `AdditionalItemService` | | | | | | Pendiente |
| 5.3 | `CustomPaymentMethodsService` | | | | | | Pendiente |
| 5.4 | `EventService` | | | | | | Pendiente |
| 5.5 | `PurchaseContextService` | | | | | | Pendiente |
| 5.6 | `SubscriptionDescriptorService` | | | | | | Pendiente |
| 5.7 | `LocalizationService` | | | | | | Pendiente |
| 5.8 | `ConfigurationService` | | | | | | Pendiente |
| 5.9 | `UtilService` | | | | | | Pendiente |
| 6.1 | `additional-field.ts` helpers | ✅ | ✅ | - | - | 37 | Documentado |
| 6.2 | `additional-item.ts` helpers | ✅ | ✅ | - | - | 12 | Documentado |
