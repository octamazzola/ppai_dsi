package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerGeoPulseMTR900;
import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerNavTrackQX7A;
import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerXTR4500L;
import ar.edu.utn.frc.dsi.bolsines.adapter.IAdaptadorGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerGeoPulseMTR900;
import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerNavTrackQX7A;
import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerXTR4500L;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class InterfazGPSTracker {

    public static final String MODELO_XTR = "XTR-4500L";
    public static final String MODELO_NAVTRACK = "NavTrack QX-7A";
    public static final String MODELO_GEOPULSE = "GeoPulse MTR-900";

    private final String[] apiKeyGPS;
    private String[][] ubicacionBolsinAdaptada;
    private int[] nroBolsines;
    private String codCMOrigen;
    private String[] codCMDestinos;

    private final ServicioGPSTrackerXTR4500L servicioXtr;
    private final ServicioGPSTrackerNavTrackQX7A servicioNavTrack;
    private final ServicioGPSTrackerGeoPulseMTR900 servicioGeoPulse;
    private final ObjectMapper objectMapper;

    public InterfazGPSTracker(GpsApiConfig gpsApiConfig,
                              ServicioGPSTrackerXTR4500L servicioXtr,
                              ServicioGPSTrackerNavTrackQX7A servicioNavTrack,
                              ServicioGPSTrackerGeoPulseMTR900 servicioGeoPulse,
                              ObjectMapper objectMapper) {
        this.apiKeyGPS = gpsApiConfig.getApiKeys();
        this.servicioXtr = servicioXtr;
        this.servicioNavTrack = servicioNavTrack;
        this.servicioGeoPulse = servicioGeoPulse;
        this.objectMapper = objectMapper;
    }

    // Firma ampliada acordada (soporta los 3 proveedores GPS via polimorfismo con IAdaptadorGPSTracker)
    public String[][] obtenerUbicacionBolsin(int[] nroBolsines, String codCMOrigen,
                                             String[] codCMDestinos, String modeloGPS) {
        this.nroBolsines = nroBolsines;
        this.codCMOrigen = codCMOrigen;
        this.codCMDestinos = codCMDestinos;

        IAdaptadorGPSTracker adaptador = crearAdaptador(modeloGPS);
        this.ubicacionBolsinAdaptada = adaptador.recibirUbicacionBolsin();
        return this.ubicacionBolsinAdaptada;
    }

    private IAdaptadorGPSTracker crearAdaptador(String modeloGPS) {
        String modelo = (modeloGPS == null || modeloGPS.isBlank()) ? MODELO_XTR : modeloGPS.trim();
        if (MODELO_NAVTRACK.equalsIgnoreCase(modelo) || "NAVTRACK".equalsIgnoreCase(modelo)) {
            return new AdaptadorGPSTrackerNavTrackQX7A(apiKeyGPS[1], nroBolsines, codCMDestinos, servicioNavTrack, objectMapper);
        }
        if (MODELO_GEOPULSE.equalsIgnoreCase(modelo) || "GEOPULSE".equalsIgnoreCase(modelo)) {
            return new AdaptadorGPSTrackerGeoPulseMTR900(apiKeyGPS[2], nroBolsines, servicioGeoPulse, objectMapper);
        }
        return new AdaptadorGPSTrackerXTR4500L(apiKeyGPS[0], nroBolsines, codCMOrigen, servicioXtr, objectMapper);
    }

    public String normalizarNombreModelo(String modeloGPS) {
        if (modeloGPS == null || modeloGPS.isBlank()) {
            return MODELO_XTR;
        }
        String m = modeloGPS.trim();
        if (MODELO_NAVTRACK.equalsIgnoreCase(m) || "NAVTRACK".equalsIgnoreCase(m)) {
            return MODELO_NAVTRACK;
        }
        if (MODELO_GEOPULSE.equalsIgnoreCase(m) || "GEOPULSE".equalsIgnoreCase(m)) {
            return MODELO_GEOPULSE;
        }
        return MODELO_XTR;
    }
}
