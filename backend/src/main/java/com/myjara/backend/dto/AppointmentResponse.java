package com.myjara.backend.dto;

import com.myjara.backend.entity.Appointment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long professionalId;
    private String professionalName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String reason;
    private String notes;

    public static AppointmentResponse from(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.id = a.getId();
        r.patientId = a.getPatient().getId();
        r.patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();
        r.professionalId = a.getProfessional().getId();
        r.professionalName = a.getProfessional().getFirstName() + " " + a.getProfessional().getLastName();
        r.startTime = a.getStartTime();
        r.endTime = a.getEndTime();
        r.status = a.getStatus().name();
        r.reason = a.getReason();
        r.notes = a.getNotes();
        return r;
    }
}