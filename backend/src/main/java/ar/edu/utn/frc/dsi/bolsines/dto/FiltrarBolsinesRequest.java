package ar.edu.utn.frc.dsi.bolsines.dto;

public class FiltrarBolsinesRequest {

    private String numeroPrecinto;
    private String codigoCMDestino;

    public FiltrarBolsinesRequest() {
    }

    public FiltrarBolsinesRequest(String numeroPrecinto, String codigoCMDestino) {
        this.numeroPrecinto = numeroPrecinto;
        this.codigoCMDestino = codigoCMDestino;
    }

    public String getNumeroPrecinto() { return numeroPrecinto; }
    public void setNumeroPrecinto(String numeroPrecinto) { this.numeroPrecinto = numeroPrecinto; }
    public String getCodigoCMDestino() { return codigoCMDestino; }
    public void setCodigoCMDestino(String codigoCMDestino) { this.codigoCMDestino = codigoCMDestino; }
}
