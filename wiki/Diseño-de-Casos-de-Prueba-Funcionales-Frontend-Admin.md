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

*(Pendiente de documentación - Fase 2)*

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
| 6.1 | `additional-field.ts` helpers | | | | | | Pendiente |
| 6.2 | `additional-item.ts` helpers | | | | | | Pendiente |
