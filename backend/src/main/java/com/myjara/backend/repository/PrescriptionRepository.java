package com.myjara.backend.repository;

import com.myjara.backend.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Prescription> findByPatientIdAndStatus(Long patientId, Prescription.Status status);
    List<Prescription> findByEncounterId(Long encounterId);

    // Medicación activa del paciente para detección de interacciones
    @Query("SELECT p FROM Prescription p WHERE " +
            "p.patient.id = :patientId AND " +
            "p.status = 'ACTIVE'")
    List<Prescription> findActiveMedication(@Param("patientId") Long patientId);
}