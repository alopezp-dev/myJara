package com.myjara.backend.repository;

import com.myjara.backend.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    List<Admission> findByPatientIdOrderByAdmissionDateDesc(Long patientId);
    List<Admission> findByStatus(Admission.Status status);

    @Query("SELECT a FROM Admission a WHERE a.status = 'ACTIVE' " +
            "ORDER BY a.admissionDate ASC")
    List<Admission> findActiveAdmissions();
}