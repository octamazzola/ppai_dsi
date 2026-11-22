package ar.edu.utn.frc.dsi.bolsines.external;

import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ServicioGPSTrackerGeoPulseMTR900 {

    private final RestClient restClient;

    protected ServicioGPSTrackerGeoPulseMTR900() {
        this.restClient = null;
    }

    @Autowired
    public ServicioGPSTrackerGeoPulseMTR900(GpsApiConfig gpsApiConfig) {
        this.restClient = RestClient.builder()
                .baseUrl(gpsApiConfig.getBaseUrl())
                .build();
    }

    // Firma fiel a Entrega 2 y a LiberaeMentis/dsi-api-bolsines
    public String fetchCargoPositions(String apiKey, int nroBolsin) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/geopulse-mtr-900/fetchCargoPositions")
                        .queryParam("numeroBolsin", nroBolsin)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}
