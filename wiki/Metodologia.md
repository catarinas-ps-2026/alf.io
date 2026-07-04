# Metodología General de Pruebas de Software

## 1. Objetivo

La presente metodología establece el proceso general de pruebas de software que será aplicado durante el desarrollo y validación del proyecto **alf.io**. Su propósito es proporcionar un marco de trabajo sistemático para planificar, diseñar, ejecutar y documentar las actividades de aseguramiento de la calidad del software, garantizando que el sistema satisfaga los requisitos funcionales y no funcionales establecidos.

---

# 2. Estrategia General de Pruebas

La estrategia de pruebas del proyecto sigue un enfoque **incremental**, donde cada nivel de prueba verifica un aspecto específico del sistema antes de continuar con el siguiente. De esta manera, los defectos son detectados y corregidos progresivamente, disminuyendo el riesgo de propagación hacia etapas posteriores del desarrollo.

El proceso de validación inicia con la comprobación del comportamiento interno de cada componente mediante pruebas unitarias, continúa con la validación de las funcionalidades implementadas, posteriormente verifica la interacción entre los diferentes módulos mediante pruebas de integración y culmina con las pruebas de sistema, donde se evalúa el comportamiento global de la aplicación desde la perspectiva del usuario final.

El flujo general de la metodología se representa de la siguiente manera:

```text
Desarrollo del Software
          │
          ▼
 Planificación de Pruebas
          │
          ▼
 Diseño de Casos de Prueba
          │
          ▼
 Preparación del Entorno
          │
          ▼
 Pruebas Unitarias
          │
          ▼
 Corrección de Defectos
          │
          ▼
 Pruebas Funcionales
          │
          ▼
 Corrección de Defectos
          │
          ▼
 Pruebas de Integración
          │
          ▼
 Corrección de Defectos
          │
          ▼
 Pruebas de Sistema
          │
          ▼
 Pruebas de Regresión
          │
          ▼
 Documentación y Liberación
```

---

# 3. Planificación de las Pruebas

La planificación constituye la primera fase del proceso de pruebas y tiene como finalidad definir la estrategia que será seguida durante todo el ciclo de validación del software.

Durante esta etapa se establecen:

- El alcance de las pruebas.
- Los objetivos de calidad.
- Los componentes que serán evaluados.
- Los recursos humanos y tecnológicos necesarios.
- El cronograma de ejecución.
- Los riesgos asociados al proceso.
- Los criterios de entrada y salida para cada nivel de prueba.

Asimismo, se seleccionan las herramientas que serán utilizadas para la automatización de pruebas y se definen las responsabilidades de los integrantes del equipo.

---

# 4. Diseño de las Pruebas

Una vez definida la planificación, se procede al diseño de las pruebas de acuerdo con el nivel de validación correspondiente. Cada tipo de prueba posee objetivos, técnicas y criterios de diseño particulares, permitiendo evaluar distintos aspectos de la calidad del software.

Los casos de prueba son elaborados considerando los requisitos del sistema, la arquitectura de la aplicación y los riesgos identificados durante la planificación.

## 4.1 Diseño de Pruebas Unitarias

Las pruebas unitarias se diseñan para validar componentes individuales de forma aislada, verificando el comportamiento interno de funciones, clases, métodos y componentes sin depender de servicios externos.

Su diseño considera principalmente:

- Cobertura de métodos y funciones.
- Escenarios de éxito y error.
- Validación de parámetros válidos e inválidos.
- Manejo de excepciones.
- Casos límite propios de la lógica implementada.
- Uso de mocks, stubs y dobles de prueba para aislar dependencias.

El objetivo es asegurar que cada unidad de código funcione correctamente antes de integrarse con el resto del sistema.

---

## 4.2 Diseño de Pruebas Funcionales

Las pruebas funcionales se diseñan tomando como base los requisitos funcionales y los casos de uso del sistema, evaluando el comportamiento observable desde la perspectiva del usuario.

Para el diseño de los casos de prueba se emplean técnicas de pruebas de caja negra, entre ellas:

- Partición por Equivalencia.
- Análisis de Valores Límite.
- Tablas de Decisión.
- Transición de Estados.
- Casos de Uso.
- Pruebas Basadas en Requisitos.

Cada caso de prueba funcional documenta:

- Funcionalidad evaluada.
- Requisito asociado.
- Precondiciones.
- Datos de entrada.
- Pasos de ejecución.
- Resultado esperado.
- Prioridad.

Estas pruebas permiten verificar que las funcionalidades implementadas cumplen con el comportamiento especificado.

---

## 4.3 Diseño de Pruebas de Integración

Las pruebas de integración se diseñan para validar la interacción entre módulos y componentes que colaboran durante la ejecución del sistema.

Su diseño se enfoca en identificar los puntos de comunicación entre capas, considerando aspectos como:

- Flujo de información entre componentes.
- Integración entre controladores, servicios y repositorios.
- Persistencia de datos.
- Comunicación mediante APIs REST.
- Contratos de intercambio de datos.
- Manejo de errores durante la integración.
- Consistencia de la información compartida entre módulos.

Cada caso de prueba reproduce un flujo de interacción real entre dos o más componentes del sistema.

---

## 4.4 Diseño de Pruebas de Sistema

Las pruebas de sistema se diseñan para validar el comportamiento del software completamente integrado en un entorno similar al de producción.

Su diseño contempla escenarios completos de operación, incluyendo:

- Flujos End-to-End (E2E).
- Procesos completos de negocio.
- Interacción entre frontend y backend.
- Integración con la base de datos.
- Compatibilidad entre navegadores.
- Escenarios de carga y rendimiento cuando corresponda.
- Validación de requisitos funcionales y no funcionales.

Los casos de prueba representan el recorrido completo que realiza un usuario durante la utilización del sistema, verificando que todos los componentes trabajen de forma conjunta para satisfacer los objetivos del negocio.

---

Independientemente del nivel de prueba, todos los casos diseñados mantienen una estructura común que facilita su documentación, ejecución y trazabilidad.

Cada caso de prueba incluye, como mínimo:

- Identificador único.
- Objetivo.
- Requisito asociado.
- Precondiciones.
- Datos de entrada.
- Procedimiento de ejecución.
- Resultado esperado.
- Prioridad.

---

# 5. Preparación del Entorno de Pruebas

Antes de iniciar la ejecución de las pruebas se configura un entorno controlado que reproduce las condiciones necesarias para validar correctamente el comportamiento del sistema.

Las actividades realizadas durante esta etapa incluyen:

- Configuración del backend.
- Configuración del frontend.
- Inicialización de la base de datos.
- Aplicación de migraciones.
- Carga de datos de prueba.
- Configuración de herramientas de automatización.
- Verificación de dependencias y servicios.

Cuando es necesario, se utilizan contenedores Docker para garantizar la reproducibilidad del entorno y facilitar la ejecución de pruebas en diferentes equipos de desarrollo e integración continua.

---

# 6. Pruebas Unitarias

Las pruebas unitarias constituyen el primer nivel de validación del software y tienen como objetivo comprobar el correcto funcionamiento de las unidades individuales de código de forma completamente aislada.

En esta etapa cada función, clase, componente o método es evaluado independientemente del resto del sistema, utilizando mocks, stubs o dobles de prueba cuando existen dependencias externas.

Las pruebas unitarias permiten validar:

- Lógica de negocio.
- Métodos y funciones individuales.
- Validaciones internas.
- Manejo de excepciones.
- Funciones auxiliares.
- Servicios y componentes del frontend.
- Clases de negocio del backend.

El principal beneficio de este nivel de prueba es detectar defectos de implementación en etapas tempranas del desarrollo, facilitando la corrección del código antes de su integración con otros módulos.

---

# 7. Pruebas Funcionales

Una vez comprobado el funcionamiento individual de los componentes, se realizan pruebas funcionales con el propósito de verificar que el sistema cumple los requisitos especificados desde la perspectiva del usuario.

Estas pruebas se centran en evaluar el comportamiento observable del software sin considerar su implementación interna, siguiendo un enfoque de pruebas de caja negra.

Para el diseño de los casos de prueba funcionales se aplican técnicas como:

- Partición por Equivalencia.
- Análisis de Valores Límite.
- Tablas de Decisión.
- Transición de Estados.
- Casos de Uso.

Cada caso de prueba describe las condiciones iniciales, los datos de entrada, las acciones que debe realizar el usuario y el resultado esperado, permitiendo comprobar que cada funcionalidad responde correctamente ante diferentes escenarios válidos e inválidos.

Las pruebas funcionales garantizan que los requisitos definidos durante el análisis del sistema han sido implementados correctamente y que la aplicación ofrece el comportamiento esperado para los usuarios finales.

---

# 8. Pruebas de Integración

Después de validar los componentes individuales y sus funcionalidades, se realizan pruebas de integración con el objetivo de verificar la correcta interacción entre los diferentes módulos del sistema.

En este nivel ya no se evalúan componentes aislados, sino el intercambio de información entre las distintas capas de la aplicación.

Las pruebas de integración verifican la comunicación entre:

- Controladores.
- Servicios.
- Managers.
- Repositorios.
- Base de datos.
- APIs REST.
- Componentes internos del sistema.

A diferencia de las pruebas unitarias, estas pruebas utilizan componentes reales, permitiendo validar el flujo completo de información y detectar problemas relacionados con interfaces, contratos de servicios, persistencia de datos o comunicación entre módulos.

Este nivel asegura que los distintos componentes del sistema colaboran correctamente para implementar los procesos de negocio definidos.

---

# 9. Pruebas de Sistema

Las pruebas de sistema representan el nivel final de validación técnica antes de la liberación del software.

Su objetivo es evaluar el comportamiento del sistema completamente integrado en un entorno que simula las condiciones reales de operación.

Durante esta etapa se verifican aspectos como:

- Flujos completos End-to-End (E2E).
- Cumplimiento de requisitos funcionales.
- Integración entre frontend y backend.
- Persistencia de información.
- Compatibilidad entre navegadores.
- Integración con servicios externos.
- Rendimiento y estabilidad bajo diferentes condiciones de carga.
- Experiencia general del usuario.

Las pruebas de sistema permiten validar que todos los componentes funcionan correctamente como una única aplicación y que el producto satisface los objetivos establecidos durante la definición de requisitos.

---

# 10. Gestión de Defectos

Cuando durante la ejecución de las pruebas se detecta un comportamiento diferente al esperado, el defecto es registrado y clasificado de acuerdo con su severidad y prioridad.

Cada incidencia documenta como mínimo:

- Identificador.
- Descripción del problema.
- Pasos para reproducirlo.
- Resultado esperado.
- Resultado obtenido.
- Evidencias.
- Componente afectado.
- Nivel de severidad.
- Estado del defecto.

Posteriormente, el equipo de desarrollo implementa las correcciones correspondientes y las incidencias permanecen bajo seguimiento hasta su resolución definitiva.

---

# 11. Pruebas de Regresión

Después de corregir defectos o incorporar nuevas funcionalidades se ejecutan pruebas de regresión para verificar que los cambios realizados no introducen errores en componentes previamente validados.

Las pruebas de regresión reutilizan casos de prueba existentes y, siempre que sea posible, son automatizadas para reducir los tiempos de ejecución y garantizar resultados consistentes durante cada iteración del desarrollo.

---

# 12. Documentación y Trazabilidad

Toda la información generada durante el proceso de pruebas es documentada con el fin de mantener la trazabilidad entre requisitos, casos de prueba, resultados obtenidos y defectos identificados.

La documentación incluye:

- Planes de prueba.
- Diseño de casos de prueba.
- Evidencias de ejecución.
- Registro de incidencias.
- Reportes de resultados.
- Métricas de cobertura.
- Informes finales de calidad.

La trazabilidad permite verificar que cada requisito del sistema ha sido evaluado mediante uno o más casos de prueba y facilita el seguimiento del proceso de aseguramiento de la calidad durante todo el ciclo de vida del proyecto.

---

# 13. Automatización y Mejora Continua

Con el objetivo de aumentar la eficiencia, repetibilidad y confiabilidad del proceso de pruebas, siempre que sea posible se emplean herramientas de automatización para la ejecución de las diferentes suites de prueba.

Las principales herramientas utilizadas son:

| Herramienta | Propósito |
|------------|-----------|
| JUnit 5 | Pruebas unitarias del backend |
| Mockito | Simulación de dependencias (Mocking) |
| Vitest | Pruebas unitarias del frontend |
| Playwright | Pruebas funcionales y End-to-End |
| Testcontainers | Pruebas de integración con servicios reales |
| Docker | Despliegue del entorno de pruebas |
| PostgreSQL | Base de datos para pruebas |
| GitHub Actions | Integración Continua y ejecución automática |

Finalmente, los resultados obtenidos en cada ciclo de pruebas son analizados para identificar oportunidades de mejora en los procesos, incrementar la cobertura de pruebas y fortalecer la calidad del software en futuras versiones del proyecto.