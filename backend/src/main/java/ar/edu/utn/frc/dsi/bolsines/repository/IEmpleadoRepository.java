package ar.edu.utn.frc.dsi.bolsines.repository;

import ar.edu.utn.frc.dsi.bolsines.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IEmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByUsuarioNombreUsuario(String nombreUsuario);
}
