package com.myjara.backend.dto;

import com.myjara.backend.entity.Prescription;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class PrescriptionResponse {

    private Long id;
    private Long patientId;
    private Long professionalId;
    private String professionalName;
    private Long medicationId;
    private String medicationName;
    private String activeIngredient;
    private String pharmaceuticalForm;
    private String dose;
    private String frequency;
    private String duration;
    private String route;
    private String instructions;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    public static PrescriptionResponse from(Prescription p) {
        PrescriptionResponse r = new PrescriptionResponse();
        r.id = p.getId();
        r.patientId = p.getPatient().getId();
        r.professionalId = p.getProfessional().getId();
        r.professionalName = p.getProfessional().getFirstName() + " " + p.getProfessional().getLastName();
        r.medicationId = p.getMedication().getId();
        r.medicationName = p.getMedication().getName();
        r.activeIngredient = p.getMedication().getActiveIngredient();
        r.pharmaceuticalForm = p.getMedication().getPharmaceuticalForm();
        r.dose = p.getDose();
        r.frequency = p.getFrequency();
        r.duration = p.getDuration();
        r.route = p.getRoute();
        r.instructions = p.getInstructions();
        r.status = p.getStatus().name();
        r.startDate = p.getStartDate();
        r.endDate = p.getEndDate();
        r.createdAt = p.getCreatedAt();
        return r;
    }
}