# Matriz de Pruebas y Evidencias — Control de Equipos de Laboratorio

**Asignatura:** Programación de Aplicaciones de Escritorio  
**Estudiante:** Moisés Abraham Valle Alonso  
**Base de Datos:** PostgreSQL (`bd_laboratorio_orm` / esquema `laboratorio`)  
**ORM:** Hibernate 7.0.4 (Jakarta Persistence) + JavaFX 21  

---

## 1. Matriz de Pruebas Obligatorias (P1 – P8)

| ID | Caso de Prueba | Datos de Entrada | Resultado Esperado | Resultado Obtenido | Estado | Evidencia |
| :--- | :--- | :--- | :--- | :--- | :---: | :--- |
| **P1** | Registrar un equipo válido | Código: `EQ-PC-004`<br>Nombre: `Monitor Dell UltraSharp 24`<br>Marca: `Dell`<br>Cantidad: `6`<br>Tipo: `Computadoras de Escritorio`<br>Estado: `Disponible` | Registro guardado en PostgreSQL mediante `persist()` y `commit()`, visible en el `TableView`. | Se mostró alerta de confirmación y el registro apareció correctamente en la tabla y en PostgreSQL. | ✅ Aprobado | ![P1](P1_registro_valido.png) |
| **P2** | Intentar código duplicado | Código: `EQ-PC-001` (ya existente)<br>Nombre: `PC Prueba Duplicado`<br>Marca: `HP`<br>Cantidad: `2`<br>Tipo: `Computadoras de Escritorio`<br>Estado: `Disponible` | Operación rechazada con alerta comprensible; sin registros duplicados en la BD. | El sistema bloqueó el registro mostrando la alerta de código duplicado y mantuvo intacta la tabla. | ✅ Aprobado | ![P2](P2_codigo_duplicado.png) |
| **P3** | Editar nombre, estado o tipo | Selección: `EQ-PC-004`<br>Cambio de Estado: `En mantenimiento`<br>Cambio de Cantidad: `5` | Cambios actualizados mediante `merge()` y `commit()`, persistidos en PostgreSQL. | Se mostró alerta de actualización exitosa y el `TableView` reflejó los nuevos valores. | ✅ Aprobado | ![P3](P3_actualizar_equipo.png) |
| **P4** | Eliminar un equipo con confirmación | Selección: `EQ-PC-004`<br>Acción: Clic en **Eliminar** y confirmar en `Alert.CONFIRMATION`. | El registro se elimina mediante `remove()` dentro de transacción y desaparece del listado y de PostgreSQL. | Tras confirmar el diálogo, el equipo fue eliminado de la interfaz y confirmado ausente mediante `SELECT` en PostgreSQL. | ✅ Aprobado | ![P4](P4_eliminar_equipo.png) |
| **P5** | Buscar por nombre parcial | Buscar nombre: `Think`<br>Filtrar por tipo: `Todos los tipos` | Solo se muestran los equipos cuyo nombre contiene `"Think"` (insensible a mayúsculas). | El `TableView` filtró únicamente `PC Lenovo ThinkCentre M70q` y `Laptop Lenovo ThinkPad T14 Gen 3`. | ✅ Aprobado | ![P5](P5_buscar_nombre.png) |
| **P6** | Filtrar por tipo y combinar con búsqueda | Buscar nombre: `Think`<br>Filtrar por tipo: `Laptops` | Solo se muestran los registros que cumplen ambas condiciones simultáneamente (`AND`). | Se mostró únicamente `Laptop Lenovo ThinkPad T14 Gen 3`. | ✅ Aprobado | ![P6](P6_filtro_combinado.png) |
| **P7** | Ingresar cantidad negativa | Código: `EQ-TEST-01`<br>Nombre: `Equipo Negativo`<br>Cantidad: `-3`<br>Tipo: `Laptops`<br>Estado: `Disponible` | Validación rechaza el dato antes de persistir y muestra alerta sin registrar en BD. | Se desplegó alerta de validación indicando que la cantidad debe ser `>= 0`. | ✅ Aprobado | ![P7](P7_cantidad_negativa.png) |
| **P8** | Reiniciar aplicación | Cerrar completamente la ventana JavaFX y volver a ejecutar `Launcher`. | Todos los datos previamente confirmados persisten al recargar desde PostgreSQL. | Al reiniciar la aplicación, todos los cambios confirmados se cargaron intactos desde PostgreSQL. | ✅ Aprobado | ![P8](P8_persistencia_reinicio.png) |

---

## 2. Demostración de Fallo de Integridad y Uso de `rollback()`

* **Escenario probado:** Intento de inserción de un equipo violando la restricción de integridad `UNIQUE` (`uq_equipo_codigo`) a nivel de PostgreSQL al ejecutar `em.persist(equipo)` y `tx.commit()`.
* **Comportamiento de la transacción:**
  1. `EquipoDAO.guardar()` inicia la transacción con `tx.begin()`.
  2. Al intentar confirmar con `tx.commit()`, PostgreSQL rechaza la operación por violación de clave única (`ConstraintViolationException`).
  3. El bloque `catch (RuntimeException ex)` detecta que `tx.isActive()` es `true` y ejecuta `tx.rollback()`, revirtiendo cualquier cambio pendiente en la sesión y asegurando que no queden registros parciales o inconsistentes en la base de datos.
  4. Finalmente, el bloque `finally` cierra el `EntityManager` (`em.close()`) y la interfaz muestra una alerta de error comprensible sin cerrarse.
* **Evidencia:**

![Prueba Rollback](P9_rollback_integridad.png)