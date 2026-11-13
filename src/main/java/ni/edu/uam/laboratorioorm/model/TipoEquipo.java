package ni.edu.uam.laboratorioorm.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_equipo", schema = "laboratorio")
public class TipoEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long idTipo;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = true, length = 250)
    private String descripcion;

    // Sin CascadeType.REMOVE para evitar borrado en cascada accidental
    @OneToMany(mappedBy = "tipoEquipo", fetch = FetchType.LAZY)
    private List<Equipo> equipos = new ArrayList<>();

    public TipoEquipo() {
    }

    public TipoEquipo(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    // Permite que los ComboBox muestren el nombre legible del tipo de equipo
    @Override
    public String toString() {
        return nombre;
    }
}