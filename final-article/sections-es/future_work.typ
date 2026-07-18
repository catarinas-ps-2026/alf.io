= Trabajo Futuro <sec:future_work>

La estrategia de pruebas implementada para alf.io proporciona una base para la mejora continua y expansión. Se han identificado varias áreas para mejora futura que fortalecerían aún más el proceso de aseguramiento de calidad.

Las pruebas de rendimiento deberían expandirse para abordar los problemas de escalabilidad identificados bajo condiciones de carga extrema @k6performance2024. Las pruebas K6 actuales revelaron una tasa de error que excedió el objetivo con 1,600 usuarios concurrentes. El trabajo futuro debería investigar las causas raíz en los endpoints de reserva y confirmación, implementar mecanismos de rate limiting y optimizar consultas de base de datos para mejorar el throughput.

La fase de pruebas de aceptación debería expandirse para incluir validación completa de extremo a extremo de flujos de trabajo de usuario @playwright2024. Esto incluye pruebas de aceptación automatizadas que simulen recorridos reales de usuario desde el descubrimiento de eventos hasta la compra de entradas, check-in y reportes posteriores al evento. Tales pruebas proporcionarían mayor confianza en la preparación del sistema para producción.

Las pruebas de seguridad podrían mejorarse expandiendo la cobertura de fuzzing para incluir componentes adicionales del sistema @jazzer2023 e implementando pruebas de penetración enfocadas en seguridad. El análisis actual de SonarQube identificó vulnerabilidades que deberían abordarse, particularmente el problema BLOCKER relacionado con conflictos de nombres de métodos y los problemas CRITICAL que involucran llamadas transaccionales @sonarqube2024.

La cobertura de automatización de pruebas debería expandirse para incluir más escenarios funcionales que actualmente se ejecutan manualmente @myers2011. Convertir casos de prueba manuales en scripts automatizados mejoraría la eficiencia de pruebas de regresión y permitiría ciclos de liberación más rápidos @cicd2016. La prioridad debería darse a casos de prueba de alto impacto que cubran procesamiento de pagos, generación de entradas y operaciones de check-in.

La infraestructura de pruebas podría beneficiarse de la implementación de pruebas de regresión visual para detectar cambios no intencionados en la interfaz de usuario. Herramientas como Percy o Chromatic podrían integrarse en la suite de pruebas de Playwright para capturar y comparar capturas de pantalla entre versiones de navegadores y tamaños de pantalla.

El monitoreo y observabilidad deberían integrarse en el proceso de pruebas para proporcionar información en tiempo real sobre el comportamiento del sistema durante la ejecución de pruebas. Implementar tracing distribuido, recolección de métricas y agregación de logs permitiría un mejor diagnóstico de fallos en pruebas y problemas de rendimiento.

La documentación generada durante este proyecto, siguiendo estándares ISO/IEC/IEEE 29119, proporciona una plantilla para esfuerzos futuros de pruebas @iso29119. Esta documentación debería mantenerse y actualizarse a medida que el sistema evolucione, asegurando que los planes de pruebas, informes de ejecución y seguimiento de defectos permanezcan actuales y útiles para actividades continuas de aseguramiento de calidad.
