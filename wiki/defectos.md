# Defectos Encontrados — Integration Tests Mandatory Endpoints

Fecha: 2026-07-19
Archivo: `ReservationApiV2ControllerIntegrationTest.java`

---

## Defecto 1: `cancelPendingReservation` no valida existencia de reservación

**Archivo:** `src/main/java/alfio/controller/api/v2/user/ReservationApiV2Controller.java:370-375`
**Test:** `ReservationApiV2ControllerIntegrationTest.cancelNonexistentReservationReturns404()`

**Comportamiento esperado:** Al intentar cancelar una reservación con un ID inexistente, el endpoint debería retornar HTTP **404 Not Found**, ya que el recurso no existe.

**Comportamiento real:** El endpoint retorna HTTP **200 OK** con `body = true`.

**Por qué es un defecto:**
El método `cancelPendingReservation` usa `getReservationWithPendingStatus(reservationId).ifPresent(...)` que simplemente no ejecuta la cancelación si no encuentra la reservación, pero **siempre** retorna `ResponseEntity.ok(true)` sin importar si la reservación existió o no. Esto viola el principio de que un endpoint DELETE sobre un recurso inexistente debe informar que el recurso no fue encontrado. Un cliente que reciba 200 asumirá que la cancelación fue exitosa cuando en realidad no ocurrió nada.

---

## Defecto 2: `initTransaction` retorna 404 para bank transfer válido

**Archivo:** `src/main/java/alfio/controller/api/v2/user/ReservationApiV2Controller.java:917-938`
**Test:** `ReservationApiV2ControllerIntegrationTest.initBankTransferPayment()`

**Comportamiento esperado:** Al inicializar una transacción de tipo bank transfer (que es un pago offline que no necesita redirección), el endpoint debería retornar HTTP **201 Created** indicando que el token de transacción fue creado exitosamente.

**Comportamiento real:** El endpoint retorna HTTP **404 Not Found**.

**Por qué es un defecto:**
El método `initTransaction` llama a `ticketReservationManager.initTransaction()` que para bank transfer retorna `Optional.empty()` (porque no hay URL de redirección que generar). El controller interpreta esto como "no encontrado" y retorna 404. Sin embargo, bank transfer es un método de pago válido que requiere inicialización (crear el registro de transacción), solo que no produce un token con URL de redirección. El 404 es engañoso: la reservación existe y el método de pago es válido, simplemente el flujo de inicialización es diferente para pagos offline.

---

## Defecto 3: `getTransactionStatus` retorna 404 sin transacción previa

**Archivo:** `src/main/java/alfio/controller/api/v2/user/ReservationApiV2Controller.java:962-989`
**Test:** `ReservationApiV2ControllerIntegrationTest.checkPaymentStatusForPendingPayment()`

**Comportamiento esperado:** Al consultar el estado de una transacción para una reservación que existe pero que aún no ha inicializado ninguna transacción, el endpoint debería retornar HTTP **200 OK** con un body que indique que no hay transacción activa (ej: `isSuccess() == false`).

**Comportamiento real:** El endpoint retorna HTTP **404 Not Found**.

**Por qué es un defecto:**
El método `getTransactionStatus` usa `paymentManager.getTransactionStatus()` que retorna `Optional.empty()` cuando no hay transacción registrada. El controller convierte esto en 404. Sin embargo, el 404 técnicamente significa "la reservación no existe", cuando en realidad la reservación **sí existe** pero simplemente no tiene una transacción asociada todavía. Un 200 con un body que indique "sin transacción" sería semánticamente correcto y permitiría al cliente distinguir entre "reservación no encontrada" (404) y "reservación encontrada pero sin transacción" (200 con estado vacío).

---

## Defecto 4: `cancelPendingReservation` no rechaza reservaciones confirmadas (COMPLETE)

**Archivo:** `src/main/java/alfio/controller/api/v2/user/ReservationApiV2Controller.java:370-375`
**Test:** `ReservationApiV2ControllerIntegrationTest.cancelConfirmedReservationShouldFailButReturnsSuccess()`

**Contexto:** El endpoint público `DELETE /reservation/{id}` (`cancelPendingReservation`) solo debe cancelar reservaciones en estado PENDING. Las reservaciones confirmadas (COMPLETE) solo pueden ser canceladas desde el módulo de administración (otro endpoint/controller).

**Comportamiento esperado:** Al intentar cancelar una reservación COMPLETE desde el endpoint público, debería retornar un error (ej: 400 o 403), indicando que esta operación no está permitida para este endpoint.

**Comportamiento real:** El endpoint retorna HTTP **200 OK** con `body = true`, pero la reservación **no se cancela** — su estado sigue siendo COMPLETE.

**Por qué es un defecto:**
El método `cancelPendingReservation` usa `getReservationWithPendingStatus(reservationId)` que solo busca reservaciones PENDING. Para una reservación COMPLETE, el Optional está vacío, el `ifPresent` no ejecuta nada, pero el método igual retorna `true`. El cliente recibe una confirmación falsa de que la cancelación fue exitosa cuando en realidad no ocurrió nada. El endpoint debería rechazar explícitamente la operación con un código de error apropiado.

---

## Resumen

| #   | Test                                                    | Status Actual    | Status Esperado           | Severidad |
| --- | ------------------------------------------------------- | ---------------- | ------------------------- | --------- |
| 1   | `cancelNonexistentReservationReturns404`                | 200              | 404                       | Media     |
| 2   | `initBankTransferPayment`                               | 404              | 201                       | Media     |
| 3   | `checkPaymentStatusForPendingPayment`                   | 404              | 200                       | Baja      |
| 4   | `cancelConfirmedReservationShouldFailButReturnsSuccess` | 200 (sin efecto) | Error (rechazo explícito) | Alta      |
