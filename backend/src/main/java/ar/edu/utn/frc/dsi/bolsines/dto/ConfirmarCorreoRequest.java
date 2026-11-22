package ar.edu.utn.frc.dsi.bolsines.dto;

public class ConfirmarCorreoRequest {

    private boolean confirmar;

    public ConfirmarCorreoRequest() {
    }

    public ConfirmarCorreoRequest(boolean confirmar) {
        this.confirmar = confirmar;
    }

    public boolean isConfirmar() { return confirmar; }
    public void setConfirmar(boolean confirmar) { this.confirmar = confirmar; }
}
