package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.dto.BolsinUbicacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.MapaPosicionesResponse;
import ar.edu.utn.frc.dsi.bolsines.external.ServidorGoogleMaps;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IGoogleMaps {

    private final ServidorGoogleMaps servidorGoogleMaps;

    public IGoogleMaps(ServidorGoogleMaps servidorGoogleMaps) {
        this.servidorGoogleMaps = servidorGoogleMaps;
    }

    // Boundary de mapa segun diagrama_secuencia (Opcion B)
    public MapaPosicionesResponse solicitarMapa(List<BolsinUbicacionResponse> marcadores) {
        return servidorGoogleMaps.solicitarMapa(marcadores);
    }
}
