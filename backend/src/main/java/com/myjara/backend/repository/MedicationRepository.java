package com.myjara.backend.repository;

import com.myjara.backend.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByNationalCode(String nationalCode);
    List<Medication> findByActiveTrue();

    @Query("SELECT m FROM Medication m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(m.activeIngredient) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "m.nationalCode LIKE CONCAT('%', :term, '%')")
    List<Medication> search(@Param("term") String term);
}