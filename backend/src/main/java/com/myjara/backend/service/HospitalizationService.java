package com.myjara.backend.service;

import com.myjara.backend.dto.*;
import com.myjara.backend.entity.*;
import com.myjara.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalizationService {

    private final AdmissionRepository admissionRepository;
    private final AdmissionNoteRepository admissionNoteRepository;
    private final HospitalUnitRepository unitRepository;
    private final HospitalBedRepository bedRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;

    @Transactional(readOnly = true)
    public List<HospitalUnit> findUnits() {
        return unitRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<HospitalBed> findBedsByUnit(Long unitId) {
        return bedRepository.findByUnitIdAndActiveTrue(unitId);
    }

    @Transactional(readOnly = true)
    public List<HospitalBed> findFreeBeds() {
        return bedRepository.findByStatusAndActiveTrue(HospitalBed.Status.FREE);
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> findActiveAdmissions() {
        return admissionRepository.findActiveAdmissions()
                .stream().map(AdmissionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> findByPatient(Long patientId) {
        return admissionRepository.findByPatientIdOrderByAdmissionDateDesc(patientId)
                .stream().map(AdmissionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AdmissionResponse findById(Long id) {
        return AdmissionResponse.from(admissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado: " + id)));
    }

    @Transactional
    public AdmissionResponse admit(AdmissionRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        HospitalBed bed = bedRepository.findById(req.getBedId())
                .orElseThrow(() -> new RuntimeException("Cama no encontrada"));

        if (bed.getStatus() != HospitalBed.Status.FREE) {
            throw new RuntimeException("La cama " + bed.getBedCode() + " no está disponible");
        }

        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setBed(bed);
        admission.setProfessional(professional);
        admission.setReason(req.getReason());
        admission.setDiagnosis(req.getDiagnosis());

        if (req.getAdmissionType() != null) {
            admission.setAdmissionType(
                    Admission.AdmissionType.valueOf(req.getAdmissionType()));
        }

        bed.setStatus(HospitalBed.Status.OCCUPIED);
        bedRepository.save(bed);

        return AdmissionResponse.from(admissionRepository.save(admission));
    }

    @Transactional
    public AdmissionResponse discharge(Long admissionId, String notes) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

        admission.setStatus(Admission.Status.DISCHARGED);
        admission.setDischargeDate(LocalDateTime.now());
        admission.setDischargeNotes(notes);

        admission.getBed().setStatus(HospitalBed.Status.CLEANING);
        bedRepository.save(admission.getBed());

        return AdmissionResponse.from(admissionRepository.save(admission));
    }

    @Transactional
    public AdmissionNoteResponse addNote(AdmissionNoteRequest req) {
        Admission admission = admissionRepository.findById(req.getAdmissionId())
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));

        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        AdmissionNote note = new AdmissionNote();
        note.setAdmission(admission);
        note.setProfessional(professional);
        note.setContent(req.getContent());

        if (req.getNoteType() != null) {
            note.setNoteType(AdmissionNote.NoteType.valueOf(req.getNoteType()));
        }

        return AdmissionNoteResponse.from(admissionNoteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<AdmissionNoteResponse> findNotes(Long admissionId) {
        return admissionNoteRepository.findByAdmissionIdOrderByCreatedAtDesc(admissionId)
                .stream().map(AdmissionNoteResponse::from).toList();
    }
}