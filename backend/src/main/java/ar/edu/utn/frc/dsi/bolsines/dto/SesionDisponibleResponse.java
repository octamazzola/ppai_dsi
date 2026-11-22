package ar.edu.utn.frc.dsi.bolsines.dto;

public class SesionDisponibleResponse {

    private String nombreUsuario;
    private String nombreEmpleado;
    private String codigoCM;
    private String nombreCM;
    private String descripcionDemo;

    public SesionDisponibleResponse() {
    }

    public SesionDisponibleResponse(String nombreUsuario, String nombreEmpleado,
                                    String codigoCM, String nombreCM, String descripcionDemo) {
        this.nombreUsuario = nombreUsuario;
        this.nombreEmpleado = nombreEmpleado;
        this.codigoCM = codigoCM;
        this.nombreCM = nombreCM;
        this.descripcionDemo = descripcionDemo;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public String getCodigoCM() { return codigoCM; }
    public String getNombreCM() { return nombreCM; }
    public String getDescripcionDemo() { return descripcionDemo; }
}
