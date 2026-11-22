package ar.edu.utn.frc.dsi.bolsines.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    public Rol() {
    }

    public Rol(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    // Patron Experto: el Rol sabe si corresponde al Gerente de Comision Medica (GCM)
    public boolean esGCM() {
        if (this.nombre == null) {
            return false;
        }
        String normalizado = this.nombre.trim();
        return "GCM".equalsIgnoreCase(normalizado)
                || "Gerente de Comision Medica".equalsIgnoreCase(normalizado)
                || "Gerente de Comisión Médica".equalsIgnoreCase(normalizado);
    }
}
