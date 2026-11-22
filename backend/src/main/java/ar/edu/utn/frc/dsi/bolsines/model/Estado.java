package ar.edu.utn.frc.dsi.bolsines.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado")
public class Estado {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "ambito", nullable = false)
    private String ambito;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    public Estado() {
    }

    public Estado(Long id, String ambito, String nombre, String descripcion) {
        this.id = id;
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public String getAmbito() {
        return ambito;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Patron Experto: el Estado conoce su ambito y si representa "Enviado" de Bolsin
    public boolean esAmbitoBolsin() {
        return this.ambito != null
                && ("Bolsin".equalsIgnoreCase(this.ambito.trim())
                || "Bolsín".equalsIgnoreCase(this.ambito.trim()));
    }

    public boolean esAmbitoRemito() {
        return this.ambito != null && "Remito".equalsIgnoreCase(this.ambito.trim());
    }

    public boolean esAmbitoDocumentacion() {
        return this.ambito != null
                && ("Documentacion".equalsIgnoreCase(this.ambito.trim())
                || "Documentación".equalsIgnoreCase(this.ambito.trim()));
    }

    public boolean sosEnviado() {
        return esAmbitoBolsin() && this.nombre != null && "Enviado".equalsIgnoreCase(this.nombre.trim());
    }
}
