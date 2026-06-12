package com.myjara.backend.service;

import com.myjara.backend.dto.*;
import com.myjara.backend.entity.*;
import com.myjara.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicalHistoryService {

    private final ConditionRepository conditionRepository;
    private final AllergyRepository allergyRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final Cie10CatalogRepository cie10CatalogRepository;
    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final ProfessionalRepository professionalRepository;

    // --- Diagnósticos ---

    @Transactional(readOnly = true)
    public List<ConditionResponse> findConditionsByPatient(Long patientId) {
        return conditionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(ConditionResponse::from).toList();
    }

    @Transactional
    public ConditionResponse addCondition(ConditionRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Encounter encounter = encounterRepository.findById(req.getEncounterId())
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado"));

        Condition condition = new Condition();
        condition.setPatient(patient);
        condition.setEncounter(encounter);
        condition.setCie10Code(req.getCie10Code());
        condition.setCie10Desc(req.getCie10Desc());
        condition.setOnsetDate(req.getOnsetDate());
        condition.setNotes(req.getNotes());
        if (req.getStatus() != null) {
            condition.setStatus(Condition.Status.valueOf(req.getStatus()));
        }
        return ConditionResponse.from(conditionRepository.save(condition));
    }

    @Transactional
    public ConditionResponse resolveCondition(Long id) {
        Condition condition = conditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado: " + id));
        condition.setStatus(Condition.Status.RESOLVED);
        condition.setResolvedDate(java.time.LocalDate.now());
        return ConditionResponse.from(conditionRepository.save(condition));
    }

    // --- Alergias ---

    @Transactional(readOnly = true)
    public List<AllergyResponse> findAllergiesByPatient(Long patientId) {
        return allergyRepository.findByPatientId(patientId)
                .stream().map(AllergyResponse::from).toList();
    }

    @Transactional
    public AllergyResponse addAllergy(AllergyRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Allergy allergy = new Allergy();
        allergy.setPatient(patient);
        allergy.setSubstance(req.getSubstance());
        allergy.setReaction(req.getReaction());
        allergy.setOnsetDate(req.getOnsetDate());
        if (req.getSeverity() != null) {
            allergy.setSeverity(Allergy.Severity.valueOf(req.getSeverity()));
        }
        return AllergyResponse.from(allergyRepository.save(allergy));
    }

    // --- Notas clínicas ---

    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> findNotesByEncounter(Long encounterId) {
        return clinicalNoteRepository.findByEncounterIdOrderByCreatedAtDesc(encounterId)
                .stream().map(ClinicalNoteResponse::from).toList();
    }

    @Transactional
    public ClinicalNoteResponse addNote(ClinicalNoteRequest req) {
        Encounter encounter = encounterRepository.findById(req.getEncounterId())
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado"));
        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        ClinicalNote note = new ClinicalNote();
        note.setEncounter(encounter);
        note.setProfessional(professional);
        note.setContent(req.getContent());
        if (req.getType() != null) {
            note.setType(ClinicalNote.Type.valueOf(req.getType()));
        }
        return ClinicalNoteResponse.from(clinicalNoteRepository.save(note));
    }

    // --- Catálogo CIE-10 ---

    public List<Cie10Catalog> searchCie10(String term) {
        return cie10CatalogRepository.search(term);
    }
}