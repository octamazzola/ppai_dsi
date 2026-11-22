package ar.edu.utn.frc.dsi.bolsines.dto;

public class NotificacionResultadoResponse {

    private boolean correoEnviado;
    private boolean casoDeUsoFinalizado;
    private String flujoAlternativo;
    private String mensaje;
    private String destinatarioNombre;
    private String destinatarioEmail;
    private String comisionMedicaDestino;
    private Integer numeroBolsin;
    private Double latitud;
    private Double longitud;
    private String fechaHoraUltimaActualizacion;

    public NotificacionResultadoResponse() {
    }

    public NotificacionResultadoResponse(boolean correoEnviado, boolean casoDeUsoFinalizado,
                                         String flujoAlternativo, String mensaje,
                                         String destinatarioNombre, String destinatarioEmail,
                                         String comisionMedicaDestino, Integer numeroBolsin,
                                         Double latitud, Double longitud,
                                         String fechaHoraUltimaActualizacion) {
        this.correoEnviado = correoEnviado;
        this.casoDeUsoFinalizado = casoDeUsoFinalizado;
        this.flujoAlternativo = flujoAlternativo;
        this.mensaje = mensaje;
        this.destinatarioNombre = destinatarioNombre;
        this.destinatarioEmail = destinatarioEmail;
        this.comisionMedicaDestino = comisionMedicaDestino;
        this.numeroBolsin = numeroBolsin;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaHoraUltimaActualizacion = fechaHoraUltimaActualizacion;
    }

    public boolean isCorreoEnviado() { return correoEnviado; }
    public boolean isCasoDeUsoFinalizado() { return casoDeUsoFinalizado; }
    public String getFlujoAlternativo() { return flujoAlternativo; }
    public String getMensaje() { return mensaje; }
    public String getDestinatarioNombre() { return destinatarioNombre; }
    public String getDestinatarioEmail() { return destinatarioEmail; }
    public String getComisionMedicaDestino() { return comisionMedicaDestino; }
    public Integer getNumeroBolsin() { return numeroBolsin; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getFechaHoraUltimaActualizacion() { return fechaHoraUltimaActualizacion; }
}
