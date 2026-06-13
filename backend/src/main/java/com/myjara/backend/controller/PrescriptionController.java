package com.myjara.backend.controller;

import com.myjara.backend.dto.InteractionWarning;
import com.myjara.backend.dto.PrescriptionRequest;
import com.myjara.backend.dto.PrescriptionResponse;
import com.myjara.backend.entity.Medication;
import com.myjara.backend.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // GET /api/prescriptions/patient/1
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<List<PrescriptionResponse>> findByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.findByPatient(patientId));
    }

    // GET /api/prescriptions/patient/1/active
    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<List<PrescriptionResponse>> findActiveByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.findActiveByPatient(patientId));
    }

    // GET /api/prescriptions/interactions?patientId=1&medicationId=2
    @GetMapping("/interactions")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<List<InteractionWarning>> checkInteractions(
            @RequestParam Long patientId,
            @RequestParam Long medicationId) {
        return ResponseEntity.ok(
                prescriptionService.checkInteractions(patientId, medicationId));
    }

    // GET /api/prescriptions/medications/search?term=ibuprofeno
    @GetMapping("/medications/search")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<List<Medication>> searchMedications(
            @RequestParam String term) {
        return ResponseEntity.ok(prescriptionService.searchMedications(term));
    }

    // POST /api/prescriptions
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<PrescriptionResponse> create(
            @Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(request));
    }

    // PATCH /api/prescriptions/1/status?status=CANCELLED
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<PrescriptionResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(prescriptionService.updateStatus(id, status));
    }
}