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

### Proceso de Pago

#### Selección de Método de Pago

**CPF-05-001**
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
      <td>CPF-05-001</td>
      <td>Verificar que al seleccionar método OFFLINE y aceptar términos, el botón "Pagar PEN X.XX" se habilite.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Texto: "Tiene X día(s) para completar su pago", Botón: "Pagar PEN X.XX" habilitado</td>
      <td colspan="3">Se muestra el texto informativo y el botón "Confirmar" se habilita correctamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/payment-method-offline-terms-accepted.png" alt="payment-method-offline-terms-accepted"><br>
        Se muestra que al seleccionar Transferencia bancaria y aceptar términos, el botón se habilita.
      </td>
    </tr>
  </tbody>
</table>

**CPF-05-002**
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
      <td>CPF-05-002</td>
      <td>Verificar que al seleccionar método OFFLINE sin aceptar términos, el botón permanezca deshabilitado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Botón: "Pagar PEN X.XX" deshabilitado</td>
      <td colspan="3">El botón permanece deshabilitado hasta que se acepten los términos.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/payment-method-offline-no-terms.png" alt="payment-method-offline-no-terms"><br>
        Se muestra que sin aceptar términos, el botón permanece deshabilitado.
      </td>
    </tr>
  </tbody>
</table>

**CPF-05-005**
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
      <td>CPF-05-005</td>
      <td>Verificar que sin seleccionar método de pago, el sistema muestre mensaje de advertencia.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Mensaje: "Por favor selecciona un método de pago para continuar"</td>
      <td colspan="3">Se muestra el mensaje de advertencia y el botón permanece deshabilitado.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/payment-method-none-selected.png" alt="payment-method-none-selected"><br>
        Se muestra el mensaje de advertencia cuando no se selecciona método de pago.
      </td>
    </tr>
  </tbody>
</table>

**CPF-05-003**
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
      <td>CPF-05-003</td>
      <td>Verificar que al seleccionar método ON_SITE y aceptar términos, el botón "Confirmar" se habilite.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Texto: "Recibirá su entrada...", Botón: "Confirmar" habilitado</td>
      <td colspan="3">Se muestra el texto informativo y el botón "Confirmar" se habilita correctamente.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/payment-method-onsite-selected.png" alt="payment-method-onsite-selected"><br>
        Se muestra que al seleccionar Pago en efectivo y aceptar términos, el botón se habilita.
      </td>
    </tr>
  </tbody>
</table>

**CPF-05-004**
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
      <td>CPF-05-004</td>
      <td>Verificar que al seleccionar método ON_SITE sin aceptar términos, el botón permanezca deshabilitado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">Botón: "Confirmar" deshabilitado</td>
      <td colspan="3">El botón permanece deshabilitado hasta que se acepten los términos.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/payment-method-onsite-no-terms.png" alt="payment-method-onsite-no-terms"><br>
        Se muestra que sin aceptar términos, el botón permanece deshabilitado.
      </td>
    </tr>
  </tbody>
</table>

**CPF-05-006**
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
      <td>CPF-05-006</td>
      <td>Verificar que al cambiar de OFFLINE a ON_SITE, la interfaz cambie según el método seleccionado.</td>
      <td>Manual</td>
      <td>Exitoso</td>
      <td>No se encontraron defectos</td>
    </tr>
    <tr>
      <th colspan="2">Resultado esperado</th>
      <th colspan="3">Resultado obtenido</th>
    </tr>
    <tr>
      <td colspan="2">La interfaz cambia según método seleccionado</td>
      <td colspan="3">El texto informativo y el botón cambian correctamente al seleccionar ON_SITE.</td>
    </tr>
    <tr>
      <th colspan="5">Evidencia</th>
    </tr>
    <tr>
      <td colspan="5">
        <img src="images/functional-tests/run/payment-method-onsite-selected.png" alt="payment-method-onsite-selected"><br>
        Se muestra que la interfaz cambia correctamente al seleccionar Pago en efectivo.
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
