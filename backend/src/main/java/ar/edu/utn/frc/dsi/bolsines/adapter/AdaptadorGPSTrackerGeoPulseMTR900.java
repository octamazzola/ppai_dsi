package ar.edu.utn.frc.dsi.bolsines.adapter;

import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerGeoPulseMTR900;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class AdaptadorGPSTrackerGeoPulseMTR900 implements IAdaptadorGPSTracker {

    private static final String ERROR_PREFIX = "ERROR|";

    private final String apiKey;
    private final int[] nroBolsines;
    private String[] ubicacionRecibida;
    private String[][] ubicacionAdaptada;
    private final ServicioGPSTrackerGeoPulseMTR900 servicio;
    private final ObjectMapper objectMapper;

    public AdaptadorGPSTrackerGeoPulseMTR900(String apiKey, int[] nroBolsines,
            ServicioGPSTrackerGeoPulseMTR900 servicio, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.nroBolsines = nroBolsines;
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
                ubicacionRecibida[i] = servicio.fetchCargoPositions(apiKey, nroBolsines[i]);
            } catch (RestClientResponseException ex) {
                ubicacionRecibida[i] = ERROR_PREFIX + "HTTP " + ex.getStatusCode().value()
                        + ": " + ex.getResponseBodyAsString();
            } catch (RestClientException ex) {
                ubicacionRecibida[i] = ERROR_PREFIX + "Sin conexión con el servicio GeoPulse MTR-900";
            }
        }
    }

    @Override
    public void adaptarUbicacionBolsin() {
        for (int i = 0; i < nroBolsines.length; i++) {
            String raw = ubicacionRecibida[i];
            if (raw == null || raw.startsWith(ERROR_PREFIX)) {
                String detalleCrudo = raw == null ? "Sin respuesta de GeoPulse MTR-900" : raw.substring(ERROR_PREFIX.length());
                ubicacionAdaptada[i] = new String[]{
                        String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                        "El GPS Tracker (GeoPulse MTR-900) no pudo localizar el bolsín.",
                        detalleCrudo
                };
                continue;
            }
            try {
                // Formato oficial GeoPulse MTR-900 (matriz JSON): [[101,-31.4201,-64.1888,"2026-03-07T12:30:00"]]
                JsonNode matrix = objectMapper.readTree(raw);
                if (!matrix.isArray() || matrix.isEmpty() || !matrix.get(0).isArray() || matrix.get(0).size() < 4) {
                    ubicacionAdaptada[i] = new String[]{
                            String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                            "El GPS Tracker (GeoPulse MTR-900) no pudo localizar el bolsín.",
                            raw
                    };
                    continue;
                }
                JsonNode fila = matrix.get(0);
                ubicacionAdaptada[i] = new String[]{
                        fila.get(0).asText(String.valueOf(nroBolsines[i])),
                        fila.get(1).asText(""),
                        fila.get(2).asText(""),
                        fila.get(3).asText(""),
                        "OK",
                        "",
                        raw
                };
            } catch (Exception ex) {
                ubicacionAdaptada[i] = new String[]{
                        String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                        "El GPS Tracker (GeoPulse MTR-900) no pudo localizar el bolsín.",
                        raw
                };
            }
        }
    }
}
