# Matriz de Pruebas y Evidencias — Control de Equipos de Laboratorio

**Asignatura:** Programación de Aplicaciones de Escritorio  
**Estudiante:** Moisés Abraham Valle Alonso  
**Base de Datos:** PostgreSQL (`bd_laboratorio_orm` / esquema `laboratorio`)  
**Tecnologías:** Java 21, JavaFX 21, Hibernate ORM 7.0.4 (Jakarta Persistence), PostgreSQL JDBC 42.7.13  

---

## 0. Estado Inicial de la Aplicación

Carga inicial de los tipos de equipo y del catálogo de 10 equipos persistidos en PostgreSQL al iniciar `Launcher`:

![Estado Inicial de la Aplicación](AppInicio.png)

---

## 1. Matriz de Pruebas Obligatorias (P1 – P8)

| ID | Caso de Prueba | Datos de Entrada | Resultado Esperado | Resultado Obtenido | Estado | Evidencia(s) |
| :--- | :--- | :--- | :--- | :--- | :---: | :--- |
| **P1** | Registrar un equipo válido | Código: `EQ-PC-004`<br>Nombre: `Monitor Dell UltraSharp 24`<br>Marca: `Dell`<br>Cantidad: `6`<br>Tipo: `Computadoras de Escritorio`<br>Estado: `Disponible` | Registro guardado en PostgreSQL mediante `persist()` y `commit()`, visible en el `TableView`. | Se ingresaron los datos en el formulario, se mostró la alerta de confirmación y el registro apareció en la tabla y en PostgreSQL. | ✅ Aprobado | ![P1-a](P1-a.png)<br>![P1-b](P1-b.png)<br>![P1-c](P1-c.png) |
| **P2** | Intentar código duplicado | Código: `EQ-PC-001` (ya existente)<br>Nombre: `PC Prueba Duplicado`<br>Marca: `HP`<br>Cantidad: `2`<br>Tipo: `Computadoras de Escritorio`<br>Estado: `Disponible` | Operación rechazada con alerta comprensible; sin registros duplicados en la BD. | El sistema bloqueó el registro mostrando la alerta de validación de código duplicado y mantuvo intacta la tabla. | ✅ Aprobado | ![P2](P2.png) |
| **P3** | Editar nombre, estado o tipo | Selección: `EQ-PC-004`<br>Modificación de atributos/estado/tipo y clic en **Actualizar**. | Cambios actualizados mediante `merge()` y `commit()`, persistidos en PostgreSQL. | Se seleccionó el equipo, se modificaron sus datos, se confirmó la actualización y el `TableView` reflejó los cambios. | ✅ Aprobado | ![P3-a](P3-a.png)<br>![P3-b](P3-b.png)<br>![P3-c](P3-c.png)<br>![P3-d](P3-d.png) |
| **P4** | Eliminar un equipo con confirmación | Selección: `EQ-PC-004`<br>Acción: Clic en **Eliminar** y confirmar en `Alert.CONFIRMATION`. | El registro se elimina mediante `remove()` dentro de transacción y desaparece del listado y de PostgreSQL. | Tras confirmar el diálogo de seguridad, el equipo fue eliminado de la interfaz y confirmado ausente en PostgreSQL. | ✅ Aprobado | ![P4-a](P4-a.png)<br>![P4-b](P4-b.png)<br>![P4-c](P4-c.png) |
| **P5** | Buscar por nombre parcial | Buscar nombre: `Think`<br>Filtrar por tipo: `Todos los tipos` | Solo se muestran los equipos cuyo nombre contiene `"Think"` (insensible a mayúsculas). | El `TableView` filtró únicamente los registros coincidentes con el fragmento ingresado. | ✅ Aprobado | ![P5](P5.png) |
| **P6** | Filtrar por tipo y combinar con búsqueda | Buscar nombre: `Think`<br>Filtrar por tipo: `Laptops` | Solo se muestran los registros que cumplen ambas condiciones simultáneamente (`AND`). | Se mostró únicamente el equipo que cumple tanto el filtro de texto como el tipo de equipo seleccionado. | ✅ Aprobado | ![P6](P6.png) |
| **P7** | Ingresar cantidad negativa | Código: `EQ-TEST-01`<br>Nombre: `Equipo Negativo`<br>Cantidad: `-3`<br>Tipo: `Laptops`<br>Estado: `Disponible` | Validación rechaza el dato antes de persistir y muestra alerta sin registrar en BD. | Se desplegó alerta de validación indicando que la cantidad debe ser un entero `>= 0`. | ✅ Aprobado | ![P7](P7.png) |
| **P8** | Reiniciar aplicación | Cerrar completamente la ventana JavaFX y volver a ejecutar `Launcher`. | Todos los datos previamente confirmados persisten al recargar desde PostgreSQL. | Al reiniciar la aplicación, todos los cambios confirmados se cargaron intactos desde PostgreSQL. | ✅ Aprobado | ![P8](P8.png) |

---

## 2. Demostración de Fallo de Integridad y Uso de `rollback()`

* **Escenario probado:** Intento de inserción directa de un equipo con código duplicado (`EQ-PC-001`) omitiendo temporalmente la validación preventiva en el controlador para forzar la violación de la restricción `UNIQUE` (`uq_equipo_codigo`) en PostgreSQL durante `tx.commit()`[cite: 4].
* **Comportamiento de la transacción en `EquipoDAO.guardar()`:**
  1. Se inicia la unidad de trabajo con `EntityManager em = JPAUtil.getEntityManager()` y se abre la transacción con `tx.begin()`[cite: 3].
  2. Se asocia la entidad administrada `TipoEquipo` y se invoca `em.persist(equipo)`[cite: 3].
  3. Al ejecutar `tx.commit()`, PostgreSQL rechaza la inserción por violación de clave única (`ConstraintViolationException` / `RollbackException`).
  4. El bloque `catch (RuntimeException ex)` verifica `if (tx.isActive()) { tx.rollback(); }`, revirtiendo la transacción por completo y asegurando que no queden registros parciales o inconsistentes en la base de datos[cite: 3, 4].
  5. El bloque `finally` cierra el `EntityManager` (`em.close()`) y la interfaz muestra una alerta descriptiva del error sin cerrarse[cite: 2, 4].

* **Evidencia:**

![Prueba Rollback Integridad](P9_rollback_integridad.png)