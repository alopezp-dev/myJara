package com.myjara.backend.controller;

import com.myjara.backend.dto.ProfessionalRequest;
import com.myjara.backend.dto.ProfessionalResponse;
import com.myjara.backend.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final ProfessionalService professionalService;

    // GET /api/professionals
    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll() {
        return ResponseEntity.ok(professionalService.findAll());
    }

    // GET /api/professionals/1
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.findById(id));
    }

    // GET /api/professionals?specialty=Cardiologia
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<ProfessionalResponse>> findBySpecialty(
            @PathVariable String specialty) {
        return ResponseEntity.ok(professionalService.findBySpecialty(specialty));
    }

    // POST /api/professionals
    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(
            @Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(professionalService.create(request));
    }

    // DELETE /api/professionals/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}