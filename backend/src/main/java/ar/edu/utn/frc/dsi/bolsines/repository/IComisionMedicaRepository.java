package ar.edu.utn.frc.dsi.bolsines.repository;

import ar.edu.utn.frc.dsi.bolsines.model.ComisionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IComisionMedicaRepository extends JpaRepository<ComisionMedica, String> {
}
