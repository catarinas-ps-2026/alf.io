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

*(Pendiente de documentación)*

---

## 4. Funciones HTTP (service/helpers.ts)

*(Pendiente de documentación)*

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
| 3.1 | `asString()` | | | | | | Pendiente |
| 3.2 | `asNumber()` | | | | | | Pendiente |
| 3.3 | `toDateTimeModification()` | | | | | | Pendiente |
| 3.4 | `extractDateTime()` | | | | | | Pendiente |
| 3.5 | `escapeHtml()` | | | | | | Pendiente |
| 3.6 | `supportedLanguages()` | | | | | | Pendiente |
| 3.7 | `notifyChange()` | | | | | | Pendiente |
| 4.1 | `performRequest()` | | | | | | Pendiente |
| 4.2 | `fetchJson()` | | | | | | Pendiente |
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
