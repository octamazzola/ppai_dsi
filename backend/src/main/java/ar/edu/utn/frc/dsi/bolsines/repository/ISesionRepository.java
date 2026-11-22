package ar.edu.utn.frc.dsi.bolsines.repository;

import ar.edu.utn.frc.dsi.bolsines.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISesionRepository extends JpaRepository<Sesion, Long> {
}
