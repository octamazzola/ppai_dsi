package ar.edu.utn.frc.dsi.bolsines.dto;

public class IniciarConsultaRequest {

    private String modeloGPS;
    private String nombreUsuario;

    public IniciarConsultaRequest() {
    }

    public IniciarConsultaRequest(String modeloGPS, String nombreUsuario) {
        this.modeloGPS = modeloGPS;
        this.nombreUsuario = nombreUsuario;
    }

    public String getModeloGPS() {
        return modeloGPS;
    }

    public void setModeloGPS(String modeloGPS) {
        this.modeloGPS = modeloGPS;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
