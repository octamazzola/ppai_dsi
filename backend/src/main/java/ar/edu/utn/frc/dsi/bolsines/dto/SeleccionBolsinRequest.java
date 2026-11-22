package ar.edu.utn.frc.dsi.bolsines.dto;

public class SeleccionBolsinRequest {

    private Integer numeroBolsin;

    public SeleccionBolsinRequest() {
    }

    public SeleccionBolsinRequest(Integer numeroBolsin) {
        this.numeroBolsin = numeroBolsin;
    }

    public Integer getNumeroBolsin() { return numeroBolsin; }
    public void setNumeroBolsin(Integer numeroBolsin) { this.numeroBolsin = numeroBolsin; }
}
