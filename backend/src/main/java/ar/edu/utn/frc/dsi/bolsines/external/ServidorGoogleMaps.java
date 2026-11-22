package ar.edu.utn.frc.dsi.bolsines.external;

import ar.edu.utn.frc.dsi.bolsines.dto.BolsinUbicacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.MapaPosicionesResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServidorGoogleMaps {

    // Simulador de ServidorGoogleMaps (Opcion B segun diagrama_secuencia)
    public MapaPosicionesResponse solicitarMapa(List<BolsinUbicacionResponse> marcadores) {
        List<BolsinUbicacionResponse> validos = marcadores.stream()
                .filter(b -> b.isUbicacionDisponible() && b.getLatitud() != null && b.getLongitud() != null)
                .toList();

        double centroLat = -31.4201;
        double centroLng = -64.1888;
        int zoom = 6;

        if (!validos.isEmpty()) {
            centroLat = validos.stream().mapToDouble(BolsinUbicacionResponse::getLatitud).average().orElse(centroLat);
            centroLng = validos.stream().mapToDouble(BolsinUbicacionResponse::getLongitud).average().orElse(centroLng);
            zoom = validos.size() == 1 ? 12 : 6;
        }

        String resumen = "Mapa generado por ServidorGoogleMaps (simulado) con "
                + validos.size() + " marcador(es) con coordenadas válidas sobre "
                + marcadores.size() + " bolsín(es).";

        return new MapaPosicionesResponse(
                "Google Maps (simulado via IGoogleMaps)",
                Math.round(centroLat * 10000.0) / 10000.0,
                Math.round(centroLng * 10000.0) / 10000.0,
                zoom,
                resumen,
                marcadores
        );
    }
}
