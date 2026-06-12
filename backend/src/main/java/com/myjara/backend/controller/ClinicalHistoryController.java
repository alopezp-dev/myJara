package com.myjara.backend.controller;

import com.myjara.backend.dto.*;
import com.myjara.backend.entity.Cie10Catalog;
import com.myjara.backend.service.ClinicalHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical")
@RequiredArgsConstructor
public class ClinicalHistoryController {

    private final ClinicalHistoryService clinicalHistoryService;

    // --- Diagnósticos ---

    // GET /api/clinical/conditions/patient/1
    @GetMapping("/conditions/patient/{patientId}")
    public ResponseEntity<List<ConditionResponse>> findConditionsByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                clinicalHistoryService.findConditionsByPatient(patientId));
    }

    // POST /api/clinical/conditions
    @PostMapping("/conditions")
    public ResponseEntity<ConditionResponse> addCondition(
            @Valid @RequestBody ConditionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicalHistoryService.addCondition(request));
    }

    // PATCH /api/clinical/conditions/1/resolve
    @PatchMapping("/conditions/{id}/resolve")
    public ResponseEntity<ConditionResponse> resolveCondition(@PathVariable Long id) {
        return ResponseEntity.ok(clinicalHistoryService.resolveCondition(id));
    }

    // --- Alergias ---

    // GET /api/clinical/allergies/patient/1
    @GetMapping("/allergies/patient/{patientId}")
    public ResponseEntity<List<AllergyResponse>> findAllergiesByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                clinicalHistoryService.findAllergiesByPatient(patientId));
    }

    // POST /api/clinical/allergies
    @PostMapping("/allergies")
    public ResponseEntity<AllergyResponse> addAllergy(
            @Valid @RequestBody AllergyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicalHistoryService.addAllergy(request));
    }

    // --- Notas clínicas ---

    // GET /api/clinical/notes/encounter/1
    @GetMapping("/notes/encounter/{encounterId}")
    public ResponseEntity<List<ClinicalNoteResponse>> findNotesByEncounter(
            @PathVariable Long encounterId) {
        return ResponseEntity.ok(
                clinicalHistoryService.findNotesByEncounter(encounterId));
    }

    // POST /api/clinical/notes
    @PostMapping("/notes")
    public ResponseEntity<ClinicalNoteResponse> addNote(
            @Valid @RequestBody ClinicalNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clinicalHistoryService.addNote(request));
    }

    // --- Catálogo CIE-10 ---

    // GET /api/clinical/cie10/search?term=asma
    @GetMapping("/cie10/search")
    public ResponseEntity<List<Cie10Catalog>> searchCie10(
            @RequestParam String term) {
        return ResponseEntity.ok(clinicalHistoryService.searchCie10(term));
    }
}