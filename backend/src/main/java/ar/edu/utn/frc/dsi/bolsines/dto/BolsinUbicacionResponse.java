package ar.edu.utn.frc.dsi.bolsines.dto;

public class BolsinUbicacionResponse {

    private Integer numeroBolsin;
    private String numeroPrecinto;
    private Double peso;
    private String fechaCreacion;
    private String codigoCMOrigen;
    private String nombreCMOrigen;
    private String codigoCMDestino;
    private String nombreCMDestino;
    private Double latitud;
    private Double longitud;
    private String fechaHoraUltimaActualizacion;
    private boolean ubicacionDisponible;
    private String mensajeErrorGPS;
    private String modeloGPS;
    private String respuestaCrudaGPS;
    private String urlGoogleMaps;

    public BolsinUbicacionResponse() {
    }

    public BolsinUbicacionResponse(Integer numeroBolsin, String numeroPrecinto, Double peso, String fechaCreacion,
                                   String codigoCMOrigen, String nombreCMOrigen,
                                   String codigoCMDestino, String nombreCMDestino,
                                   Double latitud, Double longitud, String fechaHoraUltimaActualizacion,
                                   boolean ubicacionDisponible, String mensajeErrorGPS,
                                   String modeloGPS, String respuestaCrudaGPS, String urlGoogleMaps) {
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.peso = peso;
        this.fechaCreacion = fechaCreacion;
        this.codigoCMOrigen = codigoCMOrigen;
        this.nombreCMOrigen = nombreCMOrigen;
        this.codigoCMDestino = codigoCMDestino;
        this.nombreCMDestino = nombreCMDestino;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaHoraUltimaActualizacion = fechaHoraUltimaActualizacion;
        this.ubicacionDisponible = ubicacionDisponible;
        this.mensajeErrorGPS = mensajeErrorGPS;
        this.modeloGPS = modeloGPS;
        this.respuestaCrudaGPS = respuestaCrudaGPS;
        this.urlGoogleMaps = urlGoogleMaps;
    }

    public Integer getNumeroBolsin() { return numeroBolsin; }
    public String getNumeroPrecinto() { return numeroPrecinto; }
    public Double getPeso() { return peso; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getCodigoCMOrigen() { return codigoCMOrigen; }
    public String getNombreCMOrigen() { return nombreCMOrigen; }
    public String getCodigoCMDestino() { return codigoCMDestino; }
    public String getNombreCMDestino() { return nombreCMDestino; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getFechaHoraUltimaActualizacion() { return fechaHoraUltimaActualizacion; }
    public boolean isUbicacionDisponible() { return ubicacionDisponible; }
    public String getMensajeErrorGPS() { return mensajeErrorGPS; }
    public String getModeloGPS() { return modeloGPS; }
    public String getRespuestaCrudaGPS() { return respuestaCrudaGPS; }
    public String getUrlGoogleMaps() { return urlGoogleMaps; }
}
