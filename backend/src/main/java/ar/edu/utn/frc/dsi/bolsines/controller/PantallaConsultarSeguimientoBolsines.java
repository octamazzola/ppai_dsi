package ar.edu.utn.frc.dsi.bolsines.controller;

import ar.edu.utn.frc.dsi.bolsines.dto.ConfirmarCorreoRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.FiltrarBolsinesRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.IniciarConsultaRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.IniciarConsultaResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.NotificacionResultadoResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.SeleccionBolsinRequest;
import ar.edu.utn.frc.dsi.bolsines.dto.SeleccionBolsinResponse;
import ar.edu.utn.frc.dsi.bolsines.controller.SesionController.UsuarioAutenticado;
import ar.edu.utn.frc.dsi.bolsines.service.GestorConsultarSeguimientoBolsines;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/seguimiento")
public class PantallaConsultarSeguimientoBolsines {

    private final GestorConsultarSeguimientoBolsines gestorConsultarSeguimientoBolsines;

    public PantallaConsultarSeguimientoBolsines(GestorConsultarSeguimientoBolsines gestorConsultarSeguimientoBolsines) {
        this.gestorConsultarSeguimientoBolsines = gestorConsultarSeguimientoBolsines;
    }

    @ModelAttribute("usuarioAutenticado")
    public UsuarioAutenticado requerirUsuario(HttpServletRequest request) {
        return SesionController.requerirUsuario(request);
    }

    // Paso 1: EB selecciona la opcion Consultar seguimiento de bolsines
    @PostMapping("/iniciar")
    public ResponseEntity<IniciarConsultaResponse> opcionConsultarUbicacion(
            @RequestBody(required = false) IniciarConsultaRequest request,
            @ModelAttribute(value = "usuarioAutenticado", binding = false) UsuarioAutenticado usuario) {
        String modeloGPS = request != null ? request.getModeloGPS() : null;
        IniciarConsultaResponse respuesta = habilitarPantalla(modeloGPS, usuario.empleadoId());
        return ResponseEntity.ok(respuesta);
    }

    // Mantiene consistencia con el diagrama de secuencia (igual que PantAdmLaboreos.habilitarVentana())
    private IniciarConsultaResponse habilitarPantalla(String modeloGPS, Long empleadoId) {
        return gestorConsultarSeguimientoBolsines.consultarUbicacion(modeloGPS, empleadoId);
    }

    // Paso 6: Filtrar por numero de precinto o CM destino (Flujo Alternativo A2)
    @PostMapping("/filtrar")
    public ResponseEntity<?> filtrarBolsines(@RequestBody(required = false) FiltrarBolsinesRequest request) {
        try {
            String precinto = request != null ? request.getNumeroPrecinto() : null;
            String codCMDestino = request != null ? request.getCodigoCMDestino() : null;
            return ResponseEntity.ok(gestorConsultarSeguimientoBolsines.filtrarBolsines(precinto, codCMDestino));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Paso 7: EB selecciona un bolsin del mapa
    @PostMapping("/seleccionar-bolsin")
    public ResponseEntity<?> tomarSeleccionBolsinMapa(@RequestBody SeleccionBolsinRequest request) {
        if (request == null || request.getNumeroBolsin() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe indicar el numeroBolsin a seleccionar."));
        }
        try {
            SeleccionBolsinResponse respuesta = gestorConsultarSeguimientoBolsines
                    .tomarSeleccionBolsinMapa(request.getNumeroBolsin());
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Paso 9: EB confirma o rechaza el envio de correo al GCM destino (Flujo Basico o A5)
    @PostMapping("/confirmar-correo")
    public ResponseEntity<?> tomarConfirmacionEnviarCorreo(@RequestBody ConfirmarCorreoRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe indicar confirmar=true o false."));
        }
        try {
            NotificacionResultadoResponse resultado = gestorConsultarSeguimientoBolsines
                    .tomarConfirmacionEnviarCorreo(request.isConfirmar());
            return ResponseEntity.ok(resultado);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Flujo Alternativo A4: EB cancela la ejecucion del caso de uso
    @PostMapping("/cancelar")
    public ResponseEntity<NotificacionResultadoResponse> cancelarCasoDeUso() {
        return ResponseEntity.ok(gestorConsultarSeguimientoBolsines.cancelarCU());
    }
}
