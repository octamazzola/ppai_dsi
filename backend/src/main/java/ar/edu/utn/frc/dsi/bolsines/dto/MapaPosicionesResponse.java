package ar.edu.utn.frc.dsi.bolsines.dto;

import java.util.List;

public class MapaPosicionesResponse {

    private String proveedorMapa;
    private Double centroLatitud;
    private Double centroLongitud;
    private Integer zoomSugerido;
    private String resumenMapa;
    private List<BolsinUbicacionResponse> marcadores;

    public MapaPosicionesResponse() {
    }

    public MapaPosicionesResponse(String proveedorMapa, Double centroLatitud, Double centroLongitud,
                                  Integer zoomSugerido, String resumenMapa,
                                  List<BolsinUbicacionResponse> marcadores) {
        this.proveedorMapa = proveedorMapa;
        this.centroLatitud = centroLatitud;
        this.centroLongitud = centroLongitud;
        this.zoomSugerido = zoomSugerido;
        this.resumenMapa = resumenMapa;
        this.marcadores = marcadores;
    }

    public String getProveedorMapa() { return proveedorMapa; }
    public Double getCentroLatitud() { return centroLatitud; }
    public Double getCentroLongitud() { return centroLongitud; }
    public Integer getZoomSugerido() { return zoomSugerido; }
    public String getResumenMapa() { return resumenMapa; }
    public List<BolsinUbicacionResponse> getMarcadores() { return marcadores; }
}
