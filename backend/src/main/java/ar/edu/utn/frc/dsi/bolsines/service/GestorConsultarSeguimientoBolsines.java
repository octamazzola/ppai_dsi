package ar.edu.utn.frc.dsi.bolsines.service;

import ar.edu.utn.frc.dsi.bolsines.boundary.IGoogleMaps;
import ar.edu.utn.frc.dsi.bolsines.boundary.InterfazGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.dto.BolsinUbicacionResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.IniciarConsultaResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.MapaPosicionesResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.NotificacionResultadoResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.SeleccionBolsinResponse;
import ar.edu.utn.frc.dsi.bolsines.dto.SesionDisponibleResponse;
import ar.edu.utn.frc.dsi.bolsines.model.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.model.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.model.Empleado;
import ar.edu.utn.frc.dsi.bolsines.model.Sesion;
import ar.edu.utn.frc.dsi.bolsines.model.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repository.IBolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.IEmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repository.ISesionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class GestorConsultarSeguimientoBolsines {

    private final ISesionRepository sesionRepository;
    private final IEmpleadoRepository empleadoRepository;
    private final IBolsinRepository bolsinRepository;
    private final InterfazGPSTracker interfazGPSTracker;
    private final IGoogleMaps iGoogleMaps;
    private final GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin;

    // Estado interno del Caso de Uso en curso (mismo enfoque que GestorLaboreos del
    // ejemplo de catedra)
    private Empleado empleadoLogueado;
    private ComisionMedica cmEmpleadoLogueado;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private int[] nroBolsines;
    private String codCMOrigen;
    private String[] codCMDestinos;
    private String[][] ubicacionBolsines;
    private List<BolsinUbicacionResponse> bolsinesConUbicacion = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private BolsinUbicacionResponse bolsinSeleccionadoDto;
    private String modeloGPSSeleccionado = InterfazGPSTracker.MODELO_XTR;

    public GestorConsultarSeguimientoBolsines(ISesionRepository sesionRepository,
            IEmpleadoRepository empleadoRepository,
            IBolsinRepository bolsinRepository,
            InterfazGPSTracker interfazGPSTracker,
            IGoogleMaps iGoogleMaps,
            GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin) {
        this.sesionRepository = sesionRepository;
        this.empleadoRepository = empleadoRepository;
        this.bolsinRepository = bolsinRepository;
        this.interfazGPSTracker = interfazGPSTracker;
        this.iGoogleMaps = iGoogleMaps;
        this.gestorNotificarUbicacionBolsin = gestorNotificarUbicacionBolsin;
    }

    public List<SesionDisponibleResponse> listarSesionesDemo() {
        List<Empleado> empleados = empleadoRepository.findAll();
        List<SesionDisponibleResponse> resultado = new ArrayList<>();
        for (Empleado e : empleados) {
            if (e.getUsuario() != null && !e.sosGCM()) {
                String codCm = e.getCM().getCodigo();
                String desc = switch (codCm) {
                    case "CM-CBA-01" -> "Flujo básico (Bolsín 101 OK) + A3 (Bolsín 999 sin GPS) + filtros de estado";
                    case "CM-ROS-02" -> "Flujo básico limpio (Bolsín 102 OK en los 3 proveedores GPS)";
                    case "CM-MDZ-03" -> "Flujo básico limpio (Bolsín 103 OK en los 3 proveedores GPS)";
                    case "CM-JUJ-01" -> "Flujo Alternativo A1 (0 bolsines en estado Enviado)";
                    default -> "Encargado de Bolsines de " + codCm;
                };
                resultado.add(new SesionDisponibleResponse(
                        e.getUsuario().getNombreUsuario(),
                        e.getNombreCompleto(),
                        codCm,
                        e.mostrarCM(),
                        desc));
            }
        }
        return resultado;
    }

    // Pasos 1 a 6 del CU 36
    public IniciarConsultaResponse consultarUbicacion(String modeloGPS, String nombreUsuarioDemo) {
        this.modeloGPSSeleccionado = interfazGPSTracker.normalizarNombreModelo(modeloGPS);
        this.bolsinSeleccionado = null;
        this.bolsinSeleccionadoDto = null;

        // 1. Buscar empleado logueado desde la Sesion actual (o usuario demo
        // seleccionado)
        this.empleadoLogueado = buscarEmpleadoLogueado(nombreUsuarioDemo);
        if (this.empleadoLogueado == null) {
            throw new IllegalStateException("No se encontró un empleado asociado a la sesión actual.");
        }

        // 2. Buscar CM del empleado logueado
        this.cmEmpleadoLogueado = buscarCMEmpleadoLogueado();
        this.codCMOrigen = this.cmEmpleadoLogueado.getCodigo();

        // 3. Buscar bolsines en estado Enviado cuya CM origen == CM del usuario
        // logueado
        this.bolsinesEnviados = buscarBolsinesEnviadosEnCM();

        // Flujo Alternativo A1: No se encuentran bolsines en estado enviado
        if (this.bolsinesEnviados.isEmpty()) {
            MapaPosicionesResponse mapaVacio = buscarMapa(List.of());
            return new IniciarConsultaResponse(
                    empleadoLogueado.getNombreCompleto(),
                    empleadoLogueado.getUsuario().getNombreUsuario(),
                    cmEmpleadoLogueado.getCodigo(),
                    cmEmpleadoLogueado.getNombre(),
                    modeloGPSSeleccionado,
                    true,
                    false,
                    "A1",
                    "A1: No se encuentran bolsines en estado Enviado para " + cmEmpleadoLogueado.getNombre() + ".",
                    List.of(),
                    List.of(),
                    mapaVacio);
        }

        // 4. Buscar datos de bolsines (nroBolsin, nroPrecinto, CM destino)
        buscarDatosBolsines();

        // 5. Solicitar datos de ubicacion al GPS Tracker via Patron Adapter
        this.ubicacionBolsines = solicitarDatosUbicacionBolsin();

        // Construir DTOs combinando datos de dominio + ubicacion adaptada
        this.bolsinesConUbicacion = construirBolsinesConUbicacion();

        // 6. Buscar mapa via IGoogleMaps (Opcion B)
        MapaPosicionesResponse mapa = buscarMapa(this.bolsinesConUbicacion);

        boolean algunErrorGPS = this.bolsinesConUbicacion.stream().anyMatch(b -> !b.isUbicacionDisponible());
        List<String> destinosDisponibles = this.bolsinesConUbicacion.stream()
                .map(BolsinUbicacionResponse::getCodigoCMDestino)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        String flujoAlt = algunErrorGPS ? "A3" : null;
        String mensaje = algunErrorGPS
                ? "Se encontraron " + bolsinesConUbicacion.size()
                        + " bolsines enviados. Atención (A3): el GPS Tracker no pudo informar ubicación de uno o más bolsines."
                : "Se encontraron " + bolsinesConUbicacion.size()
                        + " bolsín(es) en estado Enviado con ubicación obtenida mediante " + modeloGPSSeleccionado
                        + ".";

        return new IniciarConsultaResponse(
                empleadoLogueado.getNombreCompleto(),
                empleadoLogueado.getUsuario().getNombreUsuario(),
                cmEmpleadoLogueado.getCodigo(),
                cmEmpleadoLogueado.getNombre(),
                modeloGPSSeleccionado,
                false,
                algunErrorGPS,
                flujoAlt,
                mensaje,
                destinosDisponibles,
                this.bolsinesConUbicacion,
                mapa);
    }

    // Paso 6 (filtros por numero de precinto o CM destino) + Flujo Alternativo A2
    public IniciarConsultaResponse filtrarBolsines(String numeroPrecinto, String codigoCMDestino) {
        if (empleadoLogueado == null || cmEmpleadoLogueado == null) {
            throw new IllegalStateException("Primero debe iniciar la consulta de seguimiento de bolsines.");
        }

        boolean filtroPrecintoActivo = numeroPrecinto != null && !numeroPrecinto.isBlank();
        boolean filtroDestinoActivo = codigoCMDestino != null && !codigoCMDestino.isBlank();

        List<Bolsin> bolsinesFiltrados = this.bolsinesEnviados.stream()
                .filter(b -> !filtroPrecintoActivo || b.tienePrecinto(numeroPrecinto))
                .filter(b -> !filtroDestinoActivo || b.tieneCMDestino(codigoCMDestino))
                .toList();

        List<Integer> numerosFiltrados = bolsinesFiltrados.stream()
                .map(Bolsin::getNumeroBolsin)
                .toList();

        List<BolsinUbicacionResponse> dtosFiltrados = this.bolsinesConUbicacion.stream()
                .filter(dto -> numerosFiltrados.contains(dto.getNumeroBolsin()))
                .toList();

        MapaPosicionesResponse mapaFiltrado = buscarMapa(dtosFiltrados);

        List<String> destinosDisponibles = this.bolsinesConUbicacion.stream()
                .map(BolsinUbicacionResponse::getCodigoCMDestino)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        String flujoAlt = null;
        String mensaje;
        if (dtosFiltrados.isEmpty() && filtroPrecintoActivo) {
            flujoAlt = "A2";
            mensaje = "A2: No se encuentra bolsín con el número de precinto ingresado (" + numeroPrecinto.trim() + ").";
        } else if (dtosFiltrados.isEmpty()) {
            mensaje = "No se encontraron bolsines para los filtros seleccionados.";
        } else {
            mensaje = "Mostrando " + dtosFiltrados.size() + " bolsín(es) filtrado(s).";
        }

        boolean algunErrorGPS = dtosFiltrados.stream().anyMatch(b -> !b.isUbicacionDisponible());

        return new IniciarConsultaResponse(
                empleadoLogueado.getNombreCompleto(),
                empleadoLogueado.getUsuario().getNombreUsuario(),
                cmEmpleadoLogueado.getCodigo(),
                cmEmpleadoLogueado.getNombre(),
                modeloGPSSeleccionado,
                false,
                algunErrorGPS,
                flujoAlt,
                mensaje,
                destinosDisponibles,
                dtosFiltrados,
                mapaFiltrado);
    }

    // Pasos 7 y 8 del CU 36
    public SeleccionBolsinResponse tomarSeleccionBolsinMapa(Integer numeroBolsin) {
        if (bolsinesEnviados.isEmpty()) {
            throw new IllegalStateException("No hay bolsines cargados en la consulta actual.");
        }

        this.bolsinSeleccionado = this.bolsinesEnviados.stream()
                .filter(b -> Objects.equals(b.getNumeroBolsin(), numeroBolsin))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el bolsín #" + numeroBolsin + " en la consulta actual."));

        this.bolsinSeleccionadoDto = this.bolsinesConUbicacion.stream()
                .filter(dto -> Objects.equals(dto.getNumeroBolsin(), numeroBolsin))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Sin datos de ubicación para el bolsín #" + numeroBolsin));

        // Si el bolsin seleccionado no tiene ubicacion GPS disponible -> Flujo
        // Alternativo A3
        if (!bolsinSeleccionadoDto.isUbicacionDisponible()) {
            return new SeleccionBolsinResponse(
                    bolsinSeleccionadoDto,
                    "A3: El GPS Tracker (" + modeloGPSSeleccionado
                            + ") no pudo localizar ni informar la ubicación del bolsín #"
                            + numeroBolsin + ". No es posible notificar su ubicación.",
                    false,
                    "A3");
        }

        ComisionMedica destino = bolsinSeleccionado.obtenerCMDestino();
        String pregunta = "¿Desea enviar un correo electrónico al Gerente de la Comisión Médica destino ("
                + destino.getCodigo() + " - " + destino.getNombre()
                + ") para informar la ubicación del Bolsín #" + bolsinSeleccionado.getNumeroBolsin() + "?";

        return new SeleccionBolsinResponse(
                bolsinSeleccionadoDto,
                pregunta,
                true,
                null);
    }

    // Pasos 9, 10 y 11 del CU 36 + Flujo Alternativo A5
    public NotificacionResultadoResponse tomarConfirmacionEnviarCorreo(boolean confirmar) {
        if (this.bolsinSeleccionado == null || this.bolsinSeleccionadoDto == null) {
            throw new IllegalStateException("Debe seleccionar un bolsín antes de confirmar el envío de correo.");
        }

        // Flujo Alternativo A5: El EB no selecciona la opcion para enviar un correo
        // electronico
        if (!confirmar) {
            Integer nro = bolsinSeleccionado.getNumeroBolsin();
            finCU();
            return new NotificacionResultadoResponse(
                    false,
                    true,
                    "A5",
                    "A5: El Encargado de Bolsines decidió no enviar correo electrónico para el Bolsín #" + nro
                            + ". Fin del caso de uso.",
                    null, null, null, nro, null, null, null, null, null, null);
        }

        if (!this.bolsinSeleccionadoDto.isUbicacionDisponible()) {
            throw new IllegalStateException("A3: El bolsín seleccionado no posee ubicación GPS válida para notificar.");
        }

        // Paso 10: Buscar GCM de la Comision Medica DESTINO (segun ERS pasos 8 y 10)
        ComisionMedica cmDestino = this.bolsinSeleccionado.obtenerCMDestino();
        Empleado gerenteDestino = buscarGCM(cmDestino);
        if (gerenteDestino == null) {
            throw new IllegalStateException(
                    "No se encontró un Gerente de Comisión Médica (GCM) para " + cmDestino.getNombre());
        }

        String emailGCM = buscarEmailGCM(gerenteDestino);

        // Paso 11: <<include>> Caso de Uso 31 - Notificar ubicacion de bolsin
        NotificacionResultadoResponse resultado = gestorNotificarUbicacionBolsin.notificarUbicacion(
                bolsinSeleccionado.getNumeroBolsin(),
                bolsinSeleccionado.getNroPrecinto(),
                bolsinSeleccionado.getOrigen().getNombre(),
                cmDestino.getNombre(),
                gerenteDestino.getNombreCompleto(),
                emailGCM,
                bolsinSeleccionadoDto.getLatitud(),
                bolsinSeleccionadoDto.getLongitud(),
                bolsinSeleccionadoDto.getFechaHoraUltimaActualizacion(),
                modeloGPSSeleccionado);

        finCU();
        return resultado;
    }

    // Flujo Alternativo A4: El EB cancela la ejecucion del caso de uso
    public NotificacionResultadoResponse cancelarCU() {
        finCU();
        return new NotificacionResultadoResponse(
                false,
                true,
                "A4",
                "A4: El Encargado de Bolsines canceló la ejecución del caso de uso.",
                null, null, null, null, null, null, null, null, null, null);
    }

    // ==================== Metodos internos fieles al Diagrama de Secuencia
    // ====================

    private Empleado buscarEmpleadoLogueado(String nombreUsuarioDemo) {
        List<Empleado> empleados = empleadoRepository.findAll();

        if (nombreUsuarioDemo != null && !nombreUsuarioDemo.isBlank()) {
            Usuario usuarioDemo = new Usuario(nombreUsuarioDemo.trim(), "");
            return empleados.stream()
                    .filter(e -> e.esTuUsuario(usuarioDemo))
                    .findFirst()
                    .orElse(null);
        }

        Sesion sesionActual = sesionRepository.findAll().stream()
                .filter(Sesion::esActual)
                .findFirst()
                .orElse(null);

        if (sesionActual == null) {
            return null;
        }

        Usuario usuarioLogueado = sesionActual.getUsuario();
        return empleados.stream()
                .filter(e -> e.esTuUsuario(usuarioLogueado))
                .findFirst()
                .orElse(null);
    }

    private ComisionMedica buscarCMEmpleadoLogueado() {
        return this.empleadoLogueado.getCM();
    }

    private List<Bolsin> buscarBolsinesEnviadosEnCM() {
        return bolsinRepository.findAll().stream()
                .filter(b -> b.esTuCMOrigen(this.cmEmpleadoLogueado))
                .filter(Bolsin::sosEnviado)
                .toList();
    }

    private void buscarDatosBolsines() {
        int n = this.bolsinesEnviados.size();
        this.nroBolsines = new int[n];
        this.codCMDestinos = new String[n];
        for (int i = 0; i < n; i++) {
            Bolsin b = this.bolsinesEnviados.get(i);
            this.nroBolsines[i] = b.getNumeroBolsin();
            this.codCMDestinos[i] = b.obtenerCMDestino().getCodigo();
        }
    }

    private String[][] solicitarDatosUbicacionBolsin() {
        return interfazGPSTracker.obtenerUbicacionBolsin(
                this.nroBolsines,
                this.codCMOrigen,
                this.codCMDestinos,
                this.modeloGPSSeleccionado);
    }

    private MapaPosicionesResponse buscarMapa(List<BolsinUbicacionResponse> marcadores) {
        return iGoogleMaps.solicitarMapa(marcadores);
    }

    private Empleado buscarGCM(ComisionMedica cmDestino) {
        return empleadoRepository.findAll().stream()
                .filter(e -> e.esTuCM(cmDestino))
                .filter(Empleado::sosGCM)
                .findFirst()
                .orElse(null);
    }

    private String buscarEmailGCM(Empleado gerente) {
        return gerente.getEmail();
    }

    private List<BolsinUbicacionResponse> construirBolsinesConUbicacion() {
        List<BolsinUbicacionResponse> lista = new ArrayList<>();
        for (int i = 0; i < bolsinesEnviados.size(); i++) {
            Bolsin b = bolsinesEnviados.get(i);
            String[] fila = (ubicacionBolsines != null && i < ubicacionBolsines.length) ? ubicacionBolsines[i] : null;

            boolean ok = fila != null && "OK".equalsIgnoreCase(fila[4]);
            Double lat = ok ? parseDoubleSafe(fila[1]) : null;
            Double lng = ok ? parseDoubleSafe(fila[2]) : null;
            String fechaHora = ok ? fila[3] : null;
            String errorMsg = ok ? null : (fila != null ? fila[5] : "Sin datos de GPS Tracker");
            String raw = fila != null ? fila[6] : "";
            String mapsUrl = (ok && lat != null && lng != null)
                    ? String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", lat, lng)
                    : null;

            lista.add(new BolsinUbicacionResponse(
                    b.getNumeroBolsin(),
                    b.getNroPrecinto(),
                    b.getPeso(),
                    b.getFecha() != null ? b.getFecha().toString() : "",
                    b.getOrigen().getCodigo(),
                    b.getOrigen().getNombre(),
                    b.obtenerCMDestino().getCodigo(),
                    b.obtenerCMDestino().getNombre(),
                    lat,
                    lng,
                    fechaHora,
                    ok,
                    errorMsg,
                    modeloGPSSeleccionado,
                    raw,
                    mapsUrl));
        }
        return lista;
    }

    private Double parseDoubleSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void finCU() {
        this.bolsinesEnviados = new ArrayList<>();
        this.nroBolsines = null;
        this.codCMDestinos = null;
        this.ubicacionBolsines = null;
        this.bolsinesConUbicacion = new ArrayList<>();
        this.bolsinSeleccionado = null;
        this.bolsinSeleccionadoDto = null;
    }
}
