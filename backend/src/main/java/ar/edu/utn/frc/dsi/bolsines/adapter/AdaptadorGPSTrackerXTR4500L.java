package ar.edu.utn.frc.dsi.bolsines.adapter;

import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerXTR4500L;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class AdaptadorGPSTrackerXTR4500L implements IAdaptadorGPSTracker {

    private static final String ERROR_PREFIX = "ERROR|";

    private final String apiKey;
    private final int[] nroBolsines;
    private final String codCMOrigen;
    private String[] ubicacionRecibida;
    private String[][] ubicacionAdaptada;
    private final ServicioGPSTrackerXTR4500L servicio;
    private final ObjectMapper objectMapper;

    public AdaptadorGPSTrackerXTR4500L(String apiKey, int[] nroBolsines, String codCMOrigen,
                                       ServicioGPSTrackerXTR4500L servicio, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.nroBolsines = nroBolsines;
        this.codCMOrigen = codCMOrigen;
        this.servicio = servicio;
        this.objectMapper = objectMapper;
        this.ubicacionRecibida = new String[nroBolsines.length];
        this.ubicacionAdaptada = new String[nroBolsines.length][7];
    }

    @Override
    public String[][] recibirUbicacionBolsin() {
        solicitarUbicacionBolsin();
        adaptarUbicacionBolsin();
        return ubicacionAdaptada;
    }

    @Override
    public void solicitarUbicacionBolsin() {
        for (int i = 0; i < nroBolsines.length; i++) {
            try {
                ubicacionRecibida[i] = servicio.getBolsinLocation(apiKey, nroBolsines[i], codCMOrigen);
            } catch (RestClientResponseException ex) {
                ubicacionRecibida[i] = ERROR_PREFIX + "HTTP " + ex.getStatusCode().value()
                        + ": " + ex.getResponseBodyAsString();
            } catch (RestClientException ex) {
                ubicacionRecibida[i] = ERROR_PREFIX + "Sin conexión con el servicio XTR-4500L";
            }
        }
    }

    @Override
    public void adaptarUbicacionBolsin() {
        for (int i = 0; i < nroBolsines.length; i++) {
            String raw = ubicacionRecibida[i];
            if (raw == null || raw.startsWith(ERROR_PREFIX)) {
                String detalleCrudo = raw == null ? "Sin respuesta de XTR-4500L" : raw.substring(ERROR_PREFIX.length());
                ubicacionAdaptada[i] = new String[]{
                        String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                        "El GPS Tracker (XTR-4500L) no pudo localizar el bolsín.",
                        detalleCrudo
                };
                continue;
            }
            try {
                // Formato oficial XTR-4500L: {"items":[{"numeroBolsin":101,"latitud":-31.4201,"longitud":-64.1888,"fechaHoraUltimaActualizacion":"2026-03-07T12:30:00"}]}
                JsonNode root = objectMapper.readTree(raw);
                JsonNode items = root.path("items");
                if (!items.isArray() || items.isEmpty()) {
                    ubicacionAdaptada[i] = new String[]{
                            String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                            "El GPS Tracker (XTR-4500L) no pudo localizar el bolsín.",
                            raw
                    };
                    continue;
                }
                JsonNode item = items.get(0);
                ubicacionAdaptada[i] = new String[]{
                        item.path("numeroBolsin").asText(String.valueOf(nroBolsines[i])),
                        item.path("latitud").asText(""),
                        item.path("longitud").asText(""),
                        item.path("fechaHoraUltimaActualizacion").asText(""),
                        "OK",
                        "",
                        raw
                };
            } catch (Exception ex) {
                ubicacionAdaptada[i] = new String[]{
                        String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                        "El GPS Tracker (XTR-4500L) no pudo localizar el bolsín.",
                        raw
                };
            }
        }
    }
}
