package ar.edu.utn.frc.dsi.bolsines.external;

import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ServicioGPSTrackerNavTrackQX7A {

    private final RestClient restClient;

    protected ServicioGPSTrackerNavTrackQX7A() {
        this.restClient = null;
    }

    @Autowired
    public ServicioGPSTrackerNavTrackQX7A(GpsApiConfig gpsApiConfig) {
        this.restClient = RestClient.builder()
                .baseUrl(gpsApiConfig.getBaseUrl())
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
