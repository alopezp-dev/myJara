package com.myjara.backend.repository;

import com.myjara.backend.entity.Condition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionRepository extends JpaRepository<Condition, Long> {

    List<Condition> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Condition> findByEncounterId(Long encounterId);
    List<Condition> findByPatientIdAndStatus(Long patientId, Condition.Status status);
}