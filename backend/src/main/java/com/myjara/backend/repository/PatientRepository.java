package com.myjara.backend.repository;

import com.myjara.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Spring genera el SQL automáticamente a partir del nombre del método
    Optional<Patient> findByHealthCard(String healthCard);

    Optional<Patient> findByDni(String dni);

    List<Patient> findByActiveTrue();

    // Búsqueda combinada por nombre, apellido, tarjeta sanitaria o DNI
    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "p.healthCard LIKE CONCAT('%', :term, '%') OR " +
            "p.dni LIKE CONCAT('%', :term, '%')")
    List<Patient> search(@Param("term") String term);
}