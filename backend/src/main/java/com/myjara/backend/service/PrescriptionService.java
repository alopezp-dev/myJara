package com.myjara.backend.service;

import com.myjara.backend.dto.InteractionWarning;
import com.myjara.backend.dto.PrescriptionRequest;
import com.myjara.backend.dto.PrescriptionResponse;
import com.myjara.backend.entity.*;
import com.myjara.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final DrugInteractionRepository drugInteractionRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final EncounterRepository encounterRepository;

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> findByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(PrescriptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> findActiveByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdAndStatus(
                        patientId, Prescription.Status.ACTIVE)
                .stream().map(PrescriptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InteractionWarning> checkInteractions(Long patientId, Long medicationId) {
        List<Prescription> active = prescriptionRepository.findActiveMedication(patientId);
        if (active.isEmpty()) return List.of();

        List<Long> activeMedIds = active.stream()
                .map(p -> p.getMedication().getId())
                .toList();

        return drugInteractionRepository.findInteractions(medicationId, activeMedIds)
                .stream().map(InteractionWarning::from).toList();
    }

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        Medication medication = medicationRepository.findById(req.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setProfessional(professional);
        prescription.setMedication(medication);
        prescription.setDose(req.getDose());
        prescription.setFrequency(req.getFrequency());
        prescription.setDuration(req.getDuration());
        prescription.setRoute(req.getRoute() != null ? req.getRoute() : medication.getRoute());
        prescription.setInstructions(req.getInstructions());
        if (req.getStartDate() != null) prescription.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) prescription.setEndDate(req.getEndDate());

        if (req.getEncounterId() != null) {
            encounterRepository.findById(req.getEncounterId())
                    .ifPresent(prescription::setEncounter);
        }

        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    @Transactional
    public PrescriptionResponse updateStatus(Long id, String status) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescripción no encontrada: " + id));
        p.setStatus(Prescription.Status.valueOf(status));
        return PrescriptionResponse.from(prescriptionRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<Medication> searchMedications(String term) {
        return medicationRepository.search(term);
    }
}