package ar.edu.utn.frc.dsi.bolsines.dto;

public class SeleccionBolsinResponse {

    private BolsinUbicacionResponse bolsin;
    private String mensajeConfirmacion;
    private boolean requiereConfirmacionCorreo;
    private String flujoAlternativo;

    public SeleccionBolsinResponse() {
    }

    public SeleccionBolsinResponse(BolsinUbicacionResponse bolsin, String mensajeConfirmacion,
                                   boolean requiereConfirmacionCorreo, String flujoAlternativo) {
        this.bolsin = bolsin;
        this.mensajeConfirmacion = mensajeConfirmacion;
        this.requiereConfirmacionCorreo = requiereConfirmacionCorreo;
        this.flujoAlternativo = flujoAlternativo;
    }

    public BolsinUbicacionResponse getBolsin() { return bolsin; }
    public String getMensajeConfirmacion() { return mensajeConfirmacion; }
    public boolean isRequiereConfirmacionCorreo() { return requiereConfirmacionCorreo; }
    public String getFlujoAlternativo() { return flujoAlternativo; }
}
