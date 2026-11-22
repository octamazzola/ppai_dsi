package ar.edu.utn.frc.dsi.bolsines;

import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerGeoPulseMTR900;
import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerNavTrackQX7A;
import ar.edu.utn.frc.dsi.bolsines.adapter.AdaptadorGPSTrackerXTR4500L;
import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerGeoPulseMTR900;
import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerNavTrackQX7A;
import ar.edu.utn.frc.dsi.bolsines.external.ServicioGPSTrackerXTR4500L;
import ar.edu.utn.frc.dsi.bolsines.model.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.model.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.model.Empleado;
import ar.edu.utn.frc.dsi.bolsines.model.Sesion;
import ar.edu.utn.frc.dsi.bolsines.repository.IBolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.IEmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.ISesionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BolsinesCu36Tests {

    @Autowired
    private IBolsinRepository bolsinRepository;

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private ISesionRepository sesionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void persistenciaH2YPatronExpertoFiltranCorrectamenteBolsinesEnviados() {
        Sesion sesionActual = sesionRepository.findAll().stream()
                .filter(Sesion::esActual)
                .findFirst()
                .orElseThrow();

        Empleado ebLogueado = empleadoRepository.findAll().stream()
                .filter(e -> e.esTuUsuario(sesionActual.getUsuario()))
                .findFirst()
                .orElseThrow();

        ComisionMedica cmOrigen = ebLogueado.getCM();
        assertEquals("CM-CBA-01", cmOrigen.getCodigo());

        List<Bolsin> enviadosDeCba = bolsinRepository.findAll().stream()
                .filter(b -> b.esTuCMOrigen(cmOrigen))
                .filter(Bolsin::sosEnviado)
                .toList();

        // Solo 101 y 999 son de CM-CBA-01 en estado actual Enviado (120 Cerrado, 121 Recibido, 102 otra CM)
        assertEquals(2, enviadosDeCba.size());
        assertTrue(enviadosDeCba.stream().anyMatch(b -> b.getNumeroBolsin().equals(101)));
        assertTrue(enviadosDeCba.stream().anyMatch(b -> b.getNumeroBolsin().equals(999)));

        // Verificar GCM destino del bolsin 101 (CM-ROS-02)
        Bolsin b101 = enviadosDeCba.stream().filter(b -> b.getNumeroBolsin().equals(101)).findFirst().orElseThrow();
        Empleado gcmDestino = empleadoRepository.findAll().stream()
                .filter(e -> e.esTuCM(b101.obtenerCMDestino()))
                .filter(Empleado::sosGCM)
                .findFirst()
                .orElseThrow();

        assertEquals("gcm.rosario02@organismo.gob.ar", gcmDestino.getEmail());
    }

    @Test
    void losTresAdaptadoresNormalizanAlMismoFormatoUniforme() {
        ServicioGPSTrackerXTR4500L stubXtr = new ServicioGPSTrackerXTR4500L() {
            @Override
            public String getBolsinLocation(String apiKey, int nroBolsin, String codCMOrigen) {
                return "{\"items\":[{\"numeroBolsin\":101,\"latitud\":-31.4201,\"longitud\":-64.1888,\"fechaHoraUltimaActualizacion\":\"2026-03-07T12:30:00\"}]}";
            }
        };
        ServicioGPSTrackerNavTrackQX7A stubNav = new ServicioGPSTrackerNavTrackQX7A() {
            @Override
            public String retrieveTrackingData(String apiKey, int nroBolsin, String codCMDestino) {
                return "101,-31.4201,-64.1888,2026-03-07T12:30";
            }
        };
        ServicioGPSTrackerGeoPulseMTR900 stubGeo = new ServicioGPSTrackerGeoPulseMTR900() {
            @Override
            public String fetchCargoPositions(String apiKey, int nroBolsin) {
                return "[[101,-31.4201,-64.1888,\"2026-03-07T12:30:00\"]]";
            }
        };

        int[] nros = new int[]{101};

        String[][] resXtr = new AdaptadorGPSTrackerXTR4500L("k1", nros, "CM-CBA-01", stubXtr, objectMapper)
                .recibirUbicacionBolsin();
        String[][] resNav = new AdaptadorGPSTrackerNavTrackQX7A("k2", nros, new String[]{"CM-ROS-02"}, stubNav, objectMapper)
                .recibirUbicacionBolsin();
        String[][] resGeo = new AdaptadorGPSTrackerGeoPulseMTR900("k3", nros, stubGeo, objectMapper)
                .recibirUbicacionBolsin();

        for (String[][] res : new String[][][]{resXtr, resNav, resGeo}) {
            assertEquals("101", res[0][0]);
            assertEquals("-31.4201", res[0][1]);
            assertEquals("-64.1888", res[0][2]);
            assertEquals("OK", res[0][4]);
        }
    }

    @Test
    void adaptadorTraduceErrorExternoAMensajeDeDominioA3() {
        ServicioGPSTrackerXTR4500L stub404 = new ServicioGPSTrackerXTR4500L() {
            @Override
            public String getBolsinLocation(String apiKey, int nroBolsin, String codCMOrigen) {
                throw new org.springframework.web.client.RestClientException("404 Bolsin no encontrado");
            }
        };

        String[][] res = new AdaptadorGPSTrackerXTR4500L("k1", new int[]{999}, "CM-CBA-01", stub404, objectMapper)
                .recibirUbicacionBolsin();

        assertEquals("999", res[0][0]);
        assertEquals("ERROR", res[0][4]);
        assertEquals("El GPS Tracker (XTR-4500L) no pudo localizar el bolsín.", res[0][5]);
    }
}
