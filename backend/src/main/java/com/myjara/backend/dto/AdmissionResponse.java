package com.myjara.backend.dto;

import com.myjara.backend.entity.Admission;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class AdmissionResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientHealthCard;
    private String bedCode;
    private String unitName;
    private String unitFloor;
    private Long professionalId;
    private String professionalName;
    private String admissionType;
    private String reason;
    private String diagnosis;
    private String status;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String dischargeNotes;
    private Long stayDays;

    public static AdmissionResponse from(Admission a) {
        AdmissionResponse r = new AdmissionResponse();
        r.id = a.getId();
        r.patientId = a.getPatient().getId();
        r.patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();
        r.patientHealthCard = a.getPatient().getHealthCard();
        r.bedCode = a.getBed().getBedCode();
        r.unitName = a.getBed().getUnit().getName();
        r.unitFloor = a.getBed().getUnit().getFloor();
        r.professionalId = a.getProfessional().getId();
        r.professionalName = a.getProfessional().getFirstName() + " " +
                a.getProfessional().getLastName();
        r.admissionType = a.getAdmissionType().name();
        r.reason = a.getReason();
        r.diagnosis = a.getDiagnosis();
        r.status = a.getStatus().name();
        r.admissionDate = a.getAdmissionDate();
        r.dischargeDate = a.getDischargeDate();
        r.dischargeNotes = a.getDischargeNotes();
        if (a.getAdmissionDate() != null) {
            LocalDateTime end = a.getDischargeDate() != null ?
                    a.getDischargeDate() : LocalDateTime.now();
            r.stayDays = java.time.Duration.between(a.getAdmissionDate(), end).toDays();
        }
        return r;
    }
}