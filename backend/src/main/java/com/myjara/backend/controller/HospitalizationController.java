package com.myjara.backend.controller;

import com.myjara.backend.dto.*;
import com.myjara.backend.entity.*;
import com.myjara.backend.service.HospitalizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitalization")
@RequiredArgsConstructor
public class HospitalizationController {

    private final HospitalizationService hospitalizationService;

    @GetMapping("/units")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<List<HospitalUnit>> findUnits() {
        return ResponseEntity.ok(hospitalizationService.findUnits());
    }

    @GetMapping("/units/{unitId}/beds")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<List<HospitalBed>> findBedsByUnit(
            @PathVariable Long unitId) {
        return ResponseEntity.ok(hospitalizationService.findBedsByUnit(unitId));
    }

    @GetMapping("/beds/free")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<List<HospitalBed>> findFreeBeds() {
        return ResponseEntity.ok(hospitalizationService.findFreeBeds());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<List<AdmissionResponse>> findActiveAdmissions() {
        return ResponseEntity.ok(hospitalizationService.findActiveAdmissions());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<List<AdmissionResponse>> findByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(hospitalizationService.findByPatient(patientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<AdmissionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalizationService.findById(id));
    }

    @PostMapping("/admit")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<AdmissionResponse> admit(
            @Valid @RequestBody AdmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hospitalizationService.admit(request));
    }

    @PatchMapping("/{admissionId}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<AdmissionResponse> discharge(
            @PathVariable Long admissionId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(hospitalizationService.discharge(admissionId, notes));
    }

    @PostMapping("/notes")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<AdmissionNoteResponse> addNote(
            @Valid @RequestBody AdmissionNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hospitalizationService.addNote(request));
    }

    @GetMapping("/{admissionId}/notes")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<List<AdmissionNoteResponse>> findNotes(
            @PathVariable Long admissionId) {
        return ResponseEntity.ok(hospitalizationService.findNotes(admissionId));
    }
}