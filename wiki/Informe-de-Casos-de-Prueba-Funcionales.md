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
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-001</td>
      <td>Verificar que el campo nombre no admita cadenas vacías.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar: Error de campo obligatorio</td>
      <td colspan="3">El sistema no permite guardar y marca el campo.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-field-len-0.png" alt="ticket-edit-field-len-0"><br>
        Se muestra que el sistema valida el campo obligatorio.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-002</td>
      <td>Verificar que el campo nombre admita 1 carácter.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Aceptar: Cambio guardado exitosamente</td>
      <td colspan="3">Cambio guardado exitosamente</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-field-len-1.png" alt="ticket-edit-field-len-1"><br>
        Se muestra la edición exitosa con un solo carácter.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-003</td>
      <td>Verificar que el campo nombre admita 254 caracteres.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>500 Unexpected Exception</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Aceptar: Cambio guardado exitosamente</td>
      <td colspan="3">Error 500 del servidor al intentar procesar la solicitud.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-field-len-254.png" alt="ticket-edit-field-len-254"><br>
        Se observa una excepción inesperada al usar cadenas largas.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-004</td>
      <td>Verificar que el campo nombre admita 255 caracteres.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>500 Unexpected Exception</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Aceptar: Cambio guardado exitosamente</td>
      <td colspan="3">Error 500 del servidor al intentar procesar la solicitud.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-field-len-255.png" alt="ticket-edit-field-len-255"><br>
        El sistema falla con error 500 en el límite superior de la base de datos.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-005</td>
      <td>Verificar que el campo nombre rechace 256 caracteres.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar: Error de longitud excedida</td>
      <td colspan="3">El sistema bloquea la entrada o rechaza por validación de frontend.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-field-len-256.png" alt="ticket-edit-field-len-256"><br>
        El sistema rechaza correctamente la longitud excedida.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-006</td>
      <td>Verificar que el campo nombre no admita números.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>El sistema acepta caracteres numéricos.</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar: Error de formato (solo letras)</td>
      <td colspan="3">El sistema acepta y guarda el nombre con números.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-field-numbers.png" alt="ticket-edit-field-numbers"><br>
        Se observa que el sistema no valida el tipo de dato alfanumérico.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-007**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-007</td>
      <td>Verificar que el campo correo no admita cadenas vacías.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar: Error de campo obligatorio</td>
      <td colspan="3">El sistema impide el guardado sin correo.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-email-len-0.png" alt="ticket-edit-email-len-0"><br>
        Validación de correo obligatorio exitosa.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-008**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-008</td>
      <td>Verificar que el campo correo rechace formatos inválidos (sin @).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar: Error de formato de correo</td>
      <td colspan="3">Rechazo por formato inválido.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-email-invalid.png" alt="ticket-edit-email-invalid"><br>
        El sistema detecta correctamente el formato de correo inválido.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-009**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-009</td>
      <td>Verificar correo con longitud de 63 caracteres.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Aceptar: Cambio guardado exitosamente</td>
      <td colspan="3">Cambio guardado exitosamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-email-len-63.png" alt="ticket-edit-email-len-63"><br>
        Prueba positiva de longitud de correo exitosa.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-010**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-010</td>
      <td>Verificar correo con longitud de 64 caracteres.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Aceptar: Cambio guardado exitosamente</td>
      <td colspan="3">Cambio guardado exitosamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-email-len-64.png" alt="ticket-edit-email-len-64"><br>
        Prueba positiva en el límite de 64 caracteres exitosa.
      </td>
    </tr>
  </tbody>
</table>

**CPF-01-011**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-01-011</td>
      <td>Verificar que el campo correo rechace 65 caracteres.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>El sistema acepta correos de más de 64 caracteres.</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar: Error de longitud excedida</td>
      <td colspan="3">El sistema permite guardar el correo de 65 caracteres.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-edit-email-len-65.png" alt="ticket-edit-email-len-65"><br>
        Se muestra que el sistema no aplica la restricción de longitud en el correo.
      </td>
    </tr>
  </tbody>
</table>

### Búsqueda de Reservas (Panel de Administración)

**CPF-02-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-02-001</td>
      <td>Búsqueda por ID de reserva existente.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El sistema muestra la reserva específica</td>
      <td colspan="3">Se muestra la reserva correspondiente al ID.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reserve-search-id.png" alt="reserve-search-id"><br>
        Búsqueda exitosa por identificador único.
      </td>
    </tr>
  </tbody>
</table>

**CPF-02-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-02-002</td>
      <td>Búsqueda por apellido de asistente.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El sistema lista todas las reservas bajo ese apellido</td>
      <td colspan="3">Resultados filtrados correctamente por apellido.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reserve-search-lastname.png" alt="reserve-search-lastname"><br>
        Búsqueda por criterio de texto exitosa.
      </td>
    </tr>
  </tbody>
</table>

**CPF-02-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-02-003</td>
      <td>Búsqueda de valor inexistente.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El sistema muestra mensaje "Sin resultados"</td>
      <td colspan="3">Mensaje de "No results found" mostrado correctamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reserve-search-none.png" alt="reserve-search-none"><br>
        Comportamiento esperado ante búsqueda sin coincidencias.
      </td>
    </tr>
  </tbody>
</table>

### Gestión de Estados de Reserva y Pagos

**CPF-03-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-03-001</td>
      <td>Transición de PENDING a COMPLETE tras aceptar pago.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">La reserva cambia a estado COMPLETE</td>
      <td colspan="3">Cambio de estado reflejado correctamente en la tabla.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reservation-approve-payment-1.png" alt="approve-1">
        <img src="images/functional-tests/run/reservation-approve-payment-2.png" alt="approve-2">
        <img src="images/functional-tests/run/reservation-approve-payment-3.png" alt="approve-3"><br>
        Flujo completo de aprobación de pago.
      </td>
    </tr>
  </tbody>
</table>

**CPF-03-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-03-002</td>
      <td>Transición de PENDING a CANCELLED tras cancelar pago.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">La reserva cambia a estado CANCELLED</td>
      <td colspan="3">Cambio de estado reflejado correctamente en la tabla.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reservation-cancel-1.png" alt="cancel-1">
        <img src="images/functional-tests/run/reservation-cancel-2.png" alt="cancel-2">
        <img src="images/functional-tests/run/reservation-cancel-3.png" alt="cancel-3"><br>
        Flujo completo de cancelación de reserva.
      </td>
    </tr>
  </tbody>
</table>

**CPF-03-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-03-003</td>
      <td>Clic fuera del modal para regresar al inicio.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El modal se cierra y el estado se mantiene</td>
      <td colspan="3">El modal se cierra y el estado se mantiene.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reservation-approve-payment-2.png" alt="modal-open">
        <img src="images/functional-tests/run/reservation-approve-payment-1.png" alt="state-result"><br>
        Al cerrar el modal, se regresa al estado inicial de la lista.
      </td>
    </tr>
  </tbody>
</table>

**CPF-03-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-03-004</td>
      <td>Botón "Marcar como completa" visible (Llenado=SI, Pago=Offline, Aprobado=SI).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Se visualiza el botón para marcar como completa</td>
      <td colspan="3">Botón visible en la interfaz de administración.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reservation-mark-as-complete-should-show.png" alt="mark-complete-show"><br>
        El botón aparece según las reglas de la tabla de decisión.
      </td>
    </tr>
  </tbody>
</table>

**CPF-03-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-03-005</td>
      <td>Botón oculto (Llenado=SI, Pago=Presencial, Aprobado=NO).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de marcar como completa se oculta</td>
      <td colspan="3">Botón no presente en la UI.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reservation-mark-as-complete-shouldnt-show.png" alt="mark-complete-hide"><br>
        El botón se oculta correctamente para pagos presenciales no aprobados.
      </td>
    </tr>
  </tbody>
</table>

**CPF-03-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-03-006</td>
      <td>Botón oculto cuando no se completó el llenado.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>El botón se muestra aunque el llenado sea incompleto.</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de marcar como completa se oculta</td>
      <td colspan="3">El botón es visible. Al hacer clic, se realiza la petición, falla sin error visible y recarga la página.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/reservation-mark-as-complete-should-show.png" alt="mark-complete-bug"><br>
        Se observa la presencia del botón a pesar de no cumplir con las condiciones de llenado.
      </td>
    </tr>
  </tbody>
</table>

### Disponibilidad de Descarga de Tickets

**CPF-04-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-04-001</td>
      <td>No mostrar botón de descarga si el evento ya pasó.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de descarga no se muestra</td>
      <td colspan="3">Botón oculto para eventos pasados.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-download-not-available.png" alt="download-not-past"><br>
        El sistema restringe la descarga post-evento.
      </td>
    </tr>
  </tbody>
</table>

**CPF-04-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-04-002</td>
      <td>Descarga disponible para evento presencial con pago aprobado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de descarga es visible y funcional</td>
      <td colspan="3">Botón visible y permite la descarga.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-download-available.png" alt="download-available"><br>
        Escenario de éxito para descarga de ticket presencial.
      </td>
    </tr>
  </tbody>
</table>

**CPF-04-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-04-003</td>
      <td>No mostrar botón si el pago está pendiente (Presencial).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de descarga permanece oculto</td>
      <td colspan="3">Botón oculto por falta de pago.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-download-not-available.png" alt="download-not-unpaid"><br>
        Restricción de descarga por estado de pago.
      </td>
    </tr>
  </tbody>
</table>

**CPF-04-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-04-004</td>
      <td>Descarga disponible para evento híbrido con pago aprobado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de descarga es visible y funcional</td>
      <td colspan="3">Botón visible y permite la descarga.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-download-available.png" alt="download-available-hybrid"><br>
        Acceso a ticket en modalidad híbrida exitoso.
      </td>
    </tr>
  </tbody>
</table>

**CPF-04-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-04-005</td>
      <td>No mostrar botón si el pago está pendiente (Híbrido).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de descarga permanece oculto</td>
      <td colspan="3">Botón oculto por falta de pago.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-download-not-available.png" alt="download-not-hybrid-unpaid"><br>
        Bloqueo de descarga en modalidad híbrida sin pago.
      </td>
    </tr>
  </tbody>
</table>

**CPF-04-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CPF-04-006</td>
      <td>No mostrar botón de descarga para modalidad Virtual.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">El botón de descarga no se muestra (acceso digital)</td>
      <td colspan="3">Botón oculto según lógica de negocio para eventos virtuales.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/ticket-download-not-available.png" alt="download-not-virtual"><br>
        Verificación de lógica de negocio para accesos virtuales.
      </td>
    </tr>
  </tbody>
</table>

### Configuración de la Organización (CONF-01)

**CONF-01-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-01-001</td>
      <td>Crear organización con datos válidos.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Registro y redirección exitosa.</td>
      <td colspan="3">La organización se crea y aparece en la lista de administración.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-org-create-input.png" alt="config-org-create-input">
        <img src="images/functional-tests/run/config-org-create-success.png" alt="config-org-create-success"><br>
        Formulario completado y creación exitosa de la organización.
      </td>
    </tr>
  </tbody>
</table>

**CONF-01-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-01-002</td>
      <td>Intentar crear organización con nombre vacío.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar guardado indicando campo requerido.</td>
      <td colspan="3">El sistema previene el envío y resalta el campo vacío.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-org-create-empty-name.png" alt="config-org-create-empty-name"><br>
        Validación del frontend al intentar omitir el nombre.
      </td>
    </tr>
  </tbody>
</table>

**CONF-01-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-01-003</td>
      <td>Intentar crear organización con email de formato inválido.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar guardado indicando error de formato.</td>
      <td colspan="3">El sistema muestra mensaje de error de correo inválido.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-org-create-invalid-email.png" alt="config-org-create-invalid-email"><br>
        Validación del formato del correo electrónico.
      </td>
    </tr>
  </tbody>
</table>

**CONF-01-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-01-004</td>
      <td>Crear organización con nombre de 255 caracteres.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Guardar exitosamente el registro en el límite superior.</td>
      <td colspan="3">Se guarda el nombre y se muestra completo en la vista.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-org-create-name-255-input.png" alt="config-org-create-name-255-input">
        <img src="images/functional-tests/run/config-org-create-name-255-view.png" alt="config-org-create-name-255-view"><br>
        Creación y guardado exitoso con longitud máxima de 255 caracteres.
      </td>
    </tr>
  </tbody>
</table>

**CONF-01-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-01-005</td>
      <td>Editar los datos de una organización existente.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Actualizar los datos exitosamente.</td>
      <td colspan="3">Los cambios se aplican y persisten en la base de datos.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-org-edit-input.png" alt="config-org-edit-input">
        <img src="images/functional-tests/run/config-org-edit-success.png" alt="config-org-edit-success"><br>
        Edición de datos de la organización guardada exitosamente.
      </td>
    </tr>
  </tbody>
</table>

**CONF-01-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-01-006</td>
      <td>Crear organización con nombre de 256 caracteres.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>SQL error message exposure</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar indicando que excede la longitud máxima.</td>
      <td colspan="3">El sistema lanza un error 500 exponiendo la excepción SQL cruda de base de datos.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-org-create-name-256-sql-error.png" alt="config-org-create-name-256-sql-error"><br>
        Error 500 expone información sensible de base de datos (SQL).
      </td>
    </tr>
  </tbody>
</table>

### Configuración del Evento (CONF-02)

**CONF-02-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-001</td>
      <td>Configurar fechas del evento con el date picker.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>date picker scroll bug</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Seleccionar fechas de forma sencilla y fluida.</td>
      <td colspan="3">El widget del date picker tiene un problema de scroll que dificulta su uso.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-date-setup.png" alt="config-evt-date-setup"><br>
        Bug de scroll visualizado al abrir el selector de fechas.
      </td>
    </tr>
  </tbody>
</table>

**CONF-02-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-002</td>
      <td>Configurar fecha de inicio del evento en el pasado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Permitir ingresar la fecha en el pasado en el formulario.</td>
      <td colspan="3">El sistema permite seleccionar la fecha pasada en el campo de entrada.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-past-start.png" alt="config-evt-past-start"><br>
        Fecha de inicio en el pasado ingresada en el formulario de configuración.
      </td>
    </tr>
  </tbody>
</table>

**CONF-02-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-003</td>
      <td>Validar error al guardar evento con fecha de inicio en el pasado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar el guardado mostrando error de validación.</td>
      <td colspan="3">El sistema muestra alerta de error indicando fecha inválida.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-past-error.png" alt="config-evt-past-error"><br>
        Mensaje de error al intentar guardar un evento con fecha pasada.
      </td>
    </tr>
  </tbody>
</table>

**CONF-02-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-004</td>
      <td>Validar configuración con fecha de inicio y fin idénticas.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar por conflicto lógico de tiempos.</td>
      <td colspan="3">El sistema muestra error de validación impidiendo el guardado.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-same-start-end.png" alt="config-evt-same-start-end"><br>
        Error mostrado cuando las fechas de inicio y fin coinciden exactamente.
      </td>
    </tr>
  </tbody>
</table>

**CONF-02-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-005</td>
      <td>Configurar disponibilidad de categoría después de inicio del evento.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Guardar la configuración de disponibilidad de tickets.</td>
      <td colspan="3">El sistema procesa y guarda la disponibilidad de la categoría.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-cat-avail-after-event.png" alt="config-evt-cat-avail-after-event"><br>
        Configuración guardada de tickets disponibles post-inicio del evento.
      </td>
    </tr>
  </tbody>
</table>

**CONF-02-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-006</td>
      <td>Configurar fin de venta de categoría después del fin del evento.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>silent save disable</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar mostrando advertencia descriptiva al usuario.</td>
      <td colspan="3">El botón guardar se inhabilita silenciosamente sin indicar la causa.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-cat-end-after-event.png" alt="config-evt-cat-end-after-event"><br>
        Botón de guardado inactivo de forma silenciosa sin feedback de error.
      </td>
    </tr>
  </tbody>
</table>

**CONF-02-007**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-02-007</td>
      <td>Configurar códigos ocultos duplicados en diferentes categorías.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>duplicate hidden code</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar la duplicidad del código de acceso.</td>
      <td colspan="3">El sistema guarda el mismo código oculto en múltiples categorías.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-evt-duplicate-hidden-code.png" alt="config-evt-duplicate-hidden-code"><br>
        Aceptación del mismo código de acceso duplicado en la configuración.
      </td>
    </tr>
  </tbody>
</table>

### Configuración de Categorías de Tickets (CONF-03)

**CONF-03-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-03-001</td>
      <td>Modificar el precio de una categoría de ticket existente.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Actualizar el precio y reflejar el cambio en la lista.</td>
      <td colspan="3">El precio se modifica y se confirma el cambio exitosamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-ticket-edit-price.png" alt="config-ticket-edit-price">
        <img src="images/functional-tests/run/config-ticket-edit-price-success.png" alt="config-ticket-edit-price-success"><br>
        Edición de precio de categoría ingresada y guardada correctamente.
      </td>
    </tr>
  </tbody>
</table>

**CONF-03-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-03-002</td>
      <td>Ingresar un precio negativo en una categoría de ticket.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar el precio negativo con validación.</td>
      <td colspan="3">El sistema impide guardar y muestra error de formato numérico.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-ticket-negative-price.png" alt="config-ticket-negative-price"><br>
        Error del sistema al intentar guardar un precio negativo.
      </td>
    </tr>
  </tbody>
</table>

**CONF-03-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-03-003</td>
      <td>Configurar una categoría de tickets gratuitos (precio cero).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Crear y guardar la categoría como gratuita.</td>
      <td colspan="3">Se configura correctamente a precio 0.00 y se guarda.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-ticket-free-select.png" alt="config-ticket-free-select">
        <img src="images/functional-tests/run/config-ticket-free-success.png" alt="config-ticket-free-success"><br>
        Selección y confirmación exitosa de categoría de tickets gratuitos.
      </td>
    </tr>
  </tbody>
</table>

**CONF-03-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-03-004</td>
      <td>Configurar categoría VIP con precio diferenciado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Guardar la categoría VIP y reflejar su precio elevado.</td>
      <td colspan="3">Categoría VIP creada correctamente en el panel.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-ticket-vip-success.png" alt="config-ticket-vip-success"><br>
        Creación y visualización exitosa de la categoría VIP.
      </td>
    </tr>
  </tbody>
</table>

**CONF-03-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-03-005</td>
      <td>Crear categoría oculta con código de acceso.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Crear la categoría oculta y requerir código para su acceso.</td>
      <td colspan="3">Categoría configurada con el código de acceso correctamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-ticket-hidden-access.png" alt="config-ticket-hidden-access"><br>
        Configuración del código de acceso para categoría oculta.
      </td>
    </tr>
  </tbody>
</table>

**CONF-03-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-03-006</td>
      <td>Eliminar categoría oculta como administrador.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Eliminar el registro completamente del sistema.</td>
      <td colspan="3">La categoría se remueve exitosamente de la lista.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-ticket-hidden-delete-admin.png" alt="config-ticket-hidden-delete-admin"><br>
        Confirmación de eliminación de la categoría oculta en el panel.
      </td>
    </tr>
  </tbody>
</table>

### Gestión de Capacidad (CONF-04)

**CONF-04-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-04-001</td>
      <td>Configurar categorías cuya capacidad supere el límite del evento.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar guardado indicando exceso del límite del evento.</td>
      <td colspan="3">El sistema muestra error e impide exceder el total configurado.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-capacity-event-limit-error.png" alt="config-capacity-event-limit-error"><br>
        Validación del límite de capacidad del evento al configurar categorías.
      </td>
    </tr>
  </tbody>
</table>

**CONF-04-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-04-002</td>
      <td>Ingresar cantidad inválida o nula de tickets en una categoría.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Rechazar guardado por entrada numérica inválida.</td>
      <td colspan="3">El sistema bloquea la acción y señala el campo incorrecto.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-capacity-invalid-tickets.png" alt="config-capacity-invalid-tickets"><br>
        Error mostrado ante cantidad de tickets inválida.
      </td>
    </tr>
  </tbody>
</table>

**CONF-04-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-04-003</td>
      <td>Comprar el último ticket disponible de una categoría.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Procesar la compra y dejar la capacidad en cero.</td>
      <td colspan="3">Compra exitosa y actualización inmediata de la disponibilidad.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-capacity-buy-last.png" alt="config-capacity-buy-last"><br>
        Visualización de la compra exitosa del último ticket disponible.
      </td>
    </tr>
  </tbody>
</table>

**CONF-04-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-04-004</td>
      <td>Comprar tickets respetando el límite máximo por transacción.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Permitir la compra si está dentro del límite (ej. máx 2).</td>
      <td colspan="3">Compra procesada exitosamente al respetar la cantidad permitida.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-capacity-max-2-buy-1.png" alt="config-capacity-max-2-buy-1"><br>
        Compra exitosa de un ticket respetando la restricción de máximo dos.
      </td>
    </tr>
  </tbody>
</table>

**CONF-04-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-04-005</td>
      <td>Verificar estado de categoría cuando se agotan los tickets.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Deshabilitar la venta y mostrar la etiqueta "Sold out".</td>
      <td colspan="3">La interfaz pública muestra "Sold out" y bloquea la selección.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-capacity-sold-out.png" alt="config-capacity-sold-out"><br>
        Categoría de tickets agotada en la vista pública de selección.
      </td>
    </tr>
  </tbody>
</table>

### Configuración de Impuestos (CONF-05)

**CONF-05-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-05-001</td>
      <td>Configurar y aplicar un nuevo impuesto (VAT/IVA).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Guardar y asociar el impuesto a la categoría de ticket.</td>
      <td colspan="3">El impuesto se configura y se aplica al precio correctamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-tax-setup.png" alt="config-tax-setup"><br>
        Formulario de configuración de impuestos guardado correctamente.
      </td>
    </tr>
  </tbody>
</table>

**CONF-05-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-05-002</td>
      <td>Actualizar la tasa del impuesto configurado a un valor de 0%.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Actualizar y guardar el impuesto con tasa 0%.</td>
      <td colspan="3">Se guarda el cambio de tasa a 0% exitosamente en el panel.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-tax-0-percent-update.png" alt="config-tax-0-percent-update"><br>
        Guardado de tasa de impuesto al 0% en la configuración.
      </td>
    </tr>
  </tbody>
</table>

**CONF-05-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-05-003</td>
      <td>Configurar y aplicar exención de impuestos (tax-free) a una categoría.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Configurar la categoría libre de cargos impositivos.</td>
      <td colspan="3">Se desvinculan los impuestos del precio de la categoría.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-tax-free-update.png" alt="config-tax-free-update"><br>
        Actualización de categoría a exenta de impuestos.
      </td>
    </tr>
  </tbody>
</table>

### Configuración de Localización y Moneda (CONF-06)

**CONF-06-001**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-06-001</td>
      <td>Seleccionar el idioma por defecto del sistema.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Cambiar el idioma principal de la configuración.</td>
      <td colspan="3">El sistema actualiza el idioma de visualización correctamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-loc-language-selector.png" alt="config-loc-language-selector"><br>
        Selector de idioma configurado en la administración de la organización.
      </td>
    </tr>
  </tbody>
</table>

**CONF-06-002**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-06-002</td>
      <td>Traducir los detalles del evento a un idioma secundario.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Guardar los campos de traducción correspondientes.</td>
      <td colspan="3">Traducciones guardadas y aplicadas a los campos del evento.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-loc-translation-input.png" alt="config-loc-translation-input"><br>
        Campos de traducción completados y guardados para el evento.
      </td>
    </tr>
  </tbody>
</table>

**CONF-06-003**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-06-003</td>
      <td>Validar el límite mínimo de idiomas requeridos al intentar eliminar.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Prevenir la eliminación si solo queda el idioma base.</td>
      <td colspan="3">El sistema restringe la eliminación en el límite mínimo de idiomas.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-loc-delete-language-boundary.png" alt="config-loc-delete-language-boundary"><br>
        Validación del límite mínimo de idiomas permitidos en el sistema.
      </td>
    </tr>
  </tbody>
</table>

**CONF-06-004**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-06-004</td>
      <td>Validar advertencia por desfase de zona horaria del evento.</td>
      <td>Manual</td>
      <td>Fallido</td>
      <td>event timezone warning</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Mostrar alerta explicativa de discrepancia de zona horaria.</td>
      <td colspan="3">Se muestra un mensaje de advertencia sobre la zona horaria del evento.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-loc-timezone-warning.png" alt="config-loc-timezone-warning"><br>
        Mensaje de advertencia por desfase de la zona horaria detectada.
      </td>
    </tr>
  </tbody>
</table>

**CONF-06-005**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-06-005</td>
      <td>Cambiar la moneda por defecto del evento a Euros (EUR).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Actualizar la moneda del evento a EUR.</td>
      <td colspan="3">La moneda se actualiza a EUR y se muestra en la tienda pública.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-loc-currency-eur.png" alt="config-loc-currency-eur"><br>
        Moneda configurada a Euros (EUR) en la administración.
      </td>
    </tr>
  </tbody>
</table>

**CONF-06-006**
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Descripción</th>
      <th>Tipo</th>
      <th>Estado</th>
      <th>Defectos</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CONF-06-006</td>
      <td>Cambiar la moneda por defecto del evento a Soles (PEN).</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Actualizar la moneda del evento a PEN.</td>
      <td colspan="3">La moneda se actualiza a PEN y se muestra en la tienda pública.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/config-loc-currency-pen.png" alt="config-loc-currency-pen"><br>
        Moneda configurada a Soles Peruanos (PEN) en la administración.
      </td>
    </tr>
  </tbody>
</table>

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
