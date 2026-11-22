package ar.edu.utn.frc.dsi.bolsines.repository;

import ar.edu.utn.frc.dsi.bolsines.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEmpleadoRepository extends JpaRepository<Empleado, Long> {
}
