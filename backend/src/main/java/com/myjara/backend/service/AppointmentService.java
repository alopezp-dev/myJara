package com.myjara.backend.service;

import com.myjara.backend.dto.AppointmentRequest;
import com.myjara.backend.dto.AppointmentResponse;
import com.myjara.backend.entity.*;
import com.myjara.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final AgendaRepository agendaRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByProfessionalAndRange(
            Long professionalId, LocalDateTime from, LocalDateTime to) {
        return appointmentRepository
                .findByProfessionalIdAndStartTimeBetween(professionalId, from, to)
                .stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<String> findAvailableSlots(Long professionalId, Long agendaId, LocalDate date) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda no encontrada"));

        // Generar todos los slots del día
        List<String> allSlots = new ArrayList<>();
        LocalTime current = agenda.getStartTime();
        while (current.plusMinutes(agenda.getSlotMinutes()).compareTo(agenda.getEndTime()) <= 0) {
            allSlots.add(current.toString());
            current = current.plusMinutes(agenda.getSlotMinutes());
        }

        // Obtener citas ya ocupadas ese día
        LocalDateTime from = date.atTime(agenda.getStartTime());
        LocalDateTime to = date.atTime(agenda.getEndTime());
        List<Appointment> existing = appointmentRepository
                .findByProfessionalIdAndStartTimeBetween(professionalId, from, to)
                .stream()
                .filter(a -> a.getStatus() != Appointment.Status.CANCELLED
                        && a.getStatus() != Appointment.Status.NO_SHOW)
                .toList();

        // Eliminar slots ocupados
        Set<String> occupied = existing.stream()
                .map(a -> a.getStartTime().toLocalTime().toString())
                .collect(java.util.stream.Collectors.toSet());

        return allSlots.stream()
                .filter(s -> !occupied.contains(s))
                .toList();
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {

        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Professional professional = professionalRepository.findById(req.getProfessionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        Agenda agenda = agendaRepository.findById(req.getAgendaId())
                .orElseThrow(() -> new RuntimeException("Agenda no encontrada"));

        // Calcular hora de fin según duración del slot
        LocalDateTime endTime = req.getStartTime().plusMinutes(agenda.getSlotMinutes());

        // Comprobar solapamiento
        boolean overlap = appointmentRepository.existsOverlap(
                professional.getId(), req.getStartTime(), endTime);
        if (overlap) {
            throw new RuntimeException(
                    "El profesional ya tiene una cita en ese horario");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setAgenda(agenda);
        appointment.setStartTime(req.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setReason(req.getReason());

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + id));
        appointment.setStatus(Appointment.Status.valueOf(status));
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public void cancel(Long id) {
        updateStatus(id, "CANCELLED");
    }
}