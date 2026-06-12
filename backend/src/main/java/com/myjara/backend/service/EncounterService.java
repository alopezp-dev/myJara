package com.myjara.backend.service;

import com.myjara.backend.dto.EncounterRequest;
import com.myjara.backend.dto.EncounterResponse;
import com.myjara.backend.entity.*;
import com.myjara.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<EncounterResponse> findByPatient(Long patientId) {
        return encounterRepository.findByPatientIdOrderByStartDateDesc(patientId)
                .stream().map(EncounterResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EncounterResponse findById(Long id) {
        Encounter e = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado: " + id));
        return EncounterResponse.from(e);
    }

    @Transactional
    public EncounterResponse create(EncounterRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setProfessional(professional);

        if (req.getAppointmentId() != null) {
            appointmentRepository.findById(req.getAppointmentId())
                    .ifPresent(encounter::setAppointment);
        }

        if (req.getType() != null) {
            encounter.setType(Encounter.Type.valueOf(req.getType()));
        }

        encounter.setReason(req.getReason());
        return EncounterResponse.from(encounterRepository.save(encounter));
    }

    @Transactional
    public EncounterResponse complete(Long id) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado: " + id));
        encounter.setStatus(Encounter.Status.COMPLETED);
        encounter.setEndDate(LocalDateTime.now());
        return EncounterResponse.from(encounterRepository.save(encounter));
    }

    @Transactional
    public EncounterResponse updateNotes(Long id, String notes) {
        Encounter encounter = encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Episodio no encontrado: " + id));
        encounter.setNotes(notes);
        return EncounterResponse.from(encounterRepository.save(encounter));
    }
}