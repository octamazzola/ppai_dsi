package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerGeoPulseMTR900;
import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerNavTrackQX7A;
import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerXTR4500L;
import ar.edu.utn.frc.dsi.bolsines.adapter.IAdaptadorGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.config.GpsApiConfig;
import org.springframework.stereotype.Component;

@Component
public class InterfazGPSTracker {

    public static final String MODELO_XTR = "XTR-4500L";
    public static final String MODELO_NAVTRACK = "NavTrack QX-7A";
    public static final String MODELO_GEOPULSE = "GeoPulse MTR-900";

    private final String[] apiKeyGPS;

    public InterfazGPSTracker(GpsApiConfig gpsApiConfig) {
        this.apiKeyGPS = gpsApiConfig.getApiKeys();
    }

    // Firma ampliada acordada (soporta los 3 proveedores GPS via polimorfismo con IAdaptadorGPSTracker)
    public String[][] obtenerUbicacionBolsin(int[] nroBolsines, String codCMOrigen,
                                             String[] codCMDestinos, String modeloGPS) {
        IAdaptadorGPSTracker adaptador = crearAdaptador(nroBolsines, codCMOrigen, codCMDestinos, modeloGPS);
        return adaptador.recibirUbicacionBolsin();
    }

    private IAdaptadorGPSTracker crearAdaptador(int[] nroBolsines, String codCMOrigen,
                                               String[] codCMDestinos, String modeloGPS) {
        String modelo = (modeloGPS == null || modeloGPS.isBlank()) ? MODELO_XTR : modeloGPS.trim();
        if (MODELO_NAVTRACK.equalsIgnoreCase(modelo) || "NAVTRACK".equalsIgnoreCase(modelo)) {
            return new AdaptadorGPSTrackerNavTrackQX7A(apiKeyGPS[1], nroBolsines, codCMDestinos);
        }
        if (MODELO_GEOPULSE.equalsIgnoreCase(modelo) || "GEOPULSE".equalsIgnoreCase(modelo)) {
            return new AdaptadorGPSTrackerGeoPulseMTR900(apiKeyGPS[2], nroBolsines);
        }
        return new AdaptadorGPSTrackerXTR4500L(apiKeyGPS[0], nroBolsines, codCMOrigen);
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
