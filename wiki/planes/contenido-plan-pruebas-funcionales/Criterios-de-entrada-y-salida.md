# Criterios de Entrada y Salida

**3.1 Criterios de Entrada (Condiciones previas para iniciar las pruebas)**
*   El código de la aplicación (alf.io) debe estar compilado y desplegado correctamente en el entorno de pruebas local o staging.
*   Las pruebas unitarias y de integración base deben pasar exitosamente en el entorno de CI (GitHub Actions).
*   El análisis estático de código no debe arrojar errores críticos que comprometan la estabilidad de la aplicación.
*   La base de datos de pruebas (PostgreSQL) debe estar inicializada con los esquemas actualizados.

**3.2 Criterios de Salida (Condiciones para considerar las pruebas funcionales terminadas)**
*   Se han ejecutado todos los casos de prueba funcionales diseñados y documentados en los issues correspondientes.
*   Se han reportado todos los defectos encontrados durante la ejecución.
*   No existen defectos con severidad "Crítica" o "Alta" abiertos sin resolver.
*   Los reportes de cobertura (si aplican a esta fase) muestran que los flujos críticos han sido evaluados.
