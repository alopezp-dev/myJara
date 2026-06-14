package com.myjara.backend.repository;

import com.myjara.backend.entity.AdmissionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionNoteRepository extends JpaRepository<AdmissionNote, Long> {
    List<AdmissionNote> findByAdmissionIdOrderByCreatedAtDesc(Long admissionId);
}