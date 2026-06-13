package com.myjara.backend.controller;

import com.myjara.backend.dto.AppointmentRequest;
import com.myjara.backend.dto.AppointmentResponse;
import com.myjara.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // GET /api/appointments/patient/1
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> findByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.findByPatient(patientId));
    }

    // GET /api/appointments/professional/1?from=2026-06-01T00:00:00&to=2026-06-30T23:59:59
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<AppointmentResponse>> findByProfessional(
            @PathVariable Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                appointmentService.findByProfessionalAndRange(professionalId, from, to));
    }

    // GET /api/appointments/slots?professionalId=1&agendaId=1&date=2026-06-16
    @GetMapping("/slots")
    public ResponseEntity<List<String>> findAvailableSlots(
            @RequestParam Long professionalId,
            @RequestParam Long agendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                appointmentService.findAvailableSlots(professionalId, agendaId, date));
    }

    // POST /api/appointments
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO','ADMINISTRATIVO')")
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.create(request));
    }

    // PATCH /api/appointments/1/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    // DELETE /api/appointments/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}