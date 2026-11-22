package ar.edu.utn.frc.dsi.bolsines.adapter;

import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerNavTrackQX7A;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class AdaptadorGPSTrackerNavTrackQX7A implements IAdaptadorGPSTracker {

    private static final String ERROR_PREFIX = "ERROR|";

    private final String apiKey;
    private final int[] nroBolsines;
    private final String[] codCMDestinos;
    private String[] ubicacionRecibida;
    private String[][] ubicacionAdaptada;
    private final ServicioGPSTrackerNavTrackQX7A servicio;
    private final ObjectMapper objectMapper;

    // Constructor fiel al diagrama de diseño E2 actualizado (composicion con ServicioGPSTrackerNavTrackQX7A)
    public AdaptadorGPSTrackerNavTrackQX7A(String apiKey, int[] nroBolsines, String[] codCMDestinos) {
        this(apiKey, nroBolsines, codCMDestinos, new ServicioGPSTrackerNavTrackQX7A(), new ObjectMapper());
    }

    // Sobrecarga para tests unitarios con stubs
    public AdaptadorGPSTrackerNavTrackQX7A(String apiKey, int[] nroBolsines, String[] codCMDestinos,
                                           ServicioGPSTrackerNavTrackQX7A servicio, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.nroBolsines = nroBolsines;
        this.codCMDestinos = codCMDestinos;
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
            String codDestino = (codCMDestinos != null && i < codCMDestinos.length) ? codCMDestinos[i] : "";
            try {
                ubicacionRecibida[i] = servicio.retrieveTrackingData(apiKey, nroBolsines[i], codDestino);
            } catch (RestClientResponseException ex) {
                ubicacionRecibida[i] = ERROR_PREFIX + "HTTP " + ex.getStatusCode().value()
                        + ": " + ex.getResponseBodyAsString();
            } catch (RestClientException ex) {
                ubicacionRecibida[i] = ERROR_PREFIX + "Sin conexión con el servicio NavTrack QX-7A";
            }
        }
    }

    @Override
    public void adaptarUbicacionBolsin() {
        for (int i = 0; i < nroBolsines.length; i++) {
            String raw = ubicacionRecibida[i];
            if (raw == null || raw.startsWith(ERROR_PREFIX)) {
                String detalleCrudo = raw == null ? "Sin respuesta de NavTrack QX-7A" : raw.substring(ERROR_PREFIX.length());
                ubicacionAdaptada[i] = new String[]{
                        String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                        "El GPS Tracker (NavTrack QX-7A) no pudo localizar el bolsín.",
                        detalleCrudo
                };
                continue;
            }
            String[] partes = raw.trim().split(",");
            if (partes.length < 4) {
                ubicacionAdaptada[i] = new String[]{
                        String.valueOf(nroBolsines[i]), "", "", "", "ERROR",
                        "El GPS Tracker (NavTrack QX-7A) no pudo localizar el bolsín.",
                        raw
                };
                continue;
            }
            ubicacionAdaptada[i] = new String[]{
                    partes[0].trim(),
                    partes[1].trim(),
                    partes[2].trim(),
                    partes[3].trim(),
                    "OK",
                    "",
                    raw
            };
        }
    }
}
