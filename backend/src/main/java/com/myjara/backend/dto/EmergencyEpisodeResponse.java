package com.myjara.backend.dto;

import com.myjara.backend.entity.EmergencyEpisode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class EmergencyEpisodeResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientHealthCard;
    private Integer triageLevel;
    private String triageColor;
    private String chiefComplaint;
    private String status;
    private String boxName;
    private String professionalName;
    private LocalDateTime arrivalTime;
    private LocalDateTime triageTime;
    private LocalDateTime attentionTime;
    private LocalDateTime dischargeTime;
    private String dischargeType;
    private String notes;
    private Long waitingMinutes;

    public static EmergencyEpisodeResponse from(EmergencyEpisode e) {
        EmergencyEpisodeResponse r = new EmergencyEpisodeResponse();
        r.id = e.getId();
        r.patientId = e.getPatient().getId();
        r.patientName = e.getPatient().getFirstName() + " " + e.getPatient().getLastName();
        r.patientHealthCard = e.getPatient().getHealthCard();
        r.triageLevel = e.getTriageLevel();
        r.triageColor = e.getTriageColor();
        r.chiefComplaint = e.getChiefComplaint();
        r.status = e.getStatus().name();
        r.boxName = e.getBox() != null ? e.getBox().getName() : null;
        r.professionalName = e.getProfessional() != null
                ? e.getProfessional().getFirstName() + " " + e.getProfessional().getLastName()
                : null;
        r.arrivalTime = e.getArrivalTime();
        r.triageTime = e.getTriageTime();
        r.attentionTime = e.getAttentionTime();
        r.dischargeTime = e.getDischargeTime();
        r.dischargeType = e.getDischargeType() != null ? e.getDischargeType().name() : null;
        r.notes = e.getNotes();
        if (e.getArrivalTime() != null && e.getStatus() == EmergencyEpisode.Status.WAITING) {
            r.waitingMinutes = java.time.Duration.between(
                    e.getArrivalTime(), LocalDateTime.now()).toMinutes();
        }
        return r;
    }
}