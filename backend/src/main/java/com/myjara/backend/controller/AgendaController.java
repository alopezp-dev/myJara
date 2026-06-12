package com.myjara.backend.controller;

import com.myjara.backend.dto.AgendaRequest;
import com.myjara.backend.entity.Agenda;
import com.myjara.backend.entity.Professional;
import com.myjara.backend.repository.AgendaRepository;
import com.myjara.backend.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendas")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaRepository agendaRepository;
    private final ProfessionalRepository professionalRepository;

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<Agenda>> findByProfessional(
            @PathVariable Long professionalId) {
        return ResponseEntity.ok(
                agendaRepository.findByProfessionalIdAndActiveTrue(professionalId));
    }

    @PostMapping
    public ResponseEntity<Agenda> create(@RequestBody AgendaRequest req) {
        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        Agenda agenda = new Agenda();
        agenda.setProfessional(professional);
        agenda.setCenter(req.getCenter());
        agenda.setDayOfWeek(req.getDayOfWeek());
        agenda.setStartTime(req.getStartTime());
        agenda.setEndTime(req.getEndTime());
        agenda.setSlotMinutes(req.getSlotMinutes());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendaRepository.save(agenda));
    }
}