package ar.edu.utn.frc.dsi.bolsines.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "localidad")
public class Localidad {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;

    public Localidad() {
    }

    public Localidad(Long id, String nombre, String descripcion, Provincia provincia) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.provincia = provincia;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Provincia getProvincia() {
        return provincia;
    }
}
