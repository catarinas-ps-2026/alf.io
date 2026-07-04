# Metodología

La metodología de pruebas adoptada para el proyecto **alf.io** se basa en las recomendaciones del estándar **ISO/IEC/IEEE 29119**, siguiendo un proceso sistemático que permite planificar, diseñar, ejecutar y documentar las pruebas de software de forma controlada y trazable. El objetivo es garantizar que los requisitos funcionales y no funcionales sean verificados mediante procedimientos repetibles y medibles.

La estrategia contempla la ejecución progresiva de los diferentes niveles de prueba, iniciando con pruebas unitarias, continuando con pruebas de integración y finalizando con pruebas de sistema, permitiendo detectar defectos en etapas tempranas y reducir el costo de corrección.

---

## Planificación de las Pruebas

Durante esta fase se definen el alcance, los objetivos, los elementos bajo prueba, los recursos necesarios, el cronograma y los criterios de entrada y salida para cada nivel de prueba.

Asimismo, se identifican los riesgos asociados al proceso de pruebas y se establecen las herramientas que serán utilizadas durante la ejecución.

**Actividades principales:**

- Definición del alcance de las pruebas.
- Identificación de requisitos funcionales y no funcionales.
- Selección de herramientas de automatización.
- Definición de criterios de aceptación.
- Elaboración del plan de pruebas.

---

## Diseño de las Pruebas

En esta etapa se diseñan los casos de prueba tomando como referencia los requisitos del sistema y los flujos de negocio identificados.

Dependiendo del tipo de prueba, se aplican técnicas de diseño adecuadas, entre ellas:

- Partición por Equivalencia.
- Análisis de Valores Límite.
- Tablas de Decisión.
- Transición de Estados.
- Casos de Uso.
- Pruebas basadas en Requisitos.

Cada caso de prueba especifica:

- Objetivo.
- Precondiciones.
- Datos de entrada.
- Pasos de ejecución.
- Resultado esperado.
- Prioridad.
- Requisito asociado.

---

## 5.3 Preparación del Entorno

Antes de ejecutar las pruebas se prepara un entorno controlado que replica las condiciones necesarias para validar el comportamiento del sistema.

Esta preparación incluye:

- Configuración del backend.
- Configuración del frontend.
- Inicialización de la base de datos.
- Carga de datos de prueba.
- Configuración de herramientas de automatización.
- Verificación de dependencias y servicios.

Cuando corresponde, el entorno utiliza contenedores Docker para garantizar la reproducibilidad de las pruebas.

---

## 5.4 Ejecución de las Pruebas

Los casos de prueba se ejecutan siguiendo el orden definido en la planificación.

Durante esta fase se registra:

- Resultado obtenido.
- Evidencias.
- Tiempo de ejecución.
- Defectos encontrados.
- Estado de cada caso de prueba (Aprobado, Fallido o Bloqueado).

Las pruebas automatizadas son ejecutadas mediante los frameworks correspondientes a cada nivel de prueba, mientras que aquellas que requieren validación de comportamiento visual o interacción del usuario pueden ejecutarse manualmente o mediante herramientas E2E.

---

## 5.5 Gestión de Defectos

Cuando un caso de prueba falla, el defecto es registrado y clasificado según su severidad y prioridad.

Para cada incidencia se documenta:

- Descripción del problema.
- Pasos para reproducirlo.
- Resultado esperado.
- Resultado obtenido.
- Evidencias.
- Componente afectado.
- Estado de resolución.

Una vez corregido el defecto, se ejecutan nuevamente los casos de prueba correspondientes para verificar la solución implementada.

---

## 5.6 Pruebas de Regresión

Después de aplicar correcciones o incorporar nuevas funcionalidades, se ejecutan pruebas de regresión con el fin de comprobar que los cambios realizados no introduzcan nuevos defectos en funcionalidades previamente validadas.

Las suites automatizadas permiten reducir el tiempo de ejecución y asegurar una validación consistente del sistema.

---

## 5.7 Documentación y Reporte

Al finalizar la ejecución de las pruebas se consolidan los resultados obtenidos en informes que incluyen:

- Casos ejecutados.
- Casos aprobados.
- Casos fallidos.
- Cobertura alcanzada.
- Defectos detectados.
- Estado general de calidad del sistema.
- Recomendaciones para futuras iteraciones.

Esta documentación sirve como evidencia del proceso de aseguramiento de calidad y facilita la trazabilidad entre requisitos, casos de prueba y resultados obtenidos.

---

## 5.8 Herramientas Utilizadas

Las principales herramientas empleadas durante el proceso de pruebas son:

| Herramienta | Propósito |
|-------------|-----------|
| JUnit 5 | Pruebas unitarias del backend |
| Mockito | Simulación de dependencias |
| Vitest | Pruebas unitarias del frontend |
| Playwright | Pruebas funcionales y E2E |
| Testcontainers | Entornos de integración con PostgreSQL |
| Docker | Despliegue del entorno de pruebas |
| PostgreSQL | Base de datos del sistema |
| GitHub Actions | Integración continua y ejecución automática de pruebas |