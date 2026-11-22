package ar.edu.utn.frc.dsi.bolsines.dto;

import java.util.List;

public class IniciarConsultaResponse {

    private String empleadoLogueado;
    private String nombreUsuario;
    private String codigoCM;
    private String nombreCM;
    private String modeloGPS;
    private boolean sinBolsinesEnviados;
    private boolean algunErrorGPS;
    private String flujoAlternativo;
    private String mensaje;
    private List<String> destinosDisponibles;
    private List<BolsinUbicacionResponse> bolsines;
    private MapaPosicionesResponse mapa;

    public IniciarConsultaResponse() {
    }

    public IniciarConsultaResponse(String empleadoLogueado, String nombreUsuario,
                                   String codigoCM, String nombreCM, String modeloGPS,
                                   boolean sinBolsinesEnviados, boolean algunErrorGPS,
                                   String flujoAlternativo, String mensaje,
                                   List<String> destinosDisponibles,
                                   List<BolsinUbicacionResponse> bolsines,
                                   MapaPosicionesResponse mapa) {
        this.empleadoLogueado = empleadoLogueado;
        this.nombreUsuario = nombreUsuario;
        this.codigoCM = codigoCM;
        this.nombreCM = nombreCM;
        this.modeloGPS = modeloGPS;
        this.sinBolsinesEnviados = sinBolsinesEnviados;
        this.algunErrorGPS = algunErrorGPS;
        this.flujoAlternativo = flujoAlternativo;
        this.mensaje = mensaje;
        this.destinosDisponibles = destinosDisponibles;
        this.bolsines = bolsines;
        this.mapa = mapa;
    }

    public String getEmpleadoLogueado() { return empleadoLogueado; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getCodigoCM() { return codigoCM; }
    public String getNombreCM() { return nombreCM; }
    public String getModeloGPS() { return modeloGPS; }
    public boolean isSinBolsinesEnviados() { return sinBolsinesEnviados; }
    public boolean isAlgunErrorGPS() { return algunErrorGPS; }
    public String getFlujoAlternativo() { return flujoAlternativo; }
    public String getMensaje() { return mensaje; }
    public List<String> getDestinosDisponibles() { return destinosDisponibles; }
    public List<BolsinUbicacionResponse> getBolsines() { return bolsines; }
    public MapaPosicionesResponse getMapa() { return mapa; }
}
