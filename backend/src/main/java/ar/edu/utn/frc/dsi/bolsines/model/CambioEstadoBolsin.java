package ar.edu.utn.frc.dsi.bolsines.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "cambio_estado_bolsin")
public class CambioEstadoBolsin {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "responsable_ce_id", nullable = false)
    private Empleado responsableCE;

    @ManyToOne
    @JoinColumn(name = "bolsin_numero", nullable = false)
    private Bolsin bolsin;

    public CambioEstadoBolsin() {
    }

    public CambioEstadoBolsin(Long id, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                              Estado estado, Empleado responsableCE, Bolsin bolsin) {
        this.id = id;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.estado = estado;
        this.responsableCE = responsableCE;
        this.bolsin = bolsin;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public Estado getEstado() {
        return estado;
    }

    public Empleado getResponsableCE() {
        return responsableCE;
    }

    // Patron Experto (Modelo de Dominio oficial: CambioEstadoBolsin)
    public boolean sosActual() {
        return this.fechaHoraFin == null;
    }

    public boolean sosEnviado() {
        return this.estado != null && this.estado.sosEnviado();
    }
}
