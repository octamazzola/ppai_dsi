package ar.edu.utn.frc.dsi.bolsines.external;

import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.springframework.web.client.RestClient;

public class ServicioGPSTrackerXTR4500L {

    private final RestClient restClient;

    public ServicioGPSTrackerXTR4500L() {
        this(GpsApiConfig.getConfiguredBaseUrl());
    }

    public ServicioGPSTrackerXTR4500L(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Firma fiel a Entrega 2 y a LiberaeMentis/dsi-api-bolsines
    public String getBolsinLocation(String apiKey, int nroBolsin, String codCMOrigen) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/xtr-4500l/getBolsinLocation")
                        .queryParam("numeroBolsin", nroBolsin)
                        .queryParam("codigoComisionMedicaOrigen", codCMOrigen)
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .body(String.class);
    }
}
