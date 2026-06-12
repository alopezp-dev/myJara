package com.myjara.backend.repository;

import com.myjara.backend.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    List<Allergy> findByPatientIdAndStatus(Long patientId, Allergy.Status status);
    List<Allergy> findByPatientId(Long patientId);
}