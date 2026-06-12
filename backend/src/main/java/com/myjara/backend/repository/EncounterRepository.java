package com.myjara.backend.repository;

import com.myjara.backend.entity.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    List<Encounter> findByPatientIdOrderByStartDateDesc(Long patientId);
    List<Encounter> findByProfessionalIdAndStatus(Long professionalId, Encounter.Status status);
    List<Encounter> findByPatientIdAndStatus(Long patientId, Encounter.Status status);
}