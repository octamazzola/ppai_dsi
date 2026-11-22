package ar.edu.utn.frc.dsi.bolsines.repository;

import ar.edu.utn.frc.dsi.bolsines.model.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBolsinRepository extends JpaRepository<Bolsin, Integer> {
}
