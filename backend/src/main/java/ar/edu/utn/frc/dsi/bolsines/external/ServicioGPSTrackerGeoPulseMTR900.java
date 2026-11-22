package ar.edu.utn.frc.dsi.bolsines.external;

import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.springframework.web.client.RestClient;

public class ServicioGPSTrackerGeoPulseMTR900 {

    private final RestClient restClient;

    public ServicioGPSTrackerGeoPulseMTR900() {
        this(GpsApiConfig.getConfiguredBaseUrl());
    }

    public ServicioGPSTrackerGeoPulseMTR900(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
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
