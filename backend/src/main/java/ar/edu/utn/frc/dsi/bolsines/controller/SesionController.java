package ar.edu.utn.frc.dsi.bolsines.controller;

import ar.edu.utn.frc.dsi.bolsines.model.Empleado;
import ar.edu.utn.frc.dsi.bolsines.repository.IEmpleadoRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/seguimiento")
public class SesionController {

    private static final String USUARIO_AUTENTICADO = "usuarioAutenticado";
    private final IEmpleadoRepository empleadoRepository;

    public SesionController(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioAutenticado> iniciarSesion(
            @RequestBody Credenciales credenciales, HttpServletRequest request) {
        Empleado empleado = empleadoRepository.findByUsuarioNombreUsuario(credenciales.nombreUsuario())
                .filter(e -> e.getRol() != null && "EB".equals(e.getRol().getNombre()))
                .filter(e -> e.getUsuario().getPassword().equals(credenciales.password()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        cerrarSesion(request);
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                empleado.getId(), empleado.getUsuario().getNombreUsuario(), empleado.getNombreCompleto(),
                empleado.getCM().getCodigo(), empleado.mostrarCM());
        request.getSession().setAttribute(USUARIO_AUTENTICADO, usuario);
        return respuestaSesion(usuario);
    }

    @GetMapping("/sesion")
    public ResponseEntity<UsuarioAutenticado> consultarSesion(HttpServletRequest request) {
        return respuestaSesion(requerirUsuario(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    static UsuarioAutenticado requerirUsuario(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute(USUARIO_AUTENTICADO) instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<UsuarioAutenticado> respuestaSesion(UsuarioAutenticado usuario) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(usuario);
    }

    public record Credenciales(String nombreUsuario, String password) {
        @Override
        public String toString() {
            return "Credenciales[password=PROTEGIDA]";
        }
    }

    public record UsuarioAutenticado(
            @JsonIgnore Long empleadoId,
            String nombreUsuario, String nombreEmpleado, String codigoCM, String nombreCM) {}
}
