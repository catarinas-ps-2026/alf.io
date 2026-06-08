4. # **Casos de Prueba** {#casos-de-prueba}

   ## **4.1. Estructura de Casos de Prueba** {#4.1.-estructura-de-casos-de-prueba}

   Cada caso de prueba será descrito siguiendo una estructura estandarizada que facilita su comprensión, ejecución y trazabilidad. La estructura adoptada es la siguiente:  
- *ID*: Código identificador único del caso de prueba (e.g., CP-001).  
- *Funcionalidad:* Breve título que indique el propósito de la prueba.  
- *Descripción*: Explicación clara y concisa del objetivo del caso de prueba.  
- *Requisito Asociado*: Código o referencia del requerimiento funcional o no funcional asociado.  
- *Precondiciones*: Condiciones que deben cumplirse antes de ejecutar la prueba (estado del sistema, usuario autenticado, datos necesarios, etc.).  
- *Datos de Entrada*: Información o parámetros requeridos para ejecutar la prueba.  
- *Pasos de Ejecución*: Secuencia detallada de acciones que debe seguir el evaluador para llevar a cabo la prueba.  
- *Técnicas de Pruebas*: Técnica de caja negra utilizada para diseñar el caso.  
- *Prioridad:* Nivel de importancia del caso (Alta, Media, Baja) según impacto y probabilidad.

  ## **4.2. Desarrollo de los Casos de prueba** {#4.2.-desarrollo-de-los-casos-de-prueba}

  En esta sección se documentarán los casos de prueba específicos organizados por módulo, empleando la estructura definida. Cada conjunto de pruebas utilizará la técnica de caja negra más apropiada según la naturaleza del módulo y sus funcionalidades:


  ### **4.2.1. Solicitud de una cuenta de instructor** {#4.2.1.-solicitud-de-una-cuenta-de-instructor}

| ID | CPF-0001 |
| :---- | :---- |
| **Funcionalidad** | Solicitud de una cuenta de instructor |
| **Descripción** | *Formulario que solicita añadir a un solo instructor* |
| **Requisito Asociado** | RF-00 |
| **Precondiciones** | Acceder a la página sin estar autenticado. |
| **Datos de Entrada**  | Full Name  University/school/institution  Country  Official email address  Any other comments/queries |
| **Pasos de Ejecución** | Ingresar a la página principal Hacer click en el botón Request  Hacer click en botón “I’m Instructor” |
| **Técnicas de Pruebas** | Partición de equivalencia Valores Límites Tablas de Decisión Transición de estados |
| **Prioridad** | Alta |

  **Técnicas de pruebas implementadas**

  **Partición de equivalencia**

| Cod. | Campo | Clase Válida	 | Clases No Válidas |
| :---- | :---- | :---- | :---- |
| FN1-PE-001 | Nombre Completo	 | ≤ 100 caracteres, no vacío	 | \> 100 caracteres, vacío, no texto (solo símbolos) |
| FN1-PE-002 | Universidad/Institución | ≤ 86 caracteres, no vacío | \> 86 caracteres, vacío |
| FN1-PE-003 | País	 | ≤ 40 caracteres, no vacío | \> 40 caracteres, vacío |
| FN1-PE-004 | Correo Institucional	 | ≤ 254 caracteres, formato válido, dominio institucional | \> 254 caracteres, formato inválido, dominio público, vacío |
| FN1-PE-005 | Comentarios/Consultas | Cualquier valor (opcional) | \- |


  **Valores Límite**

| Cod. | Campo	 | Límite Inferior Válido	 | Límite Inferior No Válido	 | Límite Superior Válido	 | Límite Superior No Válido |
| :---- | :---- | :---- | :---- | :---- | :---- |
| FN1-VL-001 | Nombre Completo | 1 caracter	 | 0 caracteres	 | 100 caracteres	 | 101 caracteres |
| FN1-VL-002 | Universidad/Institución | 1 caracter	 | 0 caracteres	 | 86 caracteres	 | 87 caracteres |
| FN1-VL-003 | País	 | 1 caracter	 | 0 caracteres	 | 40 caracteres	 | 41 caracteres |
| FN1-VL-004 | Correo Institucional	 | 1 caracter válido	 | 0 caracteres	 | 254 caracteres válidos	 | 255 caracteres o inválido |


  **Tablas de Decisión**

| Cod.	 | Nombre Completo	 | Institución | País	 | Correo Inst.	 | Acción Sistema	 |
| :---- | :---- | :---- | :---- | :---- | :---- |
| FN1-TD-001 | válido | válido | válido | válido | “Success” |
| FN1-TD-002 | No válido | \- | \- | \- | “Error” |
| FN1-TD-003 | \- | No válido | \- | \- | “Error” |
| FN1-TD-004 | \- | \- | No válido | \- | “Error” |
| FN1-TD-005 | \- | \- | \- | No válido | “Error” |

    
  **Transición de estados**

		FN1-TS-001

![][image2]

**Catálogo de Pruebas**

| \#CP | Datos de Entrada | Resultado Esperado | Obs |
| :---- | :---- | :---- | :---- |
| FN01-CP-001 | Nombre: "Juan Perez", Inst: "UNSA", País: "Perú", Correo: "jperez@unsa.pe", Comentario: "N/A" | “Solicitud enviada exitosamente” | f+ |
| FN01-CP-002 | Nombre: "" (vacío), Inst: "UNSA", País: "Perú", Correo: "jperez@unsa.pe", Comentario: "N/A" | “Error: Nombre inválido” | f- |
| FN01-CP-003 | Nombre: (101 letras), Inst: "UNSA", País: "Perú", Correo: "jperez@unsa.pe", Comentario: "N/A" | “Error: Nombre mayor a 100 caracteres” | f- |
| FN01-CP-004 | Nombre: "Juan Perez", Inst: "" (vacío), País: "Perú", Correo: "jperez@unsa.pe", Comentario: "N/A" | “Error: Institución inválida” | f- |
| FN01-CP-005 | Nombre: "Juan Perez", Inst:(87 letras), País: “Perú”, Correo: "jperez@unsa.pe", Comentario: "N/A" | “Error: Institución inválida mayor a 86 caracteres ” | f- |
| FN01-CP-006 | Nombre: "Juan Perez", Inst: "UNSA", País:  "" (vacío), Correo: "jperez@unsa.pe", Comentario: "N/A" | “Error: País inválido” | f- |
| FN01-CP-007 | Nombre: "Juan Perez", Inst: "UNSA", País: (41 caracteres), Correo: "jperez@unsa.pe", Comentario: "N/A" | “Error: País inválido mayor a 40 caracteres” | f- |
| FN01-CP-008 | Nombre: "Juan Perez", Inst: "UNSA", País: "Perú", Correo:  "" (vacío), Comentario: "N/A" | “Error: Correo inválido” | f- |
| FN01-CP-009 | Nombre: "Juan Perez", Inst: "UNSA", País: "Perú", Correo:  (255 letras), Comentario: "N/A" | “Error: Correo inválido mayor a 254 caracteres”  | f- |
| FN01-CP-010 | Nombre: "Juan Perez", Inst: "UNSA", País: "Perú", Correo: "jperez@unsa.pe", Comentario: (500 letras) | “Solicitud enviada exitosamente” | f+ |

### **4.2.2. Recuperación de enlaces de sesiones para estudiantes** {#4.2.2.-recuperación-de-enlaces-de-sesiones-para-estudiantes}

| ID | CPF-0002 |
| :---- | :---- |
| **Funcionalidad** | Recuperación de enlaces de sesiones para estudiantes |
| **Descripción** | *Formulario para que un estudiante solicite el reenvío de los enlaces de sus sesiones de retroalimentación, ingresando su correo electrónico registrado.* |
| **Requisito Asociado** | RF-00 |
| **Precondiciones** | Acceder a la página sin estar autenticado. |
| **Datos de Entrada** | Correo Electrónico |
| **Pasos de Ejecución** | Ingresar a la página principal Hacer click en la opción de “Help” de la cabecera Hacer click en “Recover Session links” |
| **Técnicas de Pruebas** | Partición de equivalencia Valores Límite Tablas de Decisión Transición de estados |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

| Cod. | Campo | Clase Válida	 | Clases No Válidas |
| :---- | :---- | :---- | :---- |
| FN2-PE-001  | Correo electrónico	 | ≤ 74 caracteres, formato válido	 | \>74 caracteres, formato inválido, vacío |

**Valores límite**

| Cod. | Campo	 | Límite Inferior Válido	 | Límite Inferior No Válido	 | Límite Superior Válido	 | Límite Superior No Válido |
| :---- | :---- | :---- | :---- | :---- | :---- |
| FN2-VL-001 | Correo electrónico | 1 carácter	 | 0 caracteres	 | 74 caracteres  | 75 caracteres |

**Tablas de Decisión**

| Cod.	 | Tiene arroba “@” | Menor a 75 caracteres | Acción Sistema	 |
| :---- | :---- | :---- | :---- |
| FN2-TD-001 | Si | Sí | “Success” |
| FN2-TD-002 | No | \- | “Error” |
| FN2-TD-003 | \- | No | “Error” |

**Transición de estado**

FN2-TS-001

**Catálogo de Pruebas**

| \#CP | Códigos de regla | Correo de Entrada | Resultado Esperado | Obs |
| :---- | :---- | :---- | :---- | :---- |
| FN02-CP-001  |  | "jperez@unsa.pe" | "Los enlaces han sido enviados a su correo" | f+ |
| FN02-CP-002 |  | "" (vacío) | "Error: El correo debe tener formato válido" | f- |
| FN02-CP-003 |  | 75 caracteres	 | "Error: El correo debe tener formato válido" | f- |
| FN02-CP-004 |  | "jperez.unsa.pe" | "Error: El correo debe tener formato válido" | f- |

### **4.2.3. Añadir múltiples instructores** {#4.2.3.-añadir-múltiples-instructores}

| ID | CPF-0003 |
| :---- | :---- |
| **Funcionalidad** | Añadir múltiples instructores como administrador  |
| **Descripción** | *Formulario para añadir varios instructores, permitiendo ingresar los datos en un solo campo de texto, separados por el carácter “|” .* |
| **Requisito Asociado** | RF-00 |
| **Precondiciones** | Acceder a la página con el rol de administrador. |
| **Datos de Entrada** | Nombre | Correo | Institución |
| **Pasos de Ejecución** | Ingresar a la página principal |
| **Técnicas de Pruebas** | Partición de equivalencia Valores Límite Tablas de Decisión Transición de estados |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

| Cod. | Campo | Clase Válida	 | Clases No Válidas |
| :---- | :---- | :---- | :---- |
|  | Línea de entrada  | 3 partes separadas por “|”  | Número de partes separadas por “|” \!= 3 |

**Valores límite**

| Cod. | Campo	 | Límite Inferior Válido	 | Límite Inferior No Válido	 | Límite Superior Válido	 | Límite Superior No Válido |
| :---- | :---- | :---- | :---- | :---- | :---- |
|  | Nombre | 1 caracter | 0 caracteres | 50 caracter | 51 caracteres |
|  | Correo	 | 1 caracter | 0 caracteres | 60 caracteres | 61 caracteres |
|  | Correo	 | 1 caracter | 0 caracteres | 40 caracteres | 41 caracteres |

**Tabla de Decisión**

| Cod.	 | Nombre | Correo | Institución | Acción Sistema	 |
| :---- | :---- | :---- | :---- | :---- |
|  | válido | válido | válido | “Success” |
|  | No válido | \- | \- | “Error” |
|  | \- | No válido | \- | “Error” |
|  | \- | \- | No válido | “Error” |

**Transición de estado**

FN3-TS-001

**Catálogo de Pruebas**

| \#CP | Códigos de regla | Datos de Entrada | Resultado Esperado | Obs |
| :---- | :---- | :---- | :---- | :---- |
| FN3-CP-001 |  | "Juan Perez | juan@unsa.pe | UNSA" | "Instructores añadidos" | f+ |
| FN-3CP-002 |  | “juan@unsa.pe | UNSA” | "Error: Nombre inválido"	 | f- |
| FN3-CP-003 |  | “(51 letras) | juan@unsa.pe | UNSA" | "Error: Nombre inválido"	 | f- |
| FN3-CP-004 |  | "Juan Perez | | UNSA” | "Error: Correo inválido" | f- |
| FN-3CP-005 |  | "Juan Perez l (61 letras) l UNSA" | "Error: Correo inválido" | f- |
| FN3-CP-006 |  | "Juan Perez l juan@unsa.pe l " | "Error: Institución inválida" | f- |
| FN3-CP-007 |  | 	"Juan Perez l juan@unsa.pe l (41 letras)" | "Error: Institución inválida" | f- |
| FN3-CP-008 |  | " l c@inst.edu l Inst" | “Solicitud enviada exitosamente” | f- |
| FN3-CP-009 |  | "Juan Perez | juan@unsa.pe | UNSA Joel Antonio | joel@unsa.pe | UNSA" | "Instructores añadidos" | f+ |

### 	**4.2.4. Añadir un nuevo instructor individual**  {#4.2.4.-añadir-un-nuevo-instructor-individual}

| ID | CPF-0004 |
| :---- | :---- |
| **Funcionalidad** | Añadir un nuevo instructor individual |
| **Descripción** | Formulario que añadir a un solo instructor. |
| **Requisito Asociado** | RF-002.2 |
| **Precondiciones** | Acceder a la página estando autenticado y con rol de administrador. |
| **Datos de Entrada**  | Name  Official email address  University/institution  |
| **Pasos de Ejecución** | Ingresar a la página principal. Autenticarse como administrador.  Dirigirse a la sección “Adding a Single Instructor” Ingresar datos de Name, Email e  Institución Presionar botón “Add instructor” |
| **Técnicas de Pruebas** | Partición de equivalencia Valores Límites Tablas de Decisión Transición de estados |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

| Cod.  | Campo | Clase Válida	 | Clases No Válidas |
| :---- | :---- | :---- | :---- |
| FN4-PE-001 | Nombre	 | No vacío	  | Vacío |
| FN4-PE-002 | Correo Institucional | No vacío	 | Vacío |
| FN4-PE-003 | Universidad/Institución  | No vacío	 | Vacío |

**Valores Límites**

| Cod.  | Campo | Límite Inferior Válido | Límite Inferior No Válido	 | Observación |
| :---- | :---- | :---- | :---- | :---- |
| FN4-VL-001 | Nombre	 | 1 carácter	  | 0 caracteres	 | No debe estar vacío |
| FN4-VL-002  | Correo Institucional | 1 carácter	 | 0 caracteres	 | No debe estar vacío |
| FN4-VL-003  | Universidad/Institución  | 1 carácter	 | 0 caracteres	 | No debe estar vacío |

**Tablas de Decisión**

| Cod.  | Nombre	 | Correo Inst.	 | Universidad/Institución 	 | Acción Sistema	 |
| :---- | :---- | :---- | :---- | :---- |
| FN4-TD-001	 | Válido | Válido | Válido | Agrega, muestra tabla Result para añadir el instructor. |
| FN4-TD-002	 | Válido | \- | \- | No agrega |
| FN4-TD-003	 | Válido | Válido | \- | No agrega |
| FN4-TD-004	 | \- | \- | \- | No agrega |
| FN4-TD-005	 | \- | \- | Válido | No agrega |
| FN4-TD-006	 | \- | Válido | \- | No agrega |
| FN4-TD-007	 | Válido | \- | Válido | No agrega |
| FN4-TD-008	 | \- | Válido | Válido | No agrega |

**Transición de estados**

FN4-TS-001

**![][image3]**

**Catálogo de Pruebas**

| \#CP | Códigos de regla | Datos de Entrada | Resultado Esperado | Obs |
| :---- | :---- | :---- | :---- | :---- |
| FN4-CP-001 | FN4-PE-002, FN4-PE-003, FN4-PE-004, FN4-TD-001, FN4-TS-002, FN4-VL-003, FN4-VL-002, FN4-VL-001, FN4-TS-001	 | Nombre: "Ana Torres" Correo: "ana@unsa.edu.pe" Institución: "UNSA" | Instructor agregado correctamente, muestra tabla Result	 | f+ |
| FN4-CP-002 | FN4-PE-002, FN4-VL-001, FN4-TD-008, FN4-TS-003, FN4-VL-003, FN4-VL-002, FN4-VL-001, FN4-TS-001,  | Nombre: "" Correo: "ana@unsa.edu.pe" Institución: "UNSA” | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-003 | FN4-VL-001 FN4-VL-003, FN4-VL-002, FN4-TS-001, 	 | Nombre: "A" Correo: "ana@unsa.edu.pe" Institución: "UNSA" | Instructor agregado correctamente, muestra tabla Result	 | f+ |
| FN4-CP-004 | FN4-PE-003, FN4-TD-002,  FN4-VL-003, FN4-VL-002, FN4-VL-001, FN4-TS-001, 	 | Nombre: "Ana Torres" Correo: "" Institución: "UNSA" | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-005 | FN4-VL-003, FN4-VL-002, FN4-VL-001 FN4-TS-001,  | Nombre: "Ana Torres" Correo: "a" Institución: "UNSA" | Instructor agregado correctamente, muestra tabla Result	 | f+ |
| FN4-CP-006 | FN4-PE-004, FN4-VL-003, FN4-VL-002, FN4-VL-001 FN4-TD-003, FN4-TS-001	 | Nombre: "Ana Torres" Correo: "ana@unsa.edu.pe" Institución: "" | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-007 | FN4-TD-004, FN4-TS-001, FN4-VL-003, FN4-VL-002, FN4-VL-001		 | Nombre: "" Correo: "" Institución: "" | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-008 | FN4-TD-005, FN4-VL-003, FN4-VL-002, FN4-VL-001 FN4-TS-001,  | Nombre: "" Correo: "" Institución: "UNSA" | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-09 | FN4-TD-006, FN4-VL-003, FN4-VL-002, FN4-VL-001, FN4-TS-001,  | Nombre: "" Correo: "ana@unsa.edu.pe" Institución: "" | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-010 | FN4-TD-007, FN4-VL-003, FN4-VL-002, FN4-VL-001 FN4-TS-001,  | Nombre: "Ana Torres" Correo: "" Institución: "UNSA" | No ejecuta nada, no agrega instructor.	 | f- |
| FN4-CP-011 | FN4-PE-002 FN4-PE-003 FN4-PE-004 FN4-VL-001 FN4-VL-002 FN4-VL-003 FN4-TD-001 | Nombre: " "   (carácter espacio) Correo: " "  (carácter espacio) Institución: " " (carácter espacio)  | Instructor agregado correctamente, muestra tabla Result	  | f+ |

### 	**4.2.5. Editar solicitud de cuenta** {#4.2.5.-editar-solicitud-de-cuenta}

| ID | CPF-0005 |
| :---- | :---- |
| **Funcionalidad** | Editar solicitud de cuenta |
| **Descripción** | Modificar datos de cuentas que se encuentran en Solicitud de Cuentas pendientes. |
| **Requisito Asociado** | RF-002.3 |
| **Precondiciones** | Acceder a la página estando autenticado y con rol de administrador. Además, la cuenta de instructor a modificar debe estar en estado pendiente. |
| **Datos de Entrada**  | Full Name  University/school/institution  Country  Official email address  Any other comments/queries |
| **Pasos de Ejecución** | Ingresar a la página principal. Autenticarse como administrador.  Dirigirse a la sección “Pending Account Requests” Seleccionar la  opción de editar (ícono de lápiz) Modificar Full Name, University/school/institution, Official email address o Any other comments/queries Seleccionar el botón de Guardar o Cancelar |
| **Técnicas de Pruebas** | Partición de equivalencia Valores Límites Tablas de Decisión Transición de estados |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

| Cod. | Campo | Clase Válida	 | Clases No Válidas |
| :---- | :---- | :---- | :---- |
| FN5-PE-001 | Nombre Completo	 | \< 100 caracteres, no vacío	 | ≥100 caracteres, vacío, no texto (solo símbolos) |
| FN5-PE-002 | Universidad/Institución | \< 128 caracteres, no vacío | ≥128 caracteres, vacío |
| FN5-PE-003 | Correo Institucional	 | \< 254 caracteres, formato válido, dominio institucional | ≥254 caracteres, formato inválido, dominio público, vacío |
| FN5-PE-004 | Comentarios/Consultas | Cualquier valor (opcional) | \- |

**Valores Límites**

| Cod. | Campo	 | Límite Inferior Válido	 | Límite Inferior No Válido	 | Límite Superior Válido	 | Límite Superior No Válido |
| :---- | :---- | :---- | :---- | :---- | :---- |
| FN5-VL-001  | Nombre Completo | 1 caracter	 | 0 caracteres	 | 99 caracteres	 | 100 caracteres |
| FN5-VL-002  | Universidad/Institución | 1 caracter	 | 0 caracteres	 | 85 caracteres	 | 86 caracteres |
| FN5-VL-003  | Correo Institucional	 | 1 caracter válido	 | 0 caracteres	 | 253 caracteres válidos	 | 254 caracteres o inválido |

**Tablas de Decisión**

| Cod.	 | Nombre Completo	 | Institución | Correo Inst.	 | Acción Sistema	 |
| :---- | :---- | :---- | :---- | :---- |
| FN5-TD-001 | válido | válido | válido | “Success” |
| FN5-TD-002 | No válido | \- | \- | “Error” |
| FN5-TD-003 | \- | No válido | \- | “Error” |
| FN5-TD-004 | \- | \- | \- | “Error” |
| FN5-TD-005 | \- | \- | No válido | “Error” |

**Transición de estados**

FN5-TS-001

![][image4]

**Catálogo de Pruebas**

| \#CP | Códigos de regla | Datos de Entrada | Resultado Esperado | Obs |
| :---- | :---- | :---- | :---- | :---- |
| FN5-CP-001 | FN5-PE-001, FN5-PE-002, FN5-PE-003, FN5-TD-001, FN5-TS-001	 | Nombre: "Lucía Gómez" Institución: "UNSA" Correo: "lucia@unsa.edu.pe" Comentario: "Actualizar datos" | “Account request was successfully updated.” | f+ |
| FN5-CP-002 | FN5-PE-001, FN5-TD-002, FN5-VL-001, FN5-TD-001 | Nombre: "" Institución: "UNSA" Correo: "[lucia@unsa.edu.pe](mailto:lucia@unsa.edu.pe)" Comentario: "Actualizar datos"  | “" " is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.” | f- |
| FN5-CP-003 | FN5-VL-001, FN5-TS-001	 | Nombre: "L" Institución: "UNSA" Correo: "[lucia@unsa.edu.pe](mailto:lucia@unsa.edu.pe)" Comentario: "Actualizar datos" | “Account request was successfully updated.” | f+ |
| FN5-CP-004 | FN5-VL-001, FN5-TS-001 | Nombre: "Juan PerezJuan Perez..." (más de 100 caracteres) Institución: "UNSA" Correo: "lucia@unsa.edu.pe"  | “"Juan PerezJuan Perez..." is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.” | f- |
| FN5-CP-005 | FN5-PE-002, FN5-TD-003, FN5-VL-002, FN5-TS-001 | Nombre: "Lucía" Institución: "" Correo: "[lucia@unsa.edu.pe](mailto:lucia@unsa.edu.pe)" Comments: \-  | “" " is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty. ” | f- |
| FN5-CP-006 | FN5-V-L002, FN5-TS-001 | Nombre: "Lucía" Institución: "U" Correo: "lucia@unsa.edu.pe" | “Account request was successfully updated.” | f+ |
| FN5-CP-007 | FN5-VL-002, FN5-TS-001	 | Nombre: "Lucía" Institución: (cadena de 129 caracteres) Correo: "lucia@unsa.edu.pe" | “"UNSAUNSA..." is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty.” | f- |
| FN5-CP-008 | FN5-PE-003, FN5-TD-005, FN5-VL-003, FN5-TS-001 | Nombre: "Lucía" Institución: "UNSA" Correo: "" | The field 'email' is empty. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces. | f- |
| FN5-CP-009 | FN5-VL-003, FN5-TS-001  | Nombre: "Lucía" Institución: "UNSA" Correo: "l@u.pe" Click: Guardar | “Account request was successfully updated.” | f+ |
| FN5-CP-010 | FN5-TS-001, FN5- PE-003	 | Nombre: "Lucía" Institución: "UNSA" Correo: "juanperez@gmail.com\#" | “"juanperez@gmail.com\#" is not acceptable to TEAMMATES as a/an email because it is not in the correct format. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces.” | f- |

### **4.2.6. Búsqueda** {#4.2.6.-búsqueda}

| ID | CPF-0006 |
| :---- | :---- |
| **Funcionalidad** | Búsqueda (Admin Search) |
| **Descripción** | Formulario que realiza búsquedas en modo administrador. |
| **Requisito Asociado** | RF-006.1 |
| **Precondiciones** | Acceder a la página estando autenticado y con rol de administrador. |
| **Datos de Entrada**  | Valor (Puede ser Relacionado a instructor: Course	Name, Google ID, Institute o Relacionado a Estudiantes: Course \[Section\] (Team), Name, GoogleID, Institute, Comments) |
| **Pasos de Ejecución** | Ingresar a la página principal. Autenticarse como administrador.  Dirigirse a la sección “Search” que se encuentra en el menú superior. Ingresar Dato de Usuario a buscar. Seleccionar el botón de Search |
| **Técnicas de Pruebas** | Partición de equivalencia Valores Límites Tablas de Decisión Transición de estados |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

| Cod. | Campo | Clase Válida	 | Clases No Válidas |
| :---- | :---- | :---- | :---- |
| FN6-PE-001 | Valor | ≤ 100 caracteres, puede incluir letras, números y símbolos (-, .) | \> 100 caracteres, vacío, solo símbolos sin texto significativo |

**Valores Límites**

| Cod. | Campo	 | Límite Inferior Válido	 | Límite Inferior No Válido	 | Límite Superior Válido	 | Límite Superior No Válido |
| :---- | :---- | :---- | :---- | :---- | :---- |
| FN6-VL-001  | Valor	 | 1 carácter	 | 0 caracteres	 | 100 caracteres	 | 101 caracteres |

**Tablas de Decisión**

| Cod.	 | Valor | Acción Sistema	 |
| :---- | :---- | :---- |
| FN6-TD-001  | válido | “Success” |
| FN6-TD-002  | No válido | “Error” |

**Transición de estados**

FN6-TS-001

**![][image5]**

**Catálogo de Pruebas**

| \#CP | Códigos de regla | Datos de Entrada | Resultado Esperado | Obs |
| :---- | :---- | :---- | :---- | :---- |
| FN6-CP-001 | FN6-PE-001, FN6-TD-001, FN6-TS-001	 | Valor: "mezcurra" | Se muestran resultados coincidentes en tablas (Instructors Found o Students Found) | f+ |
| FN6-CP-002 | FN6-VL-001,  FN6-TS-001	 | Valor: "A" (1 carácter válido) | Se muestran resultados si hay coincidencias; si no, No results found. | f-/f+ |
| FN6-CP-003 | FN6-VL001, FN6-TS-001	 | Valor: 100 caracteres (límite superior válido)	 | Se realiza búsqueda si hay coincidencia, se muestra; si no, No results found. | f-/f+  |
| FN6-CP-004 | FN6-VL-001, FN6- TS-001	 | Intentar ingresar más de 100 caracteres	 | No se permite seguir escribiendo. No se lanza mensaje, campo se bloquea al límite. | f- |
| FN6-CP-005 | FN6-PE-001, FN6-TD-002, FN6-TS-001	 | Valor: "" (vacío)  | “The \[searchkey\] HTTP parameter is null. ” | f- |
| FN6-CP-006 | FN6-PE001, FN6-TS-001	 | Valor: "\#\#\#$$$%%%^^^" (solo símbolos sin significado textual) | “No results found.” | f- |

### **4.2.7. Añadir nueva notificación** {#4.2.7.-añadir-nueva-notificación}

| ID | CPF-0007 |
| :---- | :---- |
| **Funcionalidad** | Añadir nueva notificación  |
| **Descripción** | Formulario para crear y programar notificaciones dirigidas a grupos de usuarios |
| **Requisito Asociado** | RF-003.1 |
| **Precondiciones** | Acceder a la página con una cuenta de administrador |
| **Datos de Entrada**  | **Target user group** (Students, Teachers, etc.) **Notification style** (Success/green, Warning, Error, etc.) **Title** (máximo 80 caracteres) **Message content** **Timezone** Zona horaria  **Notification start time** **Notification end time** |
| **Pasos de Ejecución** | 1\. Acceder al formulario de creación de notificaciones 2\. Seleccionar grupo objetivo y estilo de notificación 3\. Completar título y contenido del mensaje 4\. Configurar fechas y horarios de vigencia 5\. Hacer clic en "Create Notification" |
| **Técnicas de Pruebas** | Partición de equivalencia Valores límite Tablas de decisión |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

| Cod. Campo | Campo | Clases Válidas | Clases No Válidas |
| ----- | ----- | ----- | ----- |
| FN7-PE-01 | Title | \- 1 a 80 caracteres- No vacío- No solo espacios | \- ≥81 caracteres- Vacío ("")- Solo espacios (" ") |
| FN7-PE-02 | Message content | \- Cualquier texto no vacío, incluyendo solo espacios | \- Vacío ("") |
| FN7-PE-03 | Notification end time | \- Fecha posterior al inicio- Mismo día con hora mayor | \- Mismo día con hora igual o menor |

**Valores Límite**

| Cod. | Campo | Límite Inferior Válido | Límite Inferior No Válido | Límite Superior Válido | Límite Superior No Válido |
| ----- | ----- | ----- | ----- | ----- | ----- |
| FN7-VL-01 | Title | 1 carácter | 0 caracteres (vacío) | 80 caracteres | 81 caracteres |
| FN7-VL-02 | Message content | 1 carácter | 0 caracteres (vacío) | \- | \- |
| FN7-VL-03 | Notification end time | Fecha inicio \+ 1 h | Fecha inicio (igual) | \- | \- |

**Tablas de Decisión**

| Condición | Caso 1 | Caso 2 | Caso 3 | Caso 4 |
| ----- | :---: | :---: | :---: | :---: |
| Grupo usuario válido | ✓ | ✓ | ✗ | ✓ |
| Título válido | ✓ | ✗ | ✓ | ✓ |
| Fechas válidas | ✓ | ✓ | ✓ | ✗ |
| **Resultado** | **Crear** | **Error** | **Error** | **Error** |

**Catálogo de Pruebas**

| \#CP | Códigos de Regla | Datos de Entrada | Resultado Esperado | Obs |
| ----- | ----- | ----- | ----- | ----- |
| FN7-CP-01 | FN7-PE-01, FN7-PE-02, FN7-PE-03, FN7-VL-01, FN7-VL-02, FN7-VL-03, TD | Title: "A"Message: "Hola" End time: inicio \+ 1 hora Grupo: "Students" | Crear | Todos los datos válidos, prueba positiva mínima |
| FN7-CP-02 | FN7-PE-01, FN7-VL-01 | Title: "" Message: "Mensaje válido" End time: inicio \+ 1 hora Grupo: "Teachers" | Error | Límite inferior no válido en Title |
| FN7-CP-03 | FN7-PE-02, FN7-VL-02 | Title: "Título válido"Message: ""End time: inicio \+ 1 hora Grupo: "General" | Error | Contenido del mensaje vacío |
| FN7-CP-04 | FN7-PE-03 | Title: "Notificación" Message: "Contenido cualquiera" End time: misma fecha, hora igual o menor que inició Grupo: "Students" | Error | Fecha de fin inválida (regla horaria en mismo día) |
| FN7-CP-05 | FN7-VL-01 (sup), FN7-VL-03 | Title: 81 caracteres Message: "Contenido" End time: inicio \+ 1 hora Grupo: "Students" | Error | Límite superior no válido en Title |

### **4.2.8. Editar una notificación** {#4.2.8.-editar-una-notificación}

| ID | CPF-0008 |
| :---- | :---- |
| **Funcionalidad** | Editar notificación  |
| **Descripción** | Formulario para editar notificaciones dirigidas a grupos de usuarios |
| **Requisito Asociado** | RF-003.2 |
| **Precondiciones** | Acceder a la página con una cuenta de administrador |
| **Datos de Entrada**  | **Target user group** (Students, Teachers, etc.) **Notification style** (Success/green, Warning, Error, etc.) **Title** (máximo 80 caracteres) **Message content** **Timezone** Zona horaria (automatico) **Notification start time** **Notification end time** |
| **Pasos de Ejecución** | 1\. Acceder al formulario de edición de la notificación que se desea editar 2\. Editar los campos que se deseen  3\. Hacer clic en "Save Changes" |
| **Técnicas de Pruebas** | Partición de equivalencia Valores límite Tablas de decisión |
| **Prioridad** | Alta |

**Técnicas de pruebas implementadas**

**Partición de equivalencia**

**Valores Límite** 

**Tablas de Decisión** 

**Catálogo de Pruebas**

| \#CP | Códigos de Regla | Datos de Entrada | Resultado Esperado | Observaciones |
| ----- | ----- | ----- | ----- | ----- |
| FN8-CP-01 | PE-01, PE-02, PE-03, VL-01, VL-02, VL-03, TD | Editar título a "A" Editar mensaje a "Hola" End time: inicio \+ 1 hora Grupo: Students | Guardado exitosamente | Prueba positiva mínima – Todos los campos válidos |
| FN8-CP-02 | PE-01, VL-01 (inválido) | Título: "" (vacío) Mensaje: "Mensaje válido" End time: inicio \+ 1 hora Grupo: Teachers | Error | Campo Title vacío – límite inferior inválido |
| FN8-CP-03 | PE-02, VL-02 (inválido) | Título: "Título válido" Mensaje: "" (vacío) End time: inicio \+ 1 hora Grupo: General | Error | Contenido del mensaje vacío |
| FN8-CP-04 | PE-03 | Título: "Notificación" Mensaje: "Contenido cualquiera" End time: misma fecha, hora igual o menor que inició | Error | Fecha de fin inválida según regla horaria |
| FN8-CP-05 | VL-01 (sup), VL-03 | Título: *(81 caracteres)* Mensaje: "Contenido" End time: inicio \+ 1 hora Grupo: Students | Error | Título excede el límite de 80 caracteres |
| FN8-CP-06 | PE-01 | Solo se edita el título: "Nuevo título válido" | Guardado exitosamente | Edición parcial de un solo campo |
| FN8-CP-07 | PE-01, VL-01 (inválido) | Borrar campo Title completamente (vacío) | Error | Campo obligatorio eliminado durante la edición |
| FN8-CP-08 | PE-03 | Editar solo el end time a una fecha anterior a la inicial | Error | Sigue aplicando la validación de rango de fechas |
| FN8-CP-09 | — | No se modifica ningún campo, solo clic en “Save Changes” | Guardado sin cambios | Sistema debe permitir guardar sin error o mostrar mensaje sin cambios |
| FN8-CP-10 | VL-01, VL-02 | Título y mensaje en límite superior válido (80 caracteres en Title, mensaje largo) Otros campos sin cambios | Guardado exitosamente | Prueba de límite superior válido durante edición |
