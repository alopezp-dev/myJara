package com.myjara.backend.repository;

import com.myjara.backend.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {

    List<ClinicalNote> findByEncounterIdOrderByCreatedAtDesc(Long encounterId);
    List<ClinicalNote> findByEncounterIdAndType(Long encounterId, ClinicalNote.Type type);
}