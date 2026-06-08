    
3. # **Resultados de ejecución** {#resultados-de-ejecución}

   1. ## **Detalles de ejecución** {#detalles-de-ejecución}

      ### **3.2.1. Solicitud de una cuenta de instructor** {#3.2.1.-solicitud-de-una-cuenta-de-instructor}

      **FN1-CP-001**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-001* | *Verificar que se agregue una solicitud de crear instructor con todos los campos válidos.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Solicitud enviada exitosamente”* |  | *“Solicitud enviada exitosamente”* |  |  |
| Evidencia |  |  |  |  |
| ![][image2] Se muestra que el envío del formulario es exitoso y el instructor está a la espera de ser aprobado para unirse a la plataforma teammates |  |  |  |  |

      

      **FN1-CP-002**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-003* | *Verificar que el campo nombre no admita cadenas vacías.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: Nombre inválido”* |  | *“Error: Nombre inválido”* |  |  |
| Evidencia |  |  |  |  |
| ![][image3]  ![][image4] La solicitud fue rechazada debido a que el nombre del instructor no cumplía la regla de tener por lo menos un caracter |  |  |  |  |

      **FN1-CP-003**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-003* | *Verificar que el campo nombre no admita más de 100 caracteres.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: Nombre mayor a 99 caracteres”* |  | *“Error: Nombre mayor a 99 caracteres”* |  |  |
| Evidencia |  |  |  |  |
| ![][image5] ![][image6] Se muestra que el programa válido que las cadenas no sean mayores a 100 caracteres |  |  |  |  |

      

      **FN1-CP-004**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-004* | *Verificar que el campo de Institución no admita cadenas vacías.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: Institución inválida”* |  | *“Error: Institución inválida”* |  |  |
| Evidencia |  |  |  |  |
| ![][image7] De primera vista notamos que la interfaz no permite envíar el formulario con la cadena vacía en el correo. Por lo tanto, el sistema cumple en no permitir ingresar cadenas vacías. |  |  |  |  |

      

      **FN1-CP-005**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-005* | *Verificar que el campo institución no admita más de 86 caracteres.* | *Manual* | *Exitoso/* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: Institución inválida mayor a 86 caracteres ”* |  | *“Error: Institución inválida mayor a 86 caracteres ”* |  |  |
| Evidencia |  |  |  |  |
| ![][image8] De primera vista notamos que la interfaz no permite envíar el formulario con la cadena de más de 86 caracteres en el correo.  |  |  |  |  |

      

      **FN1-CP-006**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-007* | *Verificar que el campo país no admita cadenas vacías.*  | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: País inválido”* |  | *“Error: País inválido”* |  |  |
| Evidencia |  |  |  |  |
| ![][image9] De primera vista notamos en la interfaz que el formulario para añadir un nuevo docente no permite cadenas vacías. |  |  |  |  |

      

      **FN1-CP-007**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-008* | *Verificar que el campo país no admita más de 40 caracteres.*  | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: País inválido mayor a 40 caracteres”* |  | *“Error: País inválido mayor a 40 caracteres”* |  |  |
| Evidencia |  |  |  |  |
| ![][image10] De primera vista notamos en la interfaz que el formulario para añadir un nuevo docente en el campo de país no permite cadenas de más de 40 caracteres. |  |  |  |  |

      

      **FN1-CP-008**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-09* | *Verificar que el campo correo no admita cadenas vacías.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: Correo inválido”* |  | *“Error: Correo inválido”* |  |  |
| Evidencia |  |  |  |  |
| ![][image11] De primera vista notamos en la interfaz que el formulario para añadir un nuevo docente no permite cadenas vacías. |  |  |  |  |

      

      **FN1-CP-009**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-010* | *Verificar que el campo correo electrónico no admita más de 254 caracteres.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Error: Correo inválido mayor a 254 caracteres”* |  | *“Error: Correo inválido mayor a 254 caracteres”* |  |  |
| Evidencia |  |  |  |  |
| ![][image12] De primera vista notamos en la interfaz que el formulario para añadir un nuevo docente en el campo de email no permite cadenas de más de 254 caracteres. |  |  |  |  |

      

      **FN1-CP-010**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN01-CP-010* | *Verificar que el campo de comentarios admita una cadena de 500 caracteres.* | *Manual* | *Exitos* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Solicitud enviada exitosamente”* |  | *“Solicitud enviada exitosamente”* |  |  |
| Evidencia |  |  |  |  |
| ![][image13] ![][image14] Se muestra en la captura que la entrada de comentarios no tiene un límite de caracteres por lo cual la solicitud de creación del instructor fue subida correctamente. |  |  |  |  |

      ### **3.2.2. Recuperación de enlaces de sesiones para estudiantes** {#3.2.2.-recuperación-de-enlaces-de-sesiones-para-estudiantes}

      **FN02-CP-001**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN02-CP-001* | *Verificar que el formulario admita un correo válido.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *"Los enlaces han sido enviados a su correo"* |  | *"Los enlaces han sido enviados a su correo"* |  |  |
| Evidencia |  |  |  |  |
| ![][image15] Se ve en la interfaz que al colocar un correo con número de caracteres válido y sintaxis válida envía la solicitud correctamente. |  |  |  |  |

      **FN02-CP-002**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN02-CP-002* | *Verificar que la interfaz no admita cadenas vacías.* | *Manual* | *Exitoso/ No exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *"Error: El correo debe tener formato válido"* |  | *"Error: El correo debe tener formato válido"* |  |  |
| Evidencia |  |  |  |  |
| ![][image16] Se ve en la interfaz que al ingresar una cadena vacía en la entrada esta no lo admite y muestra un error. |  |  |  |  |

      **FN02-CP-003**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN02-CP-003* | *Verificar que la interfaz no admita cadenas de más de 74 caracteres.* | *Manual* | *Exitoso/ No exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *"Error: El correo debe tener formato válido"* |  | *"Error: El correo debe tener formato válido"*  |  |  |
| Evidencia |  |  |  |  |
| ![][image17]  Se muestra en la interfaz que el formulario no admite cadenas de más de 74 caracteres. |  |  |  |  |

      **FN02-CP-004**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN02-CP-004* | *Verificar que el formato del correo sea válido.* | *Manual* | *Exitoso/ No exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *"Error: El correo debe tener formato válido"* |  | *"Error: El correo debe tener formato válido"*  |  |  |
| Evidencia |  |  |  |  |
| ![][image18] Se muestra en la interfaz que el formulario no admite correos sin el simbolo de @. |  |  |  |  |

      ### **3.2.3. Añadir múltiples instructores** {#3.2.3.-añadir-múltiples-instructores}

      **FN3-CP-001**

      

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN3-CP-001* | *Añadir múltiples instructores como administrador* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *"Instructores añadidos"* |  | *"Instructores añadidos"*  |  |  |
| Evidencia |  |  |  |  |
| ![][image19] Se muestra el ingreso de datos, a continuación, la ejecución. ![][image20] Finalmente: ![][image21]  |  |  |  |  |

      ### **3.2.4. Añadir un nuevo instructor individual** {#3.2.4.-añadir-un-nuevo-instructor-individual}

      **FN4-CP-001**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-001-*EJEC-001* | *Verifica que los datos ingresados con todos los campos válidos sean correctos.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Instructor agregado correctamente, muestra tabla Result*	  |  | *Instructor agregado correctamente, muestra tabla Result*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba. ![][image22] Presionamos el botón “Add Instructor”  ![][image23] Nos muestra que los datos se agregaron correctamente y esto se refleja en la tabla “Results”. |  |  |  |  |

      

      **FN4-CP-002**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-002-*EJEC-001*  | *Verifica que se muestre error cuando el campo nombre está vacío.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	  |  | *No ejecuta nada, no agrega instructor.*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba: ![][image24] Presionamos el botón “Add Instructor” ![][image24] Lo que nos muestra es la misma imagen ya que el sistema no permite que dejen campos vacíos al momento de ingresar los datos, por lo que, no se ha guardado estos datos de instructor. |  |  |  |  |

      

      **FN4-CP-003**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-003-*EJEC-001*  | *Verifica que se permita registrar con nombre de longitud mínima válida.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Instructor agregado correctamente, muestra tabla Result*	  |  | *Instructor agregado correctamente, muestra tabla Result*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba  ![][image25] Presionamos el botón “Add Instructor” ![][image26] Nos muestra que los datos se agregaron correctamente y esto se refleja en la tabla “Results”. |  |  |  |  |

      

      **FN4-CP-004**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-004-*EJEC-001*  | *Verifica que se muestre error cuando el campo correo está vacío.* | *Manual* | *Exitoso/ No exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	 |  | *No ejecuta nada, no agrega instructor.*	  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba  ![][image27] Presionamos el botón “Add Instructor” ![][image28] Lo que nos muestra es la misma imagen ya que el sistema no permite que dejen campos vacíos al momento de ingresar los datos, por lo que, no se ha guardado estos datos del instructor. |  |  |  |  |

      

      **FN4-CP-005**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-005-*EJEC-001*  | *Verifica que se muestre error cuando el formato del correo es válido con el mínimo número de caracteres.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Instructor agregado correctamente, muestra tabla Result*	  |  | *Instructor agregado correctamente, muestra tabla Result*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba  ![][image29] Presionamos el botón “Add Instructor” ![][image30] Nos muestra que los datos se agregaron correctamente y esto se refleja en la tabla “Results”.  |  |  |  |  |

      

      **FN4-CP-006**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-006-*EJEC-001*  | *Verifica que se muestre error cuando el campo institución está vacío.* | *Manual* | *Exitoso/* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	  |  | *No ejecuta nada, no agrega instructor.*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba  ![][image31] Presionamos el botón “Add Instructor” ![][image32] Lo que nos muestra es la misma imagen ya que el sistema no permite que dejen campos vacíos al momento de ingresar los datos, por lo que, no se ha guardado estos datos del instructor.  |  |  |  |  |

      

      **FN4-CP-007**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-007-*EJEC-001*  | *Verifica que se muestre un error general cuando todos los campos están vacíos.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	  |  | *No ejecuta nada, no agrega instructor.*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba, en este caso todos los campos son vacíos: ![][image33] Presionamos el botón “Add Instructor” ![][image34] No nos ejecuta nada ya que el sistema no permite que se agreguen nuevos instructores si los campos están vacíos. |  |  |  |  |

      

      **FN4-CP-008**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-008-*EJEC-001*  | *Verifica que no se agregue instructor si nombre y correo están vacíos.* | *Manual* | *Exitoso/ No exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	  |  | *No ejecuta nada, no agrega instructor.*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba, en este caso solo el campo de Institución tiene contenido, los demás están vacíos:  ![][image35] Presionamos el botón “Add Instructor” ![][image36] No ejecuta nada ya que el sistema no permite agregar datos con campos vacíos. |  |  |  |  |

      

      **FN4-CP-09**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-09-*EJEC-001*  | *Verifica que no se agregue instructor si nombre e institución están vacíos.* | *Manual* | *Exitoso/ No exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	  |  | *Aqui el obtenido* |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba, en este caso solo se prueba con el campo de Email: ![][image37] Presionamos el botón “Add Instructor” ![][image37] No ejecuta nada ya que el sistema no permite agregar datos con campos vacíos.  |  |  |  |  |

      

      **FN4-CP-010**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-010-*EJEC-001*  | *Verifica que no se agregue instructor si el correo está vacío.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *No ejecuta nada, no agrega instructor.*	  |  | *No ejecuta nada, no agrega instructor.*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba  ![][image38] Presionamos el botón “Add Instructor” ![][image38] No ejecuta nada ya que el sistema no permite agregar datos con campos vacíos. |  |  |  |  |

      

      **FN4-CP-011**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN4-CP-011-*EJEC-001*  | *Verifica que se agregue instructor si los campos contienen como valores el carácter espacio.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Instructor agregado correctamente, muestra tabla Result*	 |  | *Instructor agregado correctamente, muestra tabla Result*	 |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Adding a Single Instructor” e ingresamos los datos de entrada de la prueba, caracter espacio para todas las entradas: ![][image39] Presionamos el botón “Add Instructor” ![][image40] Como se puede observar se agrega el instructor aunque los campos estén vacíos ya que las entradas tienen la condición de que agreguen si los campos no están vacíos. |  |  |  |  |

      ### **Casos cubiertos para Funcionalidad de Añadir un nuevo instructor individual:**

| Total de casos | Ejecutados | Exitosos | No exitosos |
| :---- | :---- | :---- | :---- |
| 11 | 11 | 11 | 0 |

      ### 

      ### **3.2.5. Editar solicitud de cuenta** {#3.2.5.-editar-solicitud-de-cuenta}

      **FN5-CP-001**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-001-*EJEC-001*  | *Verifica que la actualización se realice correctamente con todos los campos válidos.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Account request was successfully updated.”*  |  | *“Account request was successfully updated.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Lucía Gómez" Institución: "UNSA" Correo: "lucia@unsa.edu.pe" Comentario: "Actualizar datos" ![][image43] Seleccionamos Guardar  ![][image44] El resultado es una ventana emergente que indica que los cambios se realizaron correctamente, el mensaje que muestra es:  “Account request was successfully updated.” |  |  |  |  |

      

      **FN5-CP-002**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-002-*EJEC-001*  | *Verifica que se muestre error si el campo nombre está vacío.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“" " is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.”*  |  | *“" " is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.”* |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "" Institución: "UNSA" Correo: "lucia@unsa.edu.pe" Comentario: "Actualizar datos" ![][image45] Seleccionamos Guardar  ![][image46] El resultado que se obtiene es la salida de una ventana emergente que indica que no se pueden dejar campos vacíos, es decir que es inválido, además muestra las condiciones para ese campo. El mensaje original es: “" " is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.” |  |  |  |  |

      

      **FN5-CP-003**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-003-*EJEC-001*  | *Verifica que se permita actualizar con nombre de longitud mínima válida.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Account request was successfully updated.”*  |  | *“Account request was successfully updated.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "L" Institución: "UNSA" Correo: "lucia@unsa.edu.pe" Comentario: "Actualizar datos" ![][image47] Seleccionamos Guardar  ![][image48] Lo que muestra es una ventana emergente que indica que los datos son válidos y se actualizó correctamente, el mensaje original es: “Account request was successfully updated.” |  |  |  |  |

      

      **FN5-CP-004**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-004-*EJEC-001*  | *Verifica que se muestre error si el nombre excede los 100 caracteres.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“"Juan PerezJuan Perez..." is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.”*  |  | *“"Juan PerezJuan Perez..." is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Juan PerezJuan Perez..." (más de 100 caracteres) Institución: "UNSA" Correo: "lucia@unsa.edu.pe"  ![][image49] Seleccionamos Guardar  ![][image50] Nos muestra una ventana emergente indicando cuales son las condiciones para el campo de nombre y no actualiza los datos del instructor. El mensaje es: “"Juan PerezJuan Perez..." is not acceptable to TEAMMATES as a/an person name because it is too long. The value of a/an person name should be no longer than 100 characters. It should not be empty.” |  |  |  |  |

      

      **FN5-CP-005**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-005-*EJEC-001*  | *Verifica que se muestre error si el campo institución está vacío.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“" " is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty. ”*  |  | *“" " is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty. ”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Lucía" Institución: "" Correo: "lucia@unsa.edu.pe" Comments: \- ![][image51] Seleccionamos Guardar  ![][image52] Lo que nos muestra es una ventana emergente que indica que no se acepta campos vacíos para el campo de institución. El mensaje que muestra es: “" " is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty. ” |  |  |  |  |

      

      **FN5-CP-006**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-006-*EJEC-001*  | *Verifica que se permita actualizar con institución de longitud mínima válida.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Account request was successfully updated.”*  |  | *“Account request was successfully updated.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Lucía" Institución: "U" Correo: "lucia@unsa.edu.pe" Comments: \- ![][image53] Seleccionamos Guardar  ![][image54] Lo que muestra es una ventana emergente que indica que los datos son válidos y se actualizó correctamente, el mensaje original es: “Account request was successfully updated.” |  |  |  |  |

      

      **FN5-CP-007**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-007-*EJEC-001*  | *Verifica que se muestre error si la institución excede los 128 caracteres.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“"UNSAUNSA..." is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty.”*  |  | *“"UNSAUNSA..." is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Lucía" Institución: (cadena de 129 caracteres) Correo: "lucia@unsa.edu.pe" ![][image55] Seleccionamos Guardar  ![][image56] Nos indica que lo que ingresamos para el campo de Institución no es válido por exceder el número de caracteres. El mesaje que muestra es: “"UNSAUNSA..." is not acceptable to TEAMMATES as a/an institute name because it is too long. The value of a/an institute name should be no longer than 128 characters. It should not be empty.”  |  |  |  |  |

      

      **FN5-CP-008**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN5-CP-008-EJEC-001* | *Verifica que se muestre error si el campo correo está vacío.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *The field 'email' is empty. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces.*  |  | *The field 'email' is empty. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces.*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Lucía" Institución: "UNSA" Correo: "" ![][image57] Seleccionamos Guardar  ![][image58] Nos muestra que lo que ingresamos en el campo de email no es válido en la ventana emergente. El mensaje original es: The field 'email' is empty. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces. |  |  |  |  |

      

      **FN5-CP-009**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-009-*EJEC-001*  | *Verifica que se permita actualizar con un correo válido de longitud corta.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“Account request was successfully updated.”*  |  | *“Account request was successfully updated.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: ![][image59] Seleccionamos Guardar ![][image60] Nos muestra una ventana emergente en la que indica que los nuevos atos se actualizaron correctamente, el mensaje original es:  “Account request was successfully updated.” |  |  |  |  |

      

      **FN5-CP-010**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN5-CP-010-*EJEC-001*  | *Verifica que se muestre error si el correo tiene formato inválido* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“"juanperez@gmail.com\#" is not acceptable to TEAMMATES as a/an email because it is not in the correct format. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces.”*  |  | *Aqui el “"juanperez@gmail.com\#" is not acceptable to TEAMMATES as a/an email because it is not in the correct format. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces.”*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Pending Account Requests”, seleccionamos la cuenta que queremos modificar los datos: ![][image41] Luego, presionamos el ícono de lápiz para editar los datos: ![][image42] Modificamos los datos con los valores: Nombre: "Lucía" Institución: "UNSA" Correo: "juanperez@gmail.com\#" ![][image61] Seleccionamos Guardar ![][image62] Nos muestra una ventana emergente que indica el error y las condicionales para ese campo, el mensaje original es: “"juanperez@gmail.com\#" is not acceptable to TEAMMATES as a/an email because it is not in the correct format. An email address contains some text followed by one '@' sign followed by some more text, and should end with a top level domain address like .com. It cannot be longer than 254 characters, cannot be empty and cannot contain spaces.” |  |  |  |  |

      ### **Casos cubiertos para Funcionalidad de Editar solicitud de cuenta:**

| Total de casos | Ejecutados | Exitosos | No exitosos |
| :---- | :---- | :---- | :---- |
| 10 | 10 | 10 | 0 |

### 

      ### **3.2.6. Búsqueda** {#3.2.6.-búsqueda}

      **FN6-CP-001**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| *FN6-CP-001-EJEC-001* | *Verifica que se muestren resultados coincidentes en las tablas según el valor ingresado.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Se muestran resultados coincidentes en tablas.*  |  | *Se muestran resultados coincidentes en tablas.*   |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Search” que se encuentra en el menú superior. ![][image63] Ingresar Dato de Usuario a buscar: Valor: "mezcurra" ![][image64] Seleccionar el botón de Search ![][image65] Nos muestra los resultados coincidentes en tablas, según donde la búsqueda esté relacionada. ![][image66] |  |  |  |  |

      

      **FN6-CP-002**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN6-CP-002-*EJEC-001*  | *Verifica que se realice la búsqueda y muestre coincidencias o “No results found”.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Se muestran resultados si hay coincidencias; si no, No results found.*  |  | *Se muestran resultados porque si hay coincidencias.*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Search” que se encuentra en el menú superior. ![][image63] Ingresar Dato de Usuario a buscar: Valor: "A" (1 carácter válido) Seleccionar el botón de Search ![][image67] En este caso si hay coincidencias y lo relaciona y muestra según la tabla a la que pertenecen. |  |  |  |  |

      **FN6-CP-003**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN6-CP-003-*EJEC-001*  | *Verifica que se permita la búsqueda con el máximo de caracteres válidos.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *Se realiza búsqueda si hay coincidencia, se muestra; si no, No results found.*  |  | *Se realiza búsqueda ,se muestra que no hay coincidencias ventana emergente de No results found.*  |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Search” que se encuentra en el menú superior. ![][image63] Ingresar Dato de Usuario a buscar: Valor: 100 caracteres (límite superior válido)	 Seleccionar el botón de Search ![][image68] Nos muestra que no existen resultados en una ventana emergente. |  |  |  |  |

      

      

      **FN6-CP-004**

| ID |  | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | :---- | ----- | :---- | :---- |
| FN6-CP-004-*EJEC-001*  |  | *Verifica que el campo de búsqueda se bloquee al alcanzar el límite de 100 caracteres.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  |  | Resultado obtenido |  |  |
| *No se permite seguir escribiendo. No se lanza mensaje, campo se bloquea al límite.*  |  |  | *No se permite seguir escribiendo. No se lanza mensaje, campo se bloquea al límite.*  |  |  |
| Evidencia |  |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Search” que se encuentra en el menú superior. ![][image63] Ingresar Dato de Usuario a buscar: Intentar ingresar más de 100 caracteres	 ![][image69] No se permite seguir escribiendo. No se lanza mensaje, campo se bloquea al límite. Además del contador de números en la parte inferior que indica que se llegó al límite. |  |  |  |  |  |

      **FN6-CP-005**

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN6-CP-005-*EJEC-001*  | *Verifica el comportamiento del sistema ante un campo de búsqueda vacío.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“The \[searchkey\] HTTP parameter is null.* |  | *“The \[searchkey\] HTTP parameter is null.* |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Search” que se encuentra en el menú superior. ![][image63] Ingresar Dato de Usuario a buscar: Valor: "" (vacío) Seleccionar el botón de Search ![][image70] Nos muestra una ventana emergente que indica que el valor de la búsqueda está vacío. |  |  |  |  |

      

      **FN6-CP-006**

      

| ID | Descripción | Tipo | Estado | Defectos |
| ----- | :---- | ----- | :---- | :---- |
| FN6-CP-006-*EJEC-001*  | *Verifica que no se muestren resultados si se ingresan solo símbolos sin coincidencias.* | *Manual* | *Exitoso* | *No se encontraron defectos* |
| Resultado esperado |  | Resultado obtenido |  |  |
| *“No results found.”*  |  | *“No results found.”* |  |  |
| Evidencia |  |  |  |  |
| Primero ingresamos con cuenta de administrador, luego nos dirigimos a la sección “Search” que se encuentra en el menú superior. ![][image63] Ingresar Dato de Usuario a buscar: Valor: "\#\#\#$$$%%%^^^" (solo símbolos sin significado textual) Seleccionar el botón de Search ![][image71] Nos muestra la ventana emergente de que no se encontraron resultados. |  |  |  |  |

      ### **Casos cubiertos para Funcionalidad de Búsqueda:**

| Total de casos | Ejecutados | Exitosos | No exitosos |
| :---- | :---- | :---- | :---- |
| 6 | 6 | 6 | 0 |
