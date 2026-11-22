package ar.edu.utn.frc.dsi.bolsines.external;

import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.springframework.web.client.RestClient;

public class ServicioGPSTrackerNavTrackQX7A {

    private final RestClient restClient;

    public ServicioGPSTrackerNavTrackQX7A() {
        this(GpsApiConfig.getConfiguredBaseUrl());
    }

    public ServicioGPSTrackerNavTrackQX7A(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Firma fiel a Entrega 2 y a LiberaeMentis/dsi-api-bolsines
    public String retrieveTrackingData(String apiKey, int nroBolsin, String codCMDestino) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/navtrack-qx-7a/retrieveTrackingData")
                        .queryParam("numeroBolsin", nroBolsin)
                        .queryParam("codigoComisionMedicaDestino", codCMDestino)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}
