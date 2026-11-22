package ar.edu.utn.frc.dsi.bolsines.service;

import ar.edu.utn.frc.dsi.bolsines.dto.NotificacionResultadoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GestorNotificarUbicacionBolsin {

    private static final Logger log = LoggerFactory.getLogger(GestorNotificarUbicacionBolsin.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // <<include>> Caso de Uso 31: Notificar ubicacion de bolsin (simulado con detalle completo del correo)
    public NotificacionResultadoResponse notificarUbicacion(Integer numeroBolsin,
                                                            String numeroPrecinto,
                                                            String nombreCMOrigen,
                                                            String nombreCMDestino,
                                                            String nombreGCM,
                                                            String emailGCM,
                                                            Double latitud,
                                                            Double longitud,
                                                            String fechaHoraUltimaActualizacion,
                                                            String modeloGPS) {
        String fechaHoraEnvio = LocalDateTime.now().format(FORMATO);
        String asunto = "[Bolsines] Ubicación actual del Bolsín #" + numeroBolsin + " en tránsito";
        String cuerpo = String.format(
                "Estimado/a %s (GCM - %s):%n%n"
                        + "Se informa la ubicación actual del bolsín en tránsito:%n"
                        + "- Número de Bolsín: %d (Precinto: %s)%n"
                        + "- Comisión Médica Origen: %s%n"
                        + "- Comisión Médica Destino: %s%n"
                        + "- Coordenadas (Lat, Lng): %.4f, %.4f%n"
                        + "- Última actualización GPS (%s): %s%n%n"
                        + "Notificación generada automáticamente por el Caso de Uso 31 (Notificar ubicación de bolsín).",
                nombreGCM, nombreCMDestino,
                numeroBolsin, numeroPrecinto,
                nombreCMOrigen, nombreCMDestino,
                latitud, longitud,
                modeloGPS, fechaHoraUltimaActualizacion
        );

        log.info("[CU 31 - SIMULACION EMAIL] Para: {} <{}> | Asunto: {} | Bolsín #{}",
                nombreGCM, emailGCM, asunto, numeroBolsin);

        return new NotificacionResultadoResponse(
                true,
                true,
                null,
                "Ejecución exitosa del Caso de Uso 31: se notificó por correo electrónico a " + emailGCM + ".",
                nombreGCM,
                emailGCM,
                nombreCMDestino,
                numeroBolsin,
                latitud,
                longitud,
                fechaHoraUltimaActualizacion,
                asunto,
                cuerpo,
                fechaHoraEnvio
        );
    }
}
