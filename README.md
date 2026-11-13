# Control de Equipos de Laboratorio — JavaFX + Hibernate ORM + PostgreSQL

Aplicación de escritorio desarrollada en **Java 21** con **JavaFX 21**, **Hibernate ORM 7** (Jakarta Persistence) y **PostgreSQL** para administrar el catálogo de equipos de un laboratorio universitario y su clasificación por tipo de equipo.

**Asignatura:** Programación de Aplicaciones de Escritorio  
**Evaluación:** Laboratorio Evaluado #1 — Corte III  
**Estudiante:** Moisés Abraham Valle Alonso  

---

## 1. Requisitos de Ejecución

* **JDK:** Java 21 o superior (ej. Microsoft OpenJDK 21 / Temurin 21).
* **Gestor de dependencias:** Apache Maven 3.8+ (incluido mediante Maven Wrapper `./mvnw` / `mvnw.cmd`).
* **Base de datos:** PostgreSQL 14 o superior ejecutándose en `localhost:5432`.
* **IDE recomendado:** IntelliJ IDEA.

---

## 2. Configuración de PostgreSQL

1. Ubique el script reproducible en [`sql/bd_laboratorio_orm.sql`](sql/bd_laboratorio_orm.sql).
2. Conectado al servidor PostgreSQL (desde **pgAdmin 4** o `psql`), ejecute primero la creación de la base de datos:
   ```sql
   CREATE DATABASE bd_laboratorio_orm;
   ```
3. Conéctese a la base de datos **`bd_laboratorio_orm`** y ejecute el resto del script (`sql/bd_laboratorio_orm.sql`), el cual crea:
   * El esquema **`laboratorio`**.
   * Las tablas **`laboratorio.tipo_equipo`** y **`laboratorio.equipo`** con sus llaves primarias (`IDENTITY`), llave foránea (`ON DELETE RESTRICT`), restricción `UNIQUE` en `codigo` y `nombre`, y restricciones `CHECK` para `cantidad >= 0` y `estado IN ('Disponible', 'En mantenimiento', 'Fuera de servicio')`.
   * Datos iniciales: **4 tipos de equipo** y **10 equipos** distribuidos con distintos estados.

---

## 3. Configuración de Credenciales y Ejecución

Por seguridad, el archivo `src/main/resources/META-INF/persistence.xml` no almacena contraseñas reales; utiliza el marcador `${DB_PASSWORD}` que es leído en tiempo de ejecución por `JPAUtil` desde las variables de entorno del sistema.

### Pasos para iniciar en IntelliJ IDEA:
1. Abra el proyecto y recargue las dependencias de Maven (**Reload All Maven Projects**).
2. Vaya a **Run → Edit Configurations...** y cree o seleccione la configuración de ejecución para la clase principal:
   ```text
   ni.edu.uam.laboratorioorm.Launcher
   ```
3. En el campo **Environment variables**, agregue la contraseña de su usuario `postgres`:
   ```text
   DB_PASSWORD=tu_contraseña_postgres
   ```
4. Ejecute la aplicación con **Run**.

*(También puede ejecutarse desde consola definiendo la variable de entorno y corriendo `./mvnw clean javafx:run`).*

---

## 4. Arquitectura del Proyecto

```text
├── evidencias/
│   ├── MATRIZ_PRUEBAS.md              # Matriz de pruebas P1–P8 y demostración de rollback()
│   ├── AppInicio.png                  # Carga inicial desde PostgreSQL
│   ├── P1-a.png ... P8.png            # Capturas de pantalla verificables (P1–P8)
│   └── P9_rollback_integridad.png     # Evidencia de manejo transaccional con rollback()
├── sql/
│   └── bd_laboratorio_orm.sql         # Script DDL + DML reproducible en PostgreSQL
├── pom.xml                            # Dependencias Maven (JavaFX 21, Hibernate 7.0.4, PostgreSQL JDBC)
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── ni/edu/uam/laboratorioorm/
    │       ├── Launcher.java                  # Punto de entrada de la aplicación
    │       ├── LaboratorioApplication.java    # Inicialización de JavaFX y cierre de EntityManagerFactory
    │       ├── controller/
    │       │   └── LaboratorioController.java # Eventos UI, validaciones, alertas y filtros
    │       ├── dao/
    │       │   ├── EquipoDAO.java             # CRUD transaccional y consultas JPQL parametrizadas
    │       │   └── TipoEquipoDAO.java         # Consulta de catálogo de tipos de equipo
    │       ├── database/
    │       │   └── JPAUtil.java               # Ciclo de vida de EntityManagerFactory y EntityManager
    │       └── model/
    │           ├── Equipo.java                # Entidad JPA mapeada a laboratorio.equipo
    │           └── TipoEquipo.java            # Entidad JPA mapeada a laboratorio.tipo_equipo
    └── resources/
        ├── META-INF/
        │   └── persistence.xml                # Unidad de persistencia "laboratorioPU" (hbm2ddl.auto=validate)
        └── ni/edu/uam/laboratorioorm/
            └── laboratorio-view.fxml          # Interfaz gráfica JavaFX
```

---

## 5. Modelo ORM, Persistencia y Consultas

* **Unidad de persistencia:** `laboratorioPU` configurada en `META-INF/persistence.xml` con `hibernate.hbm2ddl.auto = validate`, verificando que las entidades coincidan exactamente con el esquema físico en PostgreSQL.
* **Entidades y asociación (`@ManyToOne` / `@OneToMany`):**
  * `Equipo` es el lado propietario de la relación mediante `@ManyToOne(fetch = FetchType.LAZY, optional = false)` y `@JoinColumn(name = "id_tipo", nullable = false)`.
  * `TipoEquipo` define la relación inversa `@OneToMany(mappedBy = "tipoEquipo", fetch = FetchType.LAZY)` **sin `CascadeType.REMOVE`**, protegiendo la integridad referencial para evitar eliminaciones accidentales en cascada.
* **Operaciones CRUD y transacciones:** Implementadas en `EquipoDAO` abriendo y cerrando un `EntityManager` por unidad de trabajo, utilizando `persist()`, `merge()`, `remove()`, y control explícito mediante `tx.begin()`, `tx.commit()` y `tx.rollback()` ante excepciones.
* **Consultas JPQL parametrizadas:** `EquipoDAO.buscarPorNombreYTipo(...)` construye consultas JPQL con `JOIN FETCH e.tipoEquipo` y parámetros enlazados mediante `setParameter()` (`LOWER(e.nombre) LIKE LOWER(:nombre)` y `e.tipoEquipo.idTipo = :idTipo`), soportando búsqueda parcial por nombre, filtro por tipo o ambos criterios combinados sin concatenación de entradas del usuario.

---

## 6. Evidencias y Matriz de Pruebas

La matriz completa de pruebas (`P1`–`P8`), la demostración de manejo transaccional con `rollback()` y todas las capturas de pantalla se encuentran documentadas en:

* **[Ver Matriz de Pruebas y Evidencias (`evidencias/MATRIZ_PRUEBAS.md`)](evidencias/MATRIZ_PRUEBAS.md)**