package com.myjara.backend.dto;

import com.myjara.backend.entity.Condition;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class ConditionResponse {

    private Long id;
    private Long patientId;
    private Long encounterId;
    private String cie10Code;
    private String cie10Desc;
    private String status;
    private LocalDate onsetDate;
    private LocalDate resolvedDate;
    private String notes;
    private LocalDateTime createdAt;

    public static ConditionResponse from(Condition c) {
        ConditionResponse r = new ConditionResponse();
        r.id = c.getId();
        r.patientId = c.getPatient().getId();
        r.encounterId = c.getEncounter().getId();
        r.cie10Code = c.getCie10Code();
        r.cie10Desc = c.getCie10Desc();
        r.status = c.getStatus().name();
        r.onsetDate = c.getOnsetDate();
        r.resolvedDate = c.getResolvedDate();
        r.notes = c.getNotes();
        r.createdAt = c.getCreatedAt();
        return r;
    }
}