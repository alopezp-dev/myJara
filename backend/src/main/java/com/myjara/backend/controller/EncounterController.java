package com.myjara.backend.controller;

import com.myjara.backend.dto.EncounterRequest;
import com.myjara.backend.dto.EncounterResponse;
import com.myjara.backend.service.EncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encounters")
@RequiredArgsConstructor
public class EncounterController {

    private final EncounterService encounterService;

    // GET /api/encounters/patient/1
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<EncounterResponse>> findByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(encounterService.findByPatient(patientId));
    }

    // GET /api/encounters/1
    @GetMapping("/{id}")
    public ResponseEntity<EncounterResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(encounterService.findById(id));
    }

    // POST /api/encounters
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<EncounterResponse> create(
            @Valid @RequestBody EncounterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(encounterService.create(request));
    }

    // PATCH /api/encounters/1/complete
    @PatchMapping("/{id}/complete")
    public ResponseEntity<EncounterResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(encounterService.complete(id));
    }

    // PATCH /api/encounters/1/notes
    @PatchMapping("/{id}/notes")
    public ResponseEntity<EncounterResponse> updateNotes(
            @PathVariable Long id,
            @RequestBody String notes) {
        return ResponseEntity.ok(encounterService.updateNotes(id, notes));
    }
}