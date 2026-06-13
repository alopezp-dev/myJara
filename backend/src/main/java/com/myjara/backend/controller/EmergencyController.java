package com.myjara.backend.controller;

import com.myjara.backend.dto.*;
import com.myjara.backend.entity.EmergencyBox;
import com.myjara.backend.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    // GET /api/emergency/active — panel de urgencias
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<List<EmergencyEpisodeResponse>> findActive() {
        return ResponseEntity.ok(emergencyService.findActive());
    }

    // GET /api/emergency/patient/1
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<List<EmergencyEpisodeResponse>> findByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(emergencyService.findByPatient(patientId));
    }

    // GET /api/emergency/1
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<EmergencyEpisodeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.findById(id));
    }

    // GET /api/emergency/boxes
    @GetMapping("/boxes")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO','CELADOR')")
    public ResponseEntity<List<EmergencyBox>> findBoxes() {
        return ResponseEntity.ok(emergencyService.findBoxes());
    }

    // POST /api/emergency/arrival — registro de llegada con triaje
    @PostMapping("/arrival")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<EmergencyEpisodeResponse> registerArrival(
            @Valid @RequestBody EmergencyEpisodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emergencyService.registerArrival(request));
    }

    // PATCH /api/emergency/1/box?boxId=2&professionalId=1
    @PatchMapping("/{episodeId}/box")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<EmergencyEpisodeResponse> assignBox(
            @PathVariable Long episodeId,
            @RequestParam Long boxId,
            @RequestParam Long professionalId) {
        return ResponseEntity.ok(
                emergencyService.assignBox(episodeId, boxId, professionalId));
    }

    // PATCH /api/emergency/1/discharge?dischargeType=HOME
    @PatchMapping("/{episodeId}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ResponseEntity<EmergencyEpisodeResponse> discharge(
            @PathVariable Long episodeId,
            @RequestParam String dischargeType,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(
                emergencyService.discharge(episodeId, dischargeType, notes));
    }

    // POST /api/emergency/vital-signs
    @PostMapping("/vital-signs")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<VitalSignsResponse> recordVitalSigns(
            @Valid @RequestBody VitalSignsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emergencyService.recordVitalSigns(request));
    }

    // GET /api/emergency/1/vital-signs
    @GetMapping("/{episodeId}/vital-signs")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
    public ResponseEntity<List<VitalSignsResponse>> findVitalSigns(
            @PathVariable Long episodeId) {
        return ResponseEntity.ok(emergencyService.findVitalSigns(episodeId));
    }
}