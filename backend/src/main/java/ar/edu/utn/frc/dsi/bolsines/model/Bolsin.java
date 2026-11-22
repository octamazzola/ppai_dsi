package ar.edu.utn.frc.dsi.bolsines.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bolsin")
public class Bolsin {

    @Id
    @Column(name = "numero_bolsin", nullable = false)
    private Integer numeroBolsin;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "numero_precinto", nullable = false)
    private String numeroPrecinto;

    @Column(name = "peso")
    private Double peso;

    @ManyToOne
    @JoinColumn(name = "cm_origen_codigo", nullable = false)
    private ComisionMedica origen;

    @ManyToOne
    @JoinColumn(name = "cm_destino_codigo", nullable = false)
    private ComisionMedica destino;

    @OneToMany(mappedBy = "bolsin", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();

    public Bolsin() {
    }

    public Bolsin(Integer numeroBolsin, LocalDate fecha, String numeroPrecinto, Double peso,
                  ComisionMedica origen, ComisionMedica destino) {
        this.numeroBolsin = numeroBolsin;
        this.fecha = fecha;
        this.numeroPrecinto = numeroPrecinto;
        this.peso = peso;
        this.origen = origen;
        this.destino = destino;
    }

    public Integer getNumeroBolsin() {
        return numeroBolsin;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getNroPrecinto() {
        return numeroPrecinto;
    }

    public Double getPeso() {
        return peso;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    public ComisionMedica obtenerCMDestino() {
        return destino;
    }

    public List<CambioEstadoBolsin> getCambiosEstado() {
        return cambiosEstado;
    }

    // Patron Experto: el Bolsin conoce su CM origen y determina si coincide
    public boolean esTuCMOrigen(ComisionMedica cmEmpleado) {
        return this.origen != null && cmEmpleado != null && this.origen.equals(cmEmpleado);
    }

    // Restriccion de implementacion (similar a Lote.conocerProyectoDeCultivoVigente() del PDF de catedra):
    // Bolsin mantiene la lista historica de CambioEstadoBolsin y recupera el actual cuando lo necesita.
    public CambioEstadoBolsin buscarCambioEstadoActual() {
        if (cambiosEstado == null) {
            return null;
        }
        return cambiosEstado.stream()
                .filter(CambioEstadoBolsin::sosActual)
                .findFirst()
                .orElse(null);
    }

    public boolean sosEnviado() {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        return actual != null && actual.sosEnviado();
    }

    public boolean tienePrecinto(String precintoBuscado) {
        return this.numeroPrecinto != null && precintoBuscado != null
                && this.numeroPrecinto.equalsIgnoreCase(precintoBuscado.trim());
    }

    public boolean tieneCMDestino(String codigoCMDestino) {
        return this.destino != null && this.destino.esTuCodigo(codigoCMDestino);
    }

    public String mostrarDatos() {
        String codOrigen = origen != null ? origen.getCodigo() : "-";
        String codDestino = destino != null ? destino.getCodigo() : "-";
        return "Bolsín #" + numeroBolsin + " | Precinto: " + numeroPrecinto
                + " | Origen: " + codOrigen + " -> Destino: " + codDestino;
    }
}
