package com.myjara.backend.dto;

import com.myjara.backend.entity.Encounter;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class EncounterResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long professionalId;
    private String professionalName;
    private String type;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private String notes;

    public static EncounterResponse from(Encounter e) {
        EncounterResponse r = new EncounterResponse();
        r.id = e.getId();
        r.patientId = e.getPatient().getId();
        r.patientName = e.getPatient().getFirstName() + " " + e.getPatient().getLastName();
        r.professionalId = e.getProfessional().getId();
        r.professionalName = e.getProfessional().getFirstName() + " " + e.getProfessional().getLastName();
        r.type = e.getType().name();
        r.status = e.getStatus().name();
        r.startDate = e.getStartDate();
        r.endDate = e.getEndDate();
        r.reason = e.getReason();
        r.notes = e.getNotes();
        return r;
    }
}